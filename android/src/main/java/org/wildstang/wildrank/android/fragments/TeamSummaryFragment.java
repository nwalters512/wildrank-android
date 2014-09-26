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
import android.widget.ImageView;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.NotesData;
import org.wildstang.wildrank.android.data.TeamPictureData;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.utils.ImageTools;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.File;

public class TeamSummaryFragment extends Fragment implements LoaderCallbacks<Cursor> {

    long teamID;
    private int teamNumber;
    private String teamName;

    private TextView teamNumberView;
    private TextView teamNameView;
    private TextView notesView;
    private ImageView imageView;

    private PitSummaryFragment pitSummaryFragment;

    private static String SCOUTED_BY = "Scouted by: ";
    private static String WEIGHT = "Weight: ";
    private static String DIMENSIONS = "Dimensions (l*w*h): ";
    private static String DRIVETRAIN = "Drivetrain: ";
    private static String CATCHER = "Catcher: ";
    private static String ACCUMULATOR = "Accumulator: ";
    private static String RECEIVE_FROM_HP = "Receive from HP: ";
    private static String SHOOTER = "Shooter: ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "Fragment created!");
        teamID = getArguments().getLong(Keys.TEAM_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_summary, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFragmentManager().beginTransaction().replace(R.id.pit_summary_container, PitSummaryFragment.newInstance(teamID)).commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("onCreateLoader", "loader crated with team id: " + teamID);
        return new CursorLoader(getActivity(), Uri.withAppendedPath(Uri.parse("DatabaseContentProvider.CONTENT_URI"), "team/" + teamID), DatabaseContract.Team.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        //Log.d("onLoadFinished", "loaded team id: " + cursor.getLong(cursor.getColumnIndex(DatabaseContract.Team._ID)));
        //teamNumber = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.NUMBER));
        teamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.NAME));
        teamNumberView = (TextView) getView().findViewById(R.id.team_number);
        teamNumberView.setText("" + teamNumber);
        teamNameView = (TextView) getView().findViewById(R.id.team_name);
        teamNameView.setText(teamName);

        notesView = (TextView) getView().findViewById(R.id.team_notes);

        imageView = (ImageView) getView().findViewById(R.id.team_picture);
        TeamPictureData picture = new TeamPictureData(teamNumber);
        try {
            File image = DataManager.getDataFileFromDirectory(picture, getActivity(), DataManager.DIRECTORY_FIRST_FOUND);
            if (image != null) {
                imageView.setImageBitmap(ImageTools.decodeSampledBitmapFromFile(image, 100, 300));
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NotesData notesData = new NotesData();
        notesData.setTeamNumber(teamNumber);
        try {
            if (DataManager.loadDataIfExists(notesData, getActivity(), DataManager.DIRECTORY_SYNCED)) {
                notesView.setText(notesData.getContent().trim());
            } else {
                notesView.setText("No available notes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        PitData pitData = new PitData();
		pitData.setTeamNumber(teamNumber);
		String fileString = null;
		try {
			if (DataManager.loadDataIfExists(pitData, getActivity(), DataManager.DIRECTORY_FIRST_FOUND)) {
				fileString = pitData.getContent();
			}
			if (fileString != null) {
				JSONObject jsonPit = new JSONObject(fileString);
				JSONObject scoring = jsonPit.getJSONObject("scoring");
				StringBuilder pitNote = new StringBuilder();
				pitNote.append(SCOUTED_BY + jsonPit.getString("scouter_id") + "\n");
				pitNote.append(WEIGHT + Integer.toString(scoring.getInt("robot_weight")) + "\n");
				pitNote.append(DIMENSIONS + Integer.toString(scoring.getInt("robot_length")) + " x " + Integer.toString(scoring.getInt("robot_width")) + " x "
						+ Integer.toString(scoring.getInt("robot_height")) + "\n");
				pitNote.append(DRIVETRAIN + scoring.getString("drivetrain") + "\n");
				pitNote.append(SHOOTER + scoring.get("shooter") + "\n");
				pitNote.append(CATCHER + (scoring.getBoolean("catcher") == true ? "yes" : "no") + "\n");
				pitNote.append(ACCUMULATOR + (scoring.getBoolean("accumulator") == true ? "yes" : "no") + "\n");
				pitNote.append(RECEIVE_FROM_HP + (scoring.getBoolean("receive_from_hp") == true ? "yes" : "no") + "\n");
				// Apply some nice formatting to our string
				String pitNoteString = pitNote.toString();
				SpannableStringBuilder formatted = new SpannableStringBuilder(pitNoteString);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(SCOUTED_BY), pitNoteString.indexOf(SCOUTED_BY) + SCOUTED_BY.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(WEIGHT), pitNoteString.indexOf(WEIGHT) + WEIGHT.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(DIMENSIONS), pitNoteString.indexOf(DIMENSIONS) + DIMENSIONS.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(DRIVETRAIN), pitNoteString.indexOf(DRIVETRAIN) + DRIVETRAIN.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(SHOOTER), pitNoteString.indexOf(SHOOTER) + SHOOTER.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(CATCHER), pitNoteString.indexOf(CATCHER) + CATCHER.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(ACCUMULATOR), pitNoteString.indexOf(ACCUMULATOR) + ACCUMULATOR.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				formatted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pitNoteString.indexOf(RECEIVE_FROM_HP), pitNoteString.indexOf(RECEIVE_FROM_HP) + RECEIVE_FROM_HP.length(),
						SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
				pitView.setText(formatted, BufferType.SPANNABLE);
			} else {
				pitView.setText("No available pit data");
			}
		} catch (Exception e) {
			e.printStackTrace();
			pitView.setText("Error reading data!\n" + e.toString());
		}*/
        // Destroy the loader
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub

    }

}
