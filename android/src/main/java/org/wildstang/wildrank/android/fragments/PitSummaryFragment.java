package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.PitData;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.interfaces.ITemplatedTextView;
import org.wildstang.wildrank.android.utils.Keys;

public class PitSummaryFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private int teamNumber;
    private long teamID = -1;

    public static PitSummaryFragment newInstance(long teamID) {
        PitSummaryFragment fragment = new PitSummaryFragment();
        Bundle args = new Bundle();
        args.putLong(Keys.TEAM_ID, teamID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teamID = getArguments().getLong(Keys.TEAM_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pit_summary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (teamID != -1) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public void setTeamID(long teamID) {
        this.teamID = teamID;
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initializeViews(ViewGroup v, JSONObject json) {
        if (v == null) {
            v = (ViewGroup) getView();
            if (v == null) {
                return;
            }
        }
        int childCount = v.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof ITemplatedTextView) {
                ((ITemplatedTextView) view).populateFromData(json);
            } else if (view instanceof ViewGroup) {
                initializeViews((ViewGroup) view, json);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + teamID), DatabaseContract.Team.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        teamNumber = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.NUMBER));
        getLoaderManager().destroyLoader(0);

        try {
            // Load the pit scouting data
            String jsonString = null;
            PitData pitData = new PitData();
            pitData.setTeamNumber(teamNumber);
            if (DataManager.loadDataIfExists(pitData, getActivity(), DataManager.DIRECTORY_FIRST_FOUND)) {
                jsonString = pitData.getContent();
            }
            if (jsonString != null) {
                JSONObject json = new JSONObject(jsonString);
                initializeViews((ViewGroup) getView(), json);
            } else {
                ((ViewGroup) getView()).removeAllViews();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PitSummaryFragment", "Error loading pit data");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

}
