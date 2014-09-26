package org.wildstang.wildrank.android.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.wildstang.wildrank.android.database.ConflictDescriptor;
import org.wildstang.wildrank.android.database.ConflictResolutionResult;
import org.wildstang.wildrank.android.database.ConflictResolver;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.database.DatabaseHelper;
import org.wildstang.wildrank.android.database.ExternalStorageDatabaseContext;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;
import org.wildstang.wildrank.android.utils.Constants;

import java.io.File;

/**
 * Synchronizes two tables, an internal one and an external one.
 * <p/>
 * Only completely successful transactions will be committed; if anything fails,
 * nothing is committed. This means that the sync is completely atomic:
 * it's an all-or-nothing operation.
 */
public class TaskSynchronizeDatabases extends AsyncTask<Void, Object, Void> {

    private Context context;
    private ConflictResolver resolver;
    private TaskFragment.TaskCallbacks callbacks;

    public TaskSynchronizeDatabases(Context context, TaskFragment.TaskCallbacks callbacks, ConflictResolver resolver) {

        this.resolver = resolver;
        this.context = context;
        this.callbacks = callbacks;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SQLiteDatabase internal = new DatabaseHelper(context).getWritableDatabase();
        SQLiteDatabase external = new ExternalStorageDatabaseContext(new File(Constants.USB_FLASH_DRIVE_ROOT_PATH), context).openOrCreateDatabase("wildrank.db", -1, null);
        // All subsequent operations will be part of a single transation to ensure atomicity.
        // That way, if part of the sync fails, we will automatically revert to a known good state.
        internal.beginTransaction();
        external.beginTransaction();

        // Get a cursor containing all the records modified since the last sync
        Cursor changedRecords = internal.rawQuery("SELECT ?, ?, ? FROM ?", new String[]{DatabaseContract.TrackChanges.UUID, DatabaseContract.TrackChanges.RECORD_TABLE, DatabaseContract.TrackChanges.OPERATION, DatabaseContract.TrackChanges.TABLE_NAME});
        changedRecords.moveToPosition(-1);
        while (changedRecords.moveToNext()) {
            String UUID = changedRecords.getString(changedRecords.getColumnIndex(DatabaseContract.TrackChanges.UUID));
            String tableName = changedRecords.getString(changedRecords.getColumnIndex(DatabaseContract.TrackChanges.RECORD_TABLE));
            String operation = changedRecords.getString(changedRecords.getColumnIndex(DatabaseContract.TrackChanges.OPERATION));
            // Selects all records in the external database with this UUID
            Cursor currentExternal = external.rawQuery("SELECT ? FROM ? WHERE ? = ?", new String[]{DatabaseContract.BaseColumns.HASH, tableName, DatabaseContract.BaseColumns.UUID, UUID});
            // Checks if a record with this UUID existed when the database was last synced
            Cursor lastKnownExternal = internal.rawQuery("SELECT ?, ? FROM ?", new String[]{DatabaseContract.ExternalState.UUID, DatabaseContract.ExternalState.HASH, DatabaseContract.ExternalState.TABLE_NAME});
            boolean existsInExternal = (currentExternal.getCount() > 0);
            String externalHash = currentExternal.getString(currentExternal.getColumnIndex(DatabaseContract.BaseColumns.HASH));
            boolean existedAfterLastSync = (lastKnownExternal.getCount() > 0);
            String hashAfterLastSync = lastKnownExternal.getString(lastKnownExternal.getColumnIndex(DatabaseContract.BaseColumns.HASH));

            // These cursors represent the state of the given record in both the internal and external tables at this moment
            Cursor originalCursor = internal.rawQuery("select * FROM ? WHERE ? = ?", new String[]{tableName, DatabaseContract.BaseColumns.UUID, UUID});
            Cursor externalCursor = external.rawQuery("SELECT * FROM ? WHERE ? = ?", new String[]{tableName, DatabaseContract.BaseColumns.UUID, UUID});

            switch (operation) {
                case DatabaseContract.TrackChanges.Operation.CREATE:
                    // This was newly created, we can safely insert this in the external database.
                    originalCursor.moveToPosition(-1);
                    if (originalCursor.moveToNext()) {
                        ContentValues contentValues = getContentValuesForCursorRow(originalCursor);
                        external.insert(tableName, null, contentValues);
                    }
                    break;

                case DatabaseContract.TrackChanges.Operation.UPDATE:
                    if (existsInExternal && existedAfterLastSync) {
                        if (externalHash.equals(hashAfterLastSync)) {
                            // The external record has not changed, sync the internal to the external.
                            originalCursor.moveToPosition(-1);
                            if (originalCursor.moveToNext()) {
                                ContentValues contentValues = getContentValuesForCursorRow(originalCursor);
                                external.update(tableName, contentValues, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                            }
                            break;
                        } else {
                            // Both the internal and external records were modified since the last sync.
                            // We need to do conflict resolution.

                            publishProgress(new Object[]{new ConflictDescriptor(originalCursor, operation, externalCursor, DatabaseContract.TrackChanges.Operation.UPDATE)});

                            ConflictResolutionResult.ResolutionStrategy strategy = resolver.getConflictResolutionResult();
                            switch (strategy) {
                                case ACCEPT_ORIGINAL:
                                    originalCursor.moveToPosition(-1);
                                    if (originalCursor.moveToNext()) {
                                        ContentValues contentValues = getContentValuesForCursorRow(originalCursor);
                                        external.update(tableName, contentValues, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                                    }
                                    break;
                                case ACCEPT_INCOMING:
                                    externalCursor.moveToPosition(-1);
                                    if (externalCursor.moveToNext()) {
                                        ContentValues contentValues = getContentValuesForCursorRow(externalCursor);
                                        internal.update(tableName, contentValues, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                                    }
                                    break;
                            }
                        }
                    } else if (existedAfterLastSync && !existsInExternal) {
                        // This was deleted in the external database since the last sync, but has been modified
                        // locally since then. Perform conflict resolution.

                        publishProgress(new Object[]{new ConflictDescriptor(originalCursor, operation, externalCursor, DatabaseContract.TrackChanges.Operation.DELETE)});

                        ConflictResolutionResult.ResolutionStrategy strategy = resolver.getConflictResolutionResult();
                        switch (strategy) {
                            case ACCEPT_ORIGINAL:
                                originalCursor.moveToPosition(-1);
                                if (originalCursor.moveToNext()) {
                                    ContentValues contentValues = getContentValuesForCursorRow(originalCursor);
                                    external.insert(tableName, null, contentValues);
                                }
                                break;
                            case ACCEPT_INCOMING:
                                internal.delete(tableName, "? = ?", new String[] {DatabaseContract.BaseColumns.UUID, UUID});
                                break;
                        }

                    }
                    break;

                case DatabaseContract.TrackChanges.Operation.DELETE:
                    if (existsInExternal && existedAfterLastSync) {
                        // This record currently exists in the external and existed after the last sync
                        if (externalHash.equals(hashAfterLastSync)) {
                            // The external record has not changed. Go ahead and delete it.
                            external.delete(tableName, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                        } else {
                            // The external record has been modified since the last sync. We need to
                            // perform conflict resolution.

                            publishProgress(new Object[]{new ConflictDescriptor(originalCursor, operation, externalCursor, DatabaseContract.TrackChanges.Operation.UPDATE)});

                            ConflictResolutionResult.ResolutionStrategy strategy = resolver.getConflictResolutionResult();
                            switch (strategy) {
                                case ACCEPT_ORIGINAL:
                                    // We should delete the external record
                                    external.delete(tableName, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                                    break;
                                case ACCEPT_INCOMING:
                                    // We should update the internal record with the incoming record
                                    externalCursor.moveToPosition(-1);
                                    if (externalCursor.moveToNext()) {
                                        ContentValues contentValues = getContentValuesForCursorRow(externalCursor);
                                        internal.update(tableName, contentValues, "? = ?", new String[]{DatabaseContract.BaseColumns.UUID, UUID});
                                    }
                                    break;
                            }
                        }
                    } else if (!existsInExternal && existedAfterLastSync) {
                        // It was already deleted from the external table. Nothing to do here.
                    } else {
                        // It was never synced to the external table before being deleted. Nothing to do here.
                    }
                    break;
            }

            // Now that we've synced any internal changes to the external table, we can sync any external changes
            // to the internal table. Because we've already
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (values[0] instanceof ConflictDescriptor) {
            ConflictDescriptor cd = (ConflictDescriptor) values[0];
            resolver.requestConflictResolution(cd.originalRecord, cd.originalRequestedOperation, cd.incomingRecord, cd.incomingRequestedOperation);
        } else if (values[0] instanceof ProgressUpdateInfo) {
            if(callbacks != null) {
                callbacks.onProgressUpdate(TaskType.TASK_SYNCHRONIZE_DATABASES, (ProgressUpdateInfo) values[0]);
            }
        }
    }

    // Uses whatever position the cursor is currently at
    private static ContentValues getContentValuesForCursorRow(Cursor c) {
        ContentValues cv = new ContentValues();

        for (int i = 0; i < c.getColumnCount(); i++) {
            switch (c.getType(i)) {
                case Cursor.FIELD_TYPE_FLOAT:
                    cv.put(c.getColumnName(i), c.getFloat(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    cv.put(c.getColumnName(i), c.getInt(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    cv.put(c.getColumnName(i), c.getString(i));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    cv.put(c.getColumnName(i), c.getBlob(i));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    cv.putNull(c.getColumnName(i));
                    break;
            }
        }

        return cv;
    }
}