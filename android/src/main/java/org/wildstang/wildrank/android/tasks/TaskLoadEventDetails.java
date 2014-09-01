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
import org.wildstang.wildrank.android.utils.MatchUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Locale;

/*
 * Background task to load the event details and the list of matches from the flash drive.
 */
public class TaskLoadEventDetails extends GenericTaskWithContext {

    public TaskLoadEventDetails() {
        super(TaskType.TASK_LOAD_EVENT_DETAILS);
    }

    @Override
    protected Void doInBackground(Void... params) {
        ProgressUpdateInfo update = new ProgressUpdateInfo();
        update.state = ProgressUpdaterState.IN_PROGRESS;
        update.message = "Loading event details";
        publishProgress(update);

        // Delete any event data that's currently in the database
        context.getContentResolver().delete(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/"), null, null);

        String filePath = DataManager.getDirectory(DataManager.DIRECTORY_SYNCED, context).getAbsolutePath() + "/event/event.json";
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File(filePath)), 8);
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
            JSONObject rootObject = new JSONObject(json.toString());

            // Used for converting Strings to Dates and vice versa
            SimpleDateFormat incomingFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.Event.KEY, rootObject.getString("key"));
            String eventKey = rootObject.getString("key");
            cv.put(DatabaseContract.Event.NAME, rootObject.getString("name"));
            cv.put(DatabaseContract.Event.SHORT_NAME, rootObject.getString("short_name"));
            cv.put(DatabaseContract.Event.LOCATION, rootObject.getString("location"));
            String startDate = rootObject.getString("start_date");
            String endDate = rootObject.getString("end_date");
            cv.put(DatabaseContract.Event.START_DATE, startDate);
            cv.put(DatabaseContract.Event.END_DATE, endDate);
            context.getContentResolver().insert(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/" + cv.getAsString(DatabaseContract.Event.KEY)), cv);
            JSONArray teamsArray = rootObject.getJSONArray("matches");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean downloadOnlyQM = prefs.getBoolean(Keys.DOWNLOAD_QM_ONLY, false);
            JSONObject alliances, blue, red;
            JSONArray blueTeams, redTeams;
            String key;
            for (int i = 0; i < teamsArray.length(); i++) {
                JSONObject jsonMatch = teamsArray.getJSONObject(i);
                key = jsonMatch.getString("key");
                if (downloadOnlyQM) {
                    if (!key.contains("qm")) {
                        continue;
                    }
                }
                alliances = jsonMatch.getJSONObject("alliances");
                blue = alliances.getJSONObject("blue");
                blueTeams = blue.getJSONArray("teams");
                int[] redAlliance = new int[3];
                int[] blueAlliance = new int[3];
                for (int j = 0; j < blueTeams.length(); j++) {
                    blueAlliance[j] = Integer.parseInt(blueTeams.getString(j).substring(3));
                }
                red = alliances.getJSONObject("red");
                redTeams = red.getJSONArray("teams");
                for (int j = 0; j < redTeams.length(); j++) {
                    redAlliance[j] = Integer.parseInt(redTeams.getString(j).substring(3));
                }
                cv = new ContentValues();
                cv.put(DatabaseContract.Match.KEY, key);
                cv.put(DatabaseContract.Match.NUMBER, MatchUtils.matchNumberFromMatchKey(key));
                cv.put(DatabaseContract.Match.RED_1, redAlliance[0]);
                cv.put(DatabaseContract.Match.RED_2, redAlliance[1]);
                cv.put(DatabaseContract.Match.RED_3, redAlliance[2]);
                cv.put(DatabaseContract.Match.BLUE_1, blueAlliance[0]);
                cv.put(DatabaseContract.Match.BLUE_2, blueAlliance[1]);
                cv.put(DatabaseContract.Match.BLUE_3, blueAlliance[2]);
                context.getContentResolver().insert(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "match/" + cv.getAsString(DatabaseContract.Match.KEY)), cv);
            }
            // Store current event in preferences
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString(Keys.CONFIGURED_EVENT, eventKey).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        update.state = ProgressUpdaterState.COMPLETE;
        publishProgress(update);
        return null;
    }

}
