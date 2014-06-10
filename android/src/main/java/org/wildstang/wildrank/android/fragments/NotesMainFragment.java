package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.activities.MainActivity;
import org.wildstang.wildrank.android.interfaces.IListFragmentCallbacks;
import org.wildstang.wildrank.android.utils.Keys;

public class NotesMainFragment extends Fragment implements IListFragmentCallbacks {

	public static final String NOTES_MODE = "mode";
	public static final String MATCH_NUMBER = "matchnum";
	public static final int MODE_ALL = 1;
	public static final int MODE_CURRENT_MATCH = 2;

	private int notesMode;
	private int matchNumber;

	private Button advance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notesMode = MODE_ALL;
		if (getArguments() != null) {
			notesMode = getArguments().getInt(NOTES_MODE, MODE_ALL);
			matchNumber = getArguments().getInt(MATCH_NUMBER, -1);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getFragmentManager().beginTransaction().replace(R.id.pick_list_fragment, new PickListFragment(), "pickList").commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	public NotesMainFragment setMode(int mode) {
		this.notesMode = mode;
		update();
		return this;
	}

	public NotesMainFragment setMatchNumber(int matchNum) {
		this.matchNumber = matchNum;
		return this;
	}

	public void update() {
		if (notesMode == MODE_CURRENT_MATCH) {
			advance.setVisibility(View.VISIBLE);
		} else {
			advance.setVisibility(View.GONE);
		}
		displayTeamList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.fragment_notes_main, container, false);
		advance = ((Button) ((ViewGroup) v).findViewById(R.id.button));
		advance.setText("Advance to next match");
		advance.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getActivity() instanceof MainActivity) {
					int newSpinnerPosition = matchNumber;
					int spinnerCount = ((Spinner) getActivity().getActionBar().getCustomView().findViewById(R.id.match_number)).getAdapter().getCount();
					Log.d("onClick", "new spinner position: " + newSpinnerPosition);
					Log.d("onClick", "spinner count: " + spinnerCount);
					Log.d("onClick", "is position less than count? " + ((newSpinnerPosition < spinnerCount) ? "true" : "false"));
					if (newSpinnerPosition < spinnerCount) {
						((Spinner) getActivity().getActionBar().getCustomView().findViewById(R.id.match_number)).setSelection(newSpinnerPosition);
						update();
					}
				}
			}
		});
		return v;
	}

	private void displayTeamList() {
		// Display loaded list in fragment
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment f = new ViewQueryResultsInListFragment();
		((ViewQueryResultsInListFragment) f).setCallbacks(this);
		Bundle args = new Bundle();
		switch (notesMode) {
		case MODE_ALL:
			args.putInt(ViewQueryResultsInListFragment.QUERY_TYPE, ViewQueryResultsInListFragment.GENERAL_TEAM_LIST);
			break;
		case MODE_CURRENT_MATCH:
			args.putInt(ViewQueryResultsInListFragment.QUERY_TYPE, ViewQueryResultsInListFragment.TEAMS_FROM_MATCH_LIST);
			args.putInt(ViewQueryResultsInListFragment.QUERY_MATCH_NUMBER, matchNumber);
			break;
		}
		f.setArguments(args);
		transaction.replace(R.id.list_container, f, "teamList");
		transaction.commit();
	}

	@Override
	public void onListItemClick(String fragmentTag, int position, long id) {
		if (fragmentTag.equals("teamList")) {
			FragmentManager fm = getFragmentManager();
			TeamDetailsFragment mf = new TeamDetailsFragment();
			Bundle args = new Bundle();
			args.putLong(Keys.TEAM_ID, id);
			args.putInt(TeamDetailsFragment.DETAILS_MODE, TeamDetailsFragment.MODE_NOTES);
			mf.setArguments(args);
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.details_container, mf, "teamDetails").commit();
		}
	}
}
