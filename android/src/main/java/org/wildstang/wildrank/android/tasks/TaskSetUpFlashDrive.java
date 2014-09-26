package org.wildstang.wildrank.android.tasks;


import android.database.sqlite.SQLiteDatabase;

import org.wildstang.wildrank.android.database.DatabaseHelper;
import org.wildstang.wildrank.android.database.ExternalStorageDatabaseContext;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;
import org.wildstang.wildrank.android.utils.Constants;

import java.io.File;

/*
 * BAckground task to configure the flash drive
 */
public class TaskSetUpFlashDrive extends GenericTaskWithContext {

    public TaskSetUpFlashDrive() {
        super(TaskType.TASK_LOAD_TEAM_LIST);
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExternalStorageDatabaseContext databaseContext = new ExternalStorageDatabaseContext(new File(Constants.USB_FLASH_DRIVE_ROOT_PATH), context);
        SQLiteDatabase db = databaseContext.openOrCreateDatabase("wildrank.db", -1, null);

        db.beginTransaction();
        db.execSQL(DatabaseHelper.CREATE_TABLE_USERS);
        db.execSQL(DatabaseHelper.CREATE_TABLE_MATCHES);
        db.execSQL(DatabaseHelper.CREATE_TABLE_MATCH_RESULTS);
        db.execSQL(DatabaseHelper.CREATE_TABLE_PIT_SCOUTING);
        db.execSQL(DatabaseHelper.CREATE_TABLE_NOTES);
        db.execSQL(DatabaseHelper.CREATE_TABLE_TEAMS);
        db.execSQL(DatabaseHelper.CREATE_TABLE_PICK_LISTS);
        db.execSQL(DatabaseHelper.CREATE_TABLE_PICK_LIST_TEAMS);
        db.setTransactionSuccessful();
        db.endTransaction();

        ProgressUpdateInfo update = new ProgressUpdateInfo();
        update.state = ProgressUpdaterState.COMPLETE;
        publishProgress(update);
        return null;
    }

}
