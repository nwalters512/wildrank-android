package org.wildstang.wildrank.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;

public class AutonomousScoutingFragment extends ScoutingFragment {

    public AutonomousScoutingFragment() {
        super("autonomous");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scout_autonomous, container, false);
        setRetainInstance(true);
        Bundle b = ((IScoutingFragmentHost) getActivity()).getScoutingViewStateBundle();
        super.restoreViewsFromBundle(b, (ViewGroup) view);
        return view;
    }

}
