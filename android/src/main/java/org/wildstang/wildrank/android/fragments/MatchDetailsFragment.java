package org.wildstang.wildrank.android.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.widget.ImageView;
import android.widget.TextView;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.activities.ScoutMatchActivity;
import org.wildstang.wildrank.android.competitionmodels.CompetitionMatch;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.TeamPictureData;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.utils.ImageTools;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.File;
import java.util.HashMap;

public class MatchDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	String matchKey;
	int teamNumber;
	String allianceColor;
	boolean rescouting;

	TextView matchNumberView;
	TextView teamNumberView;
	ImageView imageView;
	TextView tabletID;

	OnSharedPreferenceChangeListener listener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		matchKey = args.getString("matchKey");

		listener = new OnSharedPreferenceChangeListener() {
			// If the team we are configured to scout for changes, reload the
			// data
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("configuredTeam")) {
					if (MatchDetailsFragment.this.isAdded()) {
						getLoaderManager().restartLoader(0, null, MatchDetailsFragment.this);
					}
				}

			}
		};

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_match_details, container, false);
		matchNumberView = (TextView) view.findViewById(R.id.match_number);
		teamNumberView = (TextView) view.findViewById(R.id.team_number);
		tabletID = (TextView) view.findViewById(R.id.tablet_id);
		((Button) view.findViewById(R.id.button_begin_scouting)).setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "match/" + matchKey), DatabaseContract.Match.ALL_COLUMNS, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cursor.moveToFirst();
		matchNumberView = (TextView) getView().findViewById(R.id.match_number);
		matchNumberView.setText("" + CompetitionMatch.matchNumberFromMatchKey(matchKey));
		HashMap<String, String> teamNumberStrings = new HashMap<String, String>();

		teamNumberStrings.put("red_1", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_1)));
		teamNumberStrings.put("red_2", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_2)));
		teamNumberStrings.put("red_3", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_3)));
		teamNumberStrings.put("blue_1", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_1)));
		teamNumberStrings.put("blue_2", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_2)));
		teamNumberStrings.put("blue_3", cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_3)));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String configuredTeamString = prefs.getString(Keys.CONFIGURED_TEAM, null);
		tabletID.setText(configuredTeamString);
		teamNumber = Integer.valueOf(teamNumberStrings.get(configuredTeamString));
		teamNumberView.setText(String.valueOf(teamNumber));
		if (configuredTeamString.contains("red")) {
			allianceColor = Keys.ALLIANCE_COLOR_RED;
			teamNumberView.setTextColor(getResources().getColor(R.color.red));
		} else {
			allianceColor = Keys.ALLIANCE_COLOR_BLUE;
			teamNumberView.setTextColor(getResources().getColor(R.color.blue));
		}
		teamNumberStrings.remove(configuredTeamString);

		int matchNumber = CompetitionMatch.matchNumberFromMatchKey(matchKey);
		if (DataManager.isMatchScouted(getActivity(), matchNumber, teamNumber)) {
			((Button) getView().findViewById(R.id.button_begin_scouting)).setText(R.string.button_rescout_match);
			rescouting = true;
		} else {
			((Button) getView().findViewById(R.id.button_begin_scouting)).setText(R.string.button_scout_match);
			rescouting = false;
		}
		
		// Add team image
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
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button_begin_scouting) {
			if (rescouting == false) {
				promptForScouterName();
			} else {
				displayRescoutWarning();
			}
		}
	}

	private void displayRescoutWarning() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		// set title
		alertDialogBuilder.setTitle("Rescouting");

		// set dialog message
		alertDialogBuilder.setMessage("You are about to rescout this match. Any previously recorded data will be permanently overwritten.").setCancelable(false)
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						promptForScouterName();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private void promptForScouterName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Confirm Scouter Name");
		alert.setMessage("Please confirm that this is your first and last name. If not, please change it.");

		// Set an EditText view to get user input
		final EditText input = new EditText(getActivity());
		input.setSingleLine(true);
		input.append(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Keys.SCOUTER_NAME, "Joshua Gustavson"));
		alert.setView(input);

		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String name = input.getText().toString();
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				prefs.edit().putString(Keys.SCOUTER_NAME, name).apply();
				// Launch a scouting activity
				Intent i = new Intent(getActivity(), ScoutMatchActivity.class);
				i.putExtra(Keys.MATCH_KEY, matchKey);
				i.putExtra(Keys.TEAM_NUMBER, teamNumber);
				i.putExtra(Keys.ALLIANCE_COLOR, allianceColor);
				getActivity().startActivityForResult(i, MatchScoutingMainFragment.MATCH_SCOUTING_FINISHED);
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		alert.show();
	}

}
