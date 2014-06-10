package org.wildstang.wildrank.android.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment {

	private ProgressDialog progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		ListPreference lp = (ListPreference) findPreference("pitGroup");
		int numPitGroups = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Keys.NUM_PIT_GROUPS, "1"));
		String[] entries = new String[numPitGroups];
		String[] entryValues = new String[numPitGroups];
		for (int i = 0; i < numPitGroups; i++) {
			entries[i] = "Group " + (i + 1);
			entryValues[i] = "" + i;
		}
		lp.setEntries(entries);
		lp.setEntryValues(entryValues);

		// Launch a new ASyncTask when we want to import the pick list
		Preference importPickList = findPreference("importPickList");
		importPickList.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startImport();
				return true;
			}
		});

		// Launch a new wASyncTask when we want to export the pick list
		Preference exportPickList = findPreference("exportPickList");
		exportPickList.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startExport();
				return true;
			}
		});
	}

	private class ExportPickListTask extends AsyncTask<SettingsFragment, Void, Void> {

		private SettingsFragment f;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SettingsFragment.this.startLoading();
		}

		@Override
		protected Void doInBackground(SettingsFragment... params) {
			f = params[0];
			Cursor c = getActivity().getContentResolver().query(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), DatabaseContract.Team.ALL_COLUMNS, null, null,
					DatabaseContract.Team.NUMBER);
			JSONArray array = new JSONArray();
			c.moveToPosition(-1);
			while (c.moveToNext()) {
				JSONObject team = new JSONObject();
				try {
					team.put("team_number", c.getString(c.getColumnIndex(DatabaseContract.Team.NUMBER)));
					team.put("team_rank", c.getString(c.getColumnIndex(DatabaseContract.Team.PICK_LIST_RANKING)));
					team.put("team_tier", c.getString(c.getColumnIndex(DatabaseContract.Team.PICK_LIST_TIER)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				array.put(team);
			}

			// Write JSON to the appropriate file
			try {
				File destination = new File(DataManager.getDirectory(DataManager.DIRECTORY_FLASH_ROOT, f.getActivity()) + File.separator + "picklist" + File.separator + "picklist.json");
				destination.getParentFile().mkdirs();
				destination.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(destination));
				bw.write(array.toString());
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			SettingsFragment.this.stopLoading();
		}

	}

	private class ImportPickListTask extends AsyncTask<SettingsFragment, Void, Void> {

		private SettingsFragment f;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SettingsFragment.this.startLoading();
		}

		@Override
		protected Void doInBackground(SettingsFragment... params) {
			try {
				f = params[0];
				File source = new File(DataManager.getDirectory(DataManager.DIRECTORY_FLASH_ROOT, f.getActivity()) + File.separator + "picklist" + File.separator + "picklist.json");

				BufferedReader br = new BufferedReader(new FileReader(source), 8);
				StringBuilder json = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					json.append(line);
				}
				br.close();
				JSONArray array = new JSONArray(json.toString());

				// Map team numbers to team IDs
				HashMap<Integer, Long> teamMap = new HashMap<Integer, Long>();
				Cursor c = getActivity().getContentResolver().query(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), DatabaseContract.Team.ALL_COLUMNS, null, null,
						DatabaseContract.Team.NUMBER);
				c.moveToPosition(-1);
				while (c.moveToNext()) {
					Long teamId = c.getLong(c.getColumnIndex(DatabaseContract.Team._ID));
					Integer teamNum = c.getInt(c.getColumnIndex(DatabaseContract.Team.NUMBER));
					teamMap.put(teamNum, teamId);
				}

				// Map ContentValues to team IDs
				HashMap<Long, ContentValues> valuesMap = new HashMap<Long, ContentValues>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject currentTeam = array.getJSONObject(i);
					int teamNumber = currentTeam.getInt("team_number");
					Long teamId = teamMap.get(teamNumber);
					if (teamId == null) {
						// team is missing from local database, handle this later
					}
					ContentValues cv = new ContentValues();
					cv.put(DatabaseContract.Team.PICK_LIST_RANKING, currentTeam.getInt("team_rank"));
					int teamTier = 0;
					if (currentTeam.has("team_tier")) {
						teamTier = currentTeam.getInt("team_tier");
					}
					cv.put(DatabaseContract.Team.PICK_LIST_TIER, teamTier);
					valuesMap.put(teamId, cv);
				}

				// Iterate over that map and update the relevant entries in the database
				for (Map.Entry<Long, ContentValues> entry : valuesMap.entrySet()) {
					Long teamId = entry.getKey();
					ContentValues cv = entry.getValue();
					getActivity().getContentResolver().update(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + teamId), cv, null, null);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			SettingsFragment.this.stopLoading();
		}

	}

	/*
	 * Displays a warning if the flash drive is not connected and prompts the
	 * user to connect one
	 */
	private void displayFlashDriveWarning(final int execute) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		// Set title
		alertDialogBuilder.setTitle("Connect a Flash Drive");

		// Set dialog message
		alertDialogBuilder
				.setMessage(
						"Please connect a flash drive and try again.\n\nIf the flash drive is already conntected and you still can't sync, you may have to manually remount. Click \"Open Settings\" below, scroll to the bottom, and unmount and remount the USB storage device. Then press the back button.")
				.setCancelable(false).setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (execute == 1) {
							startImport();
						} else if (execute == 2) {
							startExport();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setNeutralButton("Open settings", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
					}
				});
		// Create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// Show it
		alertDialog.show();
	}

	public void startLoading() {
		if (progress == null) {
			progress = new ProgressDialog(getActivity());
		}
		progress.setIndeterminate(true);
		progress.setTitle("Working");
		progress.setMessage("Working...");
		progress.show();
	}

	public void stopLoading() {
		progress.dismiss();
		progress = null;
		DataManager.prepareForEject();
		startActivity(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
		Toast.makeText(getActivity(), "Scroll down, press \"Unmount\", press back button.", Toast.LENGTH_LONG).show();
	}

	private void startImport() {
		if (DataManager.isFlashDriveConnected(getActivity())) {
			new ImportPickListTask().execute(new SettingsFragment[] { this });
		} else {
			displayFlashDriveWarning(1);
		}
	}

	private void startExport() {
		if (DataManager.isFlashDriveConnected(getActivity())) {
			new ExportPickListTask().execute(new SettingsFragment[] { this });
		} else {
			displayFlashDriveWarning(2);
		}
	}

}
