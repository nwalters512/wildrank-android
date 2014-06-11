package org.wildstang.wildrank.android.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.activities.NoteActivity;
import org.wildstang.wildrank.android.activities.ScoutPitActivity;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.utils.Keys;

public class TeamDetailsFragment extends Fragment implements LoaderCallbacks<Cursor>, OnClickListener {

    long teamID;
    int teamNumber;
    String teamName;
    boolean rescouting;

    TextView teamNumberView;
    TextView teamNameView;

    public static String DETAILS_MODE = "detailsMode";
    public static int MODE_PIT = 1;
    public static int MODE_NOTES = 2;
    private int mode;

    public static TeamDetailsFragment newInstance(long teamID, int mode) {
        TeamDetailsFragment fragment = new TeamDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(Keys.TEAM_ID, teamID);
        args.putInt(TeamDetailsFragment.DETAILS_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (!args.containsKey(Keys.TEAM_ID) && !args.containsKey(DETAILS_MODE)) {
            throw new IllegalArgumentException("TeamDetailsFragment must be created with a team ID and a details mode!");
        }
        teamID = args.getLong(Keys.TEAM_ID);
        mode = args.getInt(DETAILS_MODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_details, container, false);
        getLoaderManager().initLoader(0, null, this);
        view.findViewById(R.id.button_begin_scouting).setOnClickListener(this);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + teamID), DatabaseContract.Team.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If this fragment has been removed from the manager once loading is finished, don't load anything
        if (getView() == null) {
            return;
        }
        cursor.moveToFirst();
        teamNumberView = (TextView) getView().findViewById(R.id.team_number);
        teamNumberView.setText("" + cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.NUMBER)));
        teamNameView = (TextView) getView().findViewById(R.id.team_name);
        teamNameView.setText("" + cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.NAME)));
        teamNumber = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.NUMBER));
        teamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.NAME));

        if (mode == MODE_PIT) {
            if (DataManager.isTeamPitScouted(getActivity(), teamNumber)) {
                ((Button) getView().findViewById(R.id.button_begin_scouting)).setText(R.string.button_rescout_team);
                rescouting = true;
            } else {
                ((Button) getView().findViewById(R.id.button_begin_scouting)).setText(R.string.button_scout_team);
                rescouting = false;
            }
        } else {
            ((Button) getView().findViewById(R.id.button_begin_scouting)).setText(R.string.button_take_notes);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_begin_scouting) {
            if (mode == MODE_PIT) {
                promptForScouterName();
            } else {
                takeNotes();
            }
        }
    }

    private void takeNotes() {
        // Launch a notes activity
        Intent i = new Intent(getActivity(), NoteActivity.class);
        i.putExtra(Keys.TEAM_NUMBER, teamNumber);
        i.putExtra(Keys.TEAM_NAME, teamName);
        i.putExtra(Keys.TEAM_ID, teamID);
        getActivity().startActivity(i);
    }

    private void promptForScouterName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Confirm Scouter Name");
        alert.setMessage("Please confirm that this is your first and last name. If not, please change it.");

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        input.setSingleLine(true);
        input.append(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Keys.SCOUTER_NAME, "Joshua Gustafson"));
        alert.setView(input);

        alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = input.getText().toString();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putString(Keys.SCOUTER_NAME, name).apply();
                // Launch a scouting activity
                Intent i = new Intent(getActivity(), ScoutPitActivity.class);
                i.putExtra(Keys.TEAM_NUMBER, teamNumber);
                i.putExtra(Keys.TEAM_NAME, teamName);
                i.putExtra(Keys.TEAM_ID, teamID);
                getActivity().startActivity(i);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(0);
    }

}
