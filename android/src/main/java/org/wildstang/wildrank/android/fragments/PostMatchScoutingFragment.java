package org.wildstang.wildrank.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.activities.ScoutMatchActivity;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;
import org.wildstang.wildrank.android.utils.Keys;

public class PostMatchScoutingFragment extends ScoutingFragment implements OnClickListener {

	private int teamNumber;

	public PostMatchScoutingFragment() {
		super("post_match");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		teamNumber = args.getInt(Keys.TEAM_NUMBER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scout_post_match, container, false);
		view.findViewById(R.id.finish).setOnClickListener(this);
		Bundle b = ((IScoutingFragmentHost) getActivity()).getScoutingViewStateBundle();
		super.restoreViewsFromBundle(b, (ViewGroup) view);
		return view;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.finish) {
			Bundle b = ((ScoutMatchActivity) getActivity()).getScoutingViewStateBundle();
			super.writeContentsToBundle(b, (ViewGroup) getView());
			((ScoutMatchActivity) getActivity()).scoutingComplete();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (!isVisibleToUser) { // Hide the keyboard when navigating away from this fragment
			if (getActivity() != null && getView() != null) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		NoteFragment f = new NoteFragment();
		Bundle args = new Bundle();
		args.putInt(Keys.TEAM_NUMBER, teamNumber);
		f.setArguments(args);
		getFragmentManager().beginTransaction().replace(R.id.notes_container, f, "notes").commit();
	}

}
