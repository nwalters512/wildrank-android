package org.wildstang.wildrank.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database info
    public static final String DATABASE_NAME = "wildrank.db";
    public static final int DATABASE_VERSION = 16;

    public static final String CREATE_TABLE_USERS = "create table " + DatabaseContract.User.TABLE_NAME + "("
            + DatabaseContract.User.UUID + " text primary key not null, "
            + DatabaseContract.User.HASH + " text not null, "
            + DatabaseContract.User.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.User.USER_ID + " integer not null, "
            + DatabaseContract.User.USER_NAME + " text not null);";

    public static final String CREATE_TABLE_MATCHES = "create table " + DatabaseContract.Match.TABLE_NAME + "("
            + DatabaseContract.Match.UUID + " text primary key not null, "
            + DatabaseContract.Match.HASH + " text not null, "
            + DatabaseContract.Match.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.Match.MATCH_KEY + " text not null, "
            + DatabaseContract.Match.MATCH_NUMBER + " integer not null, "
            + DatabaseContract.Match.RED_1 + " text not null, "
            + DatabaseContract.Match.RED_2 + " text not null, "
            + DatabaseContract.Match.RED_3 + " text not null, "
            + DatabaseContract.Match.BLUE_1 + " text not null, "
            + DatabaseContract.Match.BLUE_2 + " text not null, "
            + DatabaseContract.Match.BLUE_3 + " text not null);";

    public static final String CREATE_TABLE_MATCH_RESULTS = "create table " + DatabaseContract.MatchResult.TABLE_NAME + "("
            + DatabaseContract.MatchResult.UUID + " text primary key not null, "
            + DatabaseContract.MatchResult.HASH + " text not null, "
            + DatabaseContract.MatchResult.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.MatchResult.MATCH_KEY + " text not null, "
            + DatabaseContract.MatchResult.TEAM_KEY + " text not null, "
            + DatabaseContract.MatchResult.USER_IDS + " text not null, "
            + DatabaseContract.MatchResult.DATA + " text not null);";

    public static final String CREATE_TABLE_PIT_SCOUTING = "create table " + DatabaseContract.PitScouting.TABLE_NAME + "("
            + DatabaseContract.PitScouting.UUID + " text primary key not null, "
            + DatabaseContract.PitScouting.HASH + " text not null, "
            + DatabaseContract.PitScouting.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.PitScouting.TEAM_KEY + " text not null, "
            + DatabaseContract.PitScouting.USER_IDS + " text not null, "
            + DatabaseContract.PitScouting.DATA + " text not null);";

    public static final String CREATE_TABLE_NOTES = "create table " + DatabaseContract.Note.TABLE_NAME + "("
            + DatabaseContract.Note.UUID + " text primary key not null, "
            + DatabaseContract.Note.HASH + " text not null, "
            + DatabaseContract.Note.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.Note.MATCH_UUID + " text, "
            + DatabaseContract.Note.MATCH_KEY + " text, "
            + DatabaseContract.Note.PIT_SCOUTING_UUID + " text, "
            + DatabaseContract.Note.PIT_SCOUTING_KEY + " text, "
            + DatabaseContract.Note.USER_IDS + " text not null, "
            + DatabaseContract.Note.DATA + "text not null);";

    public static final String CREATE_TABLE_TEAMS = "create table " + DatabaseContract.Team.TABLE_NAME + "("
            + DatabaseContract.Team.UUID + " text primary key not null, "
            + DatabaseContract.Team.HASH + " text not null, "
            + DatabaseContract.Team.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.Team.TEAM_KEY + " text not null, "
            + DatabaseContract.Team.NAME + " text not null, "
            + DatabaseContract.Team.PIT_GROUP + " integer not null);";

    public static final String CREATE_TABLE_PICK_LISTS = "create table " + DatabaseContract.PickList.TABLE_NAME + "("
            + DatabaseContract.PickList.UUID + " text primary key not null, "
            + DatabaseContract.PickList.HASH + " text not null, "
            + DatabaseContract.PickList.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.PickList.NAME + " text not null, " +
            DatabaseContract.PickList.USER_IDS + " text not null);";

    public static final String CREATE_TABLE_PICK_LIST_TEAMS = "create table " + DatabaseContract.PickListTeam.TABLE_NAME + "("
            + DatabaseContract.PickListTeam.UUID + " text primary key not null, "
            + DatabaseContract.PickListTeam.HASH + " text not null, "
            + DatabaseContract.PickListTeam.LAST_MODIFIED + " integer not null, "
            + DatabaseContract.PickListTeam.LIST_UUID + " text not null, "
            + DatabaseContract.PickListTeam.TEAM_KEY + " text not null, "
            + DatabaseContract.PickListTeam.RANKING + " integer not null, "
            + DatabaseContract.PickListTeam.TIER + " integer not null, "
            + DatabaseContract.PickListTeam.PICKED + " integer not null);";

    public static final String CREATE_TABLE_TRACK_CHANGES = "create table " + DatabaseContract.TrackChanges.TABLE_NAME + "("
            + DatabaseContract.TrackChanges.UUID + " text primary key not null, "
            + DatabaseContract.TrackChanges.RECORD_TABLE + " text not null, "
            + DatabaseContract.TrackChanges.OPERATION + " text not null);";

    public static final String CREATE_TABLE_EXTERNAL_STATE = "create table " + DatabaseContract.ExternalState.TABLE_NAME + "("
            + DatabaseContract.ExternalState.UUID + " text primary key not null, "
            + DatabaseContract.ExternalState.HASH + " text not null);";

    private PickList pickListTable;
    private Matches matchesTable;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        pickListTable = new PickList();
        matchesTable = new Matches();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_USERS);
        database.execSQL(CREATE_TABLE_MATCHES);
        database.execSQL(CREATE_TABLE_MATCH_RESULTS);
        database.execSQL(CREATE_TABLE_PIT_SCOUTING);
        database.execSQL(CREATE_TABLE_NOTES);
        database.execSQL(CREATE_TABLE_TEAMS);
        database.execSQL(CREATE_TABLE_PICK_LISTS);
        database.execSQL(CREATE_TABLE_PICK_LIST_TEAMS);
        database.execSQL(CREATE_TABLE_TRACK_CHANGES);
        database.execSQL(CREATE_TABLE_EXTERNAL_STATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Nothing to do here yet
    }

    public PickList getPickListTable() {return pickListTable;}
    public Matches getMatchesTable() {return matchesTable;}

    public class PickList {

        /**
         * Returns the UUID of a pick list created with the specified name and user IDs.
         *
         * @param pickListName
         * @param userIDs
         * @return
         */
        public String createNewPickList(String pickListName, int[] userIDs) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.PickList.NAME, pickListName);
            JSONArray array = new JSONArray();
            for (int i = 0; i < userIDs.length; i++) {
                array.put(userIDs[i]);
            }
            cv.put(DatabaseContract.PickList.USER_IDS, array.toString());
            String uuid = Utils.createNewUUID();
            cv.put(DatabaseContract.PickList.UUID, uuid);
            cv.put(DatabaseContract.PickList.LAST_MODIFIED, System.currentTimeMillis() / 1000L);
            cv.put(DatabaseContract.PickList.HASH, Utils.computeHashForContentValues(cv));
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            db.insert(DatabaseContract.PickList.TABLE_NAME, null, cv);
            ChangesTracker.markRecordCreated(uuid, db);
            db.setTransactionSuccessful();
            db.endTransaction();

            return uuid;
        }

        public void deletePickList(String uuid) {
            // Delete both the Pick List itself and all the Pick List Teams associated with this pick list.
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            db.delete(DatabaseContract.PickList.TABLE_NAME, DatabaseContract.PickList.UUID + "=?", new String[]{uuid});
            db.delete(DatabaseContract.PickListTeam.TABLE_NAME, DatabaseContract.PickListTeam.LIST_UUID + "=?", new String[]{uuid});
            db.setTransactionSuccessful();
        }

        /**
         * Updates the ranking value for each of the given teams.
         *
         * @param pickListUUID the UUID of the pick list whose teams we should update
         * @param teams        a HashMap with team keys (Strings) as the keys and their ranks (int) as the values
         */
        public void updatePickListRankings(String pickListUUID, HashMap<String, Integer> teams) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            for (Map.Entry<String, Integer> entry : teams.entrySet()) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.PickListTeam.RANKING, entry.getValue());
                db.update(DatabaseContract.PickListTeam.TABLE_NAME, cv, DatabaseContract.PickListTeam.TEAM_KEY + "=? AND " + DatabaseContract.PickListTeam.LIST_UUID + "=?", new String[]{entry.getKey().toString(), pickListUUID});
                // We need to mark this record as updated. Retreive the UUID of the updated row
                Cursor c = db.query(DatabaseContract.PickListTeam.TABLE_NAME, new String[]{DatabaseContract.PickListTeam.UUID}, DatabaseContract.PickListTeam.TEAM_KEY + "=? AND " + DatabaseContract.PickListTeam.LIST_UUID + "=?", new String[]{entry.getKey().toString(), pickListUUID}, null, null, null);
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    ChangesTracker.markRecordUpdated(c.getString(c.getColumnIndex(DatabaseContract.PickListTeam.UUID)), db);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public class Matches {
        public int getMatchesCount() {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM ? AS count", new String[] {DatabaseContract.Match.TABLE_NAME});
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    private static class ChangesTracker {
        public static void markRecordCreated(String uuid, SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.TrackChanges.OPERATION, DatabaseContract.TrackChanges.Operation.CREATE);
            int rowsModified = db.update(DatabaseContract.TrackChanges.TABLE_NAME, cv, DatabaseContract.TrackChanges.UUID + "=?", new String[]{uuid});
            if (rowsModified == 0) {
                // Update failed, we need to create the record instead
                cv.put(DatabaseContract.TrackChanges.UUID, uuid);
                db.insert(DatabaseContract.TrackChanges.TABLE_NAME, null, cv);
            }
        }

        public static void markRecordUpdated(String uuid, SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.TrackChanges.OPERATION, DatabaseContract.TrackChanges.Operation.UPDATE);
            int rowsModified = db.update(DatabaseContract.TrackChanges.TABLE_NAME, cv, DatabaseContract.TrackChanges.UUID + "=?", new String[]{uuid});
            if (rowsModified == 0) {
                // Update failed, we need to create the record instead
                cv.put(DatabaseContract.TrackChanges.UUID, uuid);
                db.insert(DatabaseContract.TrackChanges.TABLE_NAME, null, cv);
            }
        }

        public static void markRecordDeleted(String uuid, SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.TrackChanges.OPERATION, DatabaseContract.TrackChanges.Operation.DELETE);
            int rowsModified = db.update(DatabaseContract.TrackChanges.TABLE_NAME, cv, DatabaseContract.TrackChanges.UUID + "=?", new String[]{uuid});
            if (rowsModified == 0) {
                // Update failed, we need to create the record instead
                cv.put(DatabaseContract.TrackChanges.UUID, uuid);
                db.insert(DatabaseContract.TrackChanges.TABLE_NAME, null, cv);
            }
        }
    }

    private static class Utils {
        /**
         * Computes the MD5 hash of a ContentValues object. The hash is determined with the following rules:
         * <p/>
         * 1. Exclude the 3 columns common to all objects (HASH, UUID, and LAST_MODIFIED
         * 2. Sort the keys of the ContentValues alphabetically
         * 3. Concatenate the keys and values into a string with the following pattern: key1:value1;key2:value2
         * 4. Compute the MD5 hash of that string
         *
         * @param cv
         * @return
         */
        public static String computeHashForContentValues(ContentValues cv) {
            String stringToHash = "";

            ArrayList<String> sortedKeys = new ArrayList(asSortedList(cv.keySet()));
            List<String> commonColumns = Arrays.asList(DatabaseContract.BaseColumns.ALL_BASE_COLUMNS);
            Iterator<String> it = sortedKeys.iterator();
            while (it.hasNext()) {
                String currentKey = it.next();
                if (!commonColumns.contains(currentKey)) {
                    stringToHash = stringToHash.concat(currentKey + ":" + cv.get(currentKey) + (it.hasNext() ? ";" : ""));
                }
            }
            Log.d("DatabaseHelper", "Computed CV string: " + stringToHash);
            return DigestUtils.md5Hex(stringToHash);
        }

        public static String createNewUUID() {
            return UUID.randomUUID().toString();
        }

        private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
            List<T> list = new ArrayList<T>(c);
            java.util.Collections.sort(list);
            return list;
        }
    }
}
