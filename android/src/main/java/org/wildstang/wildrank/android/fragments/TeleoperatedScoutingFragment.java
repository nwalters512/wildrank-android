package org.wildstang.wildrank.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;

public class TeleoperatedScoutingFragment extends ScoutingFragment {

	public TeleoperatedScoutingFragment() {
		super("teleop");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scout_teleop, container, false);
		Bundle b = ((IScoutingFragmentHost) getActivity()).getScoutingViewStateBundle();
		super.restoreViewsFromBundle(b, (ViewGroup) view);
		return view;
	}

}
