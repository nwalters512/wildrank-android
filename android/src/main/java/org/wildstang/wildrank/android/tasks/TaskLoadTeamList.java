package org.wildstang.wildrank.android.tasks;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/*
 * BAckground task to load the list of teams for this event from the flash drive.
 */
public class TaskLoadTeamList extends GenericTaskWithContext {

    public TaskLoadTeamList() {
        super(TaskType.TASK_LOAD_TEAM_LIST);
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Delete any team data that's currently in the database
        context.getContentResolver().delete(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/"), null, null);
        ProgressUpdateInfo update = new ProgressUpdateInfo();
        update.state = ProgressUpdaterState.IN_PROGRESS;
        update.message = "Loading team list";
        publishProgress(update);
        String filePath = DataManager.getDirectory(DataManager.DIRECTORY_SYNCED, context).getAbsolutePath() + "/event/pit.json";
        BufferedReader br;

        int maxPitGroupNumber = 0;
        try {
            br = new BufferedReader(new FileReader(new File(filePath)), 8);
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
            JSONObject rootObject = new JSONObject(json.toString());
            JSONArray teamsArray = rootObject.getJSONArray("team");
            for (int i = 0; i < teamsArray.length(); i++) {
                JSONObject currentObject = teamsArray.getJSONObject(i);
                String teamName = currentObject.getString("name");
                int teamNumber = currentObject.getInt("number");
                int pitGroup = currentObject.getInt("pit_group");
                if (pitGroup > maxPitGroupNumber) {
                    maxPitGroupNumber = pitGroup;
                }
                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.Team.NUMBER, teamNumber);
                cv.put(DatabaseContract.Team.NAME, teamName);
                cv.put(DatabaseContract.Team.PIT_GROUP, pitGroup);
                context.getContentResolver().insert(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(Keys.NUM_PIT_GROUPS, "" + (maxPitGroupNumber + 1)).commit();
        update.state = ProgressUpdaterState.COMPLETE;
        publishProgress(update);
        return null;
    }

}
