package org.wildstang.wildrank.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.MatchData;
import org.wildstang.wildrank.android.interfaces.IDataViewHost;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;

import java.util.ArrayList;

/*
 * Background task to load the event details and the list of matches from the flash drive.
 */
public class TaskLoadTeamData extends AsyncTask<Integer, ProgressUpdateInfo, ArrayList<JSONObject>> {

    private Context context;
    private IDataViewHost callbacks;

    public void setContext(Context c) {
        context = c;
    }

    public void setCallbacks(IDataViewHost callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(Integer... params) {
        ProgressUpdateInfo update = new ProgressUpdateInfo();
        update.state = ProgressUpdaterState.IN_PROGRESS;
        update.message = "Loading event details";
        publishProgress(update);

        int teamNumber = params[0];

        ArrayList<MatchData> matches = DataManager.getAllMatchResultsForTeam(context, teamNumber);
        Log.d("doInBackground", "matches list length: " + matches.size());
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        for (MatchData match : matches) {
            try {
                JSONObject matchJSON = new JSONObject(match.getContent());
                JSONObject scoringJSON = matchJSON.getJSONObject("scoring");
                jsonObjects.add(scoringJSON);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        update.state = ProgressUpdaterState.COMPLETE;
        publishProgress(update);
        return jsonObjects;
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> result) {
        super.onPostExecute(result);
        callbacks.setData(result);
    }

}
