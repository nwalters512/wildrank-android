package org.wildstang.wildrank.android.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.adapters.ScoutingFragmentPagerAdapter;
import org.wildstang.wildrank.android.competitionmodels.CompetitionMatch;
import org.wildstang.wildrank.android.customviews.MatchDetailsView;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.MatchData;
import org.wildstang.wildrank.android.fragments.AutonomousScoutingFragment;
import org.wildstang.wildrank.android.fragments.MatchScoutingMainFragment;
import org.wildstang.wildrank.android.fragments.PostMatchScoutingFragment;
import org.wildstang.wildrank.android.fragments.TeleoperatedScoutingFragment;
import org.wildstang.wildrank.android.utils.JSONBundleTools;
import org.wildstang.wildrank.android.utils.Keys;

public class ScoutMatchActivity extends ScoutingActivity {

	public enum ScoutingState {
		STATE_AUTON,
		STATE_TELEOP,
		STATE_POST_MATCH
	}

	private String matchKey;
	private int teamNumber;
	private String allianceColor;
	private ScoutingState state;

	private AutonomousScoutingFragment autonFragment;
	private TeleoperatedScoutingFragment teleopFragment;
	private PostMatchScoutingFragment postMatchFragment;
	private MatchDetailsView matchDetails;

	private ViewPager pager;
	private ScoutingFragmentPagerAdapter pagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_match_scouting);

		// Keep the screen on while we are scouting
		findViewById(android.R.id.content).setKeepScreenOn(true);

		// Create a FragmentPagerAdapter
		pagerAdapter = new ScoutingFragmentPagerAdapter(getFragmentManager(), this);
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between pages, select the
				// corresponding tab.
				getActionBar().setSelectedNavigationItem(position);
				// Also note the current state
				switch (position) {
				case 0:
					state = ScoutingState.STATE_AUTON;
					break;
				case 1:
					state = ScoutingState.STATE_TELEOP;
					break;
				case 2:
					state = ScoutingState.STATE_POST_MATCH;
					break;
				default:
					state = ScoutingState.STATE_AUTON;
					break;
				}
			}
		});

		setupActionBar();

		// Note our current state if we are being restored
		String stateString = "null";
		if (savedInstanceState != null) {
			stateString = savedInstanceState.getString("state", "null");
		} else {
		}
		if (stateString.equals("auton")) {
			state = ScoutingState.STATE_AUTON;
		} else if (stateString.equals("teleop")) {
			state = ScoutingState.STATE_TELEOP;
		} else if (stateString.equals("post_match")) {
			state = ScoutingState.STATE_POST_MATCH;
		} else {
			state = ScoutingState.STATE_AUTON;
		}

		// Get match key and team number extras from the intent
		Intent i = getIntent();
		matchKey = i.getStringExtra(Keys.MATCH_KEY);
		teamNumber = i.getIntExtra(Keys.TEAM_NUMBER, -1);
		allianceColor = i.getStringExtra(Keys.ALLIANCE_COLOR);
		if (matchKey == null || teamNumber == -1) {
			throw new IllegalArgumentException("ScoutMatchActivity must be created with a valid match key and team number");
		}

		matchDetails = (MatchDetailsView) findViewById(R.id.match_details);
		matchDetails.setMatchNumber(CompetitionMatch.matchNumberFromMatchKey(matchKey));
		matchDetails.setTeamNumber(teamNumber);
		matchDetails.setAllianceColor(allianceColor);
		autonFragment = new AutonomousScoutingFragment();
		teleopFragment = new TeleoperatedScoutingFragment();
		postMatchFragment = new PostMatchScoutingFragment();
		Bundle args = new Bundle();
		args.putInt(Keys.TEAM_NUMBER, teamNumber);
		postMatchFragment.setArguments(args);
		this.switchToState(state);
	}

	private void setupActionBar() {

		ActionBar actionBar = getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);

		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				ScoutMatchActivity.this.pager.setCurrentItem(tab.getPosition());
			}

			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
				// hide the given tab
			}

			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
				// probably ignore this event
			}
		};

		// Add 3 tabs, specifying the tab's text and TabListener
		actionBar.addTab(actionBar.newTab().setText("Autonomous").setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Teleop").setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Post-Match").setTabListener(tabListener));

		actionBar.setTitle(R.string.activity_scout_match);

	}

	public void switchToState(ScoutingState state) {
		this.state = state;
		if (state == ScoutingState.STATE_AUTON) {
			pager.setCurrentItem(0);
		} else if (state == ScoutingState.STATE_TELEOP) {
			pager.setCurrentItem(1);
		} else if (state == ScoutingState.STATE_POST_MATCH) {
			pager.setCurrentItem(2);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle b) {
		super.onSaveInstanceState(b);
		String stateString;
		if (state == ScoutingState.STATE_AUTON) {
			stateString = "auton";
		} else if (state == ScoutingState.STATE_TELEOP) {
			stateString = "teleop";
		} else {
			stateString = "post_match";
		}
		b.putString("state", stateString);
	}

	public void scoutingComplete() {
		// To be called by PostMatchScoutingFragment when scouting is complete

		autonFragment.writeContentsToBundle(scoutingViewsState);
		teleopFragment.writeContentsToBundle(scoutingViewsState);
		postMatchFragment.writeContentsToBundle(scoutingViewsState);

		Log.d("auton complete", "" + autonFragment.isComplete());
		Log.d("teleop complete", "" + teleopFragment.isComplete());
		Log.d("postmatch complete", "" + postMatchFragment.isComplete());
		if (!autonFragment.isComplete() || !teleopFragment.isComplete() || !postMatchFragment.isComplete()) {
			Toast.makeText(this, "Some required fields are blank! Please fill in the fields highlighted in red.", Toast.LENGTH_LONG).show();
			return;
		}

		Bundle hierarchialBundle = JSONBundleTools.createHierarchicalBundle(scoutingViewsState);
		JSONObject scoringJson = JSONBundleTools.writeBundleToJSONObject(hierarchialBundle);
		JSONObject completeJson = new JSONObject();
		try {
			completeJson.put("match_number", CompetitionMatch.matchNumberFromMatchKey(matchKey));
			completeJson.put("team_number", teamNumber);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String scouterName = prefs.getString(Keys.SCOUTER_NAME, null);
			completeJson.put("scouter_id", scouterName);
			completeJson.put("scoring", scoringJson);
			Log.d("bundle", completeJson.toString(4));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			MatchData data = new MatchData();
			data.setMatchNumber(CompetitionMatch.matchNumberFromMatchKey(matchKey));
			data.setTeamNumber(teamNumber);
			data.setContent(completeJson.toString(4));
			DataManager.getInstance().saveChangedFile(this, data);
			Toast.makeText(this, "Match results saved successfully", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error saving match results", Toast.LENGTH_SHORT).show();
		}
		this.setResult(MatchScoutingMainFragment.MATCH_SCOUTING_SUCCESSFUL);
		this.finish();
	}

	public AutonomousScoutingFragment getAutonomousFragment() {
		return autonFragment;
	}

	public TeleoperatedScoutingFragment getTeleopFragment() {
		return teleopFragment;
	}

	public PostMatchScoutingFragment getPostMatchFragment() {
		return postMatchFragment;
	}
}
