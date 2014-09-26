package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;
import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.interfaces.IDataView;
import org.wildstang.wildrank.android.interfaces.IDataViewHost;
import org.wildstang.wildrank.android.tasks.TaskLoadTeamData;
import org.wildstang.wildrank.android.utils.Keys;

import java.util.ArrayList;
import java.util.List;

public class TeamDataFragment extends Fragment implements IDataViewHost, LoaderCallbacks<Cursor> {

    private int teamNumber;
    private long teamID;

    private List<JSONObject> data = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teamID = getArguments().getLong(Keys.TEAM_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_data, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initializeViews(ViewGroup v) {
        if (v == null) {
            v = (ViewGroup) getView();
            if (v == null) {
                return;
            }
        }
        int childCount = v.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof IDataView) {
                ((IDataView) view).populateFromData(data);
            } else if (view instanceof ViewGroup) {
                initializeViews((ViewGroup) view);
            }
        }
    }

    @Override
    public void setData(List<JSONObject> data) {
        this.data = data;
        initializeViews(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + teamID), DatabaseContract.Team.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        //teamNumber = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.NUMBER));
        ((TextView) getView().findViewById(R.id.team_number)).setText("Team " + teamNumber);
        TaskLoadTeamData task = new TaskLoadTeamData();
        task.setContext(getActivity());
        task.setCallbacks(this);
        task.execute(teamNumber);
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

}
