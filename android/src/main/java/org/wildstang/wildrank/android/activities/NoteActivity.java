package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.customviews.TeamDetailsView;
import org.wildstang.wildrank.android.fragments.NoteFragment;
import org.wildstang.wildrank.android.fragments.PickListFragment;
import org.wildstang.wildrank.android.utils.Keys;

/*
 * Activity that provides an interface for entering notes for a specific team. It consists of a TextView
 * that displays notes that were previously compiled by the desktop application and an EditText
 * that allows users to input new notes. The EditText is automatically filled with any notes that were
 * previously saved but not yet synced to the flash drive or integrated.
 */

public class NoteActivity extends Activity implements OnClickListener {

	private TeamDetailsView teamDetails;

	private int teamNumber;
	private String teamName;
	private long teamID;

	private NoteFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes);

		// Show the Up button in the action bar.
		setupActionBar();

		// Keep the screen on while we are scouting
		findViewById(android.R.id.content).setKeepScreenOn(true);

		// Get details about the team we are scouting from the intent
		Intent i = getIntent();
		teamNumber = i.getIntExtra(Keys.TEAM_NUMBER, -1);
		teamName = i.getStringExtra(Keys.TEAM_NAME);
		teamID = i.getLongExtra(Keys.TEAM_ID, -1);

		if (teamName == null || teamNumber == -1 || teamID == -1) {
			throw new IllegalArgumentException("ScoutPitActivity must be created with a valid match key, team ID, and team number");
		}

		// Create the notes fragment
		fragment = new NoteFragment();
		Bundle args = new Bundle();
		args.putInt(Keys.TEAM_NUMBER, teamNumber);
		args.putString(Keys.TEAM_NAME, teamName);
		args.putLong(Keys.TEAM_ID, teamID);
		fragment.setArguments(args);
		getFragmentManager().beginTransaction().add(R.id.note_fragment, fragment, "notes").commit();
		
		// Create pick list fragment
		Fragment picklist = new PickListFragment();
		getFragmentManager().beginTransaction().add(R.id.pick_list_fragment, picklist, "pickList").commit();

		// Save references to our important views
		teamDetails = (TeamDetailsView) findViewById(R.id.team_details);
		teamDetails.setTeamName(teamName);
		teamDetails.setTeamNumber(teamNumber);

		((Button) findViewById(R.id.finish)).setOnClickListener(this);
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			fragment.saveNotes();
			this.finish();
			Toast.makeText(this, "Notes saved successfully", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.finish) {
			fragment.saveNotes();
			Toast.makeText(this, "Notes saved successfully", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		fragment.saveNotes();
		Toast.makeText(this, "Notes saved successfully", Toast.LENGTH_SHORT).show();
		super.onBackPressed();
	}

}
