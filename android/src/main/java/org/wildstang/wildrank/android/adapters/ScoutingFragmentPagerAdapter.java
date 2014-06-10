package org.wildstang.wildrank.android.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import org.wildstang.wildrank.android.activities.ScoutMatchActivity;

public class ScoutingFragmentPagerAdapter extends FragmentPagerAdapter {

	static final int NUM_FRAGMENTS = 3;
	private ScoutMatchActivity hostActivity;

	public ScoutingFragmentPagerAdapter(FragmentManager fm, ScoutMatchActivity hostActivity) {
		super(fm);
		this.hostActivity = hostActivity;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return hostActivity.getAutonomousFragment();
		case 1:
			return hostActivity.getTeleopFragment();
		case 2:
			return hostActivity.getPostMatchFragment();
		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Autonomous";
		case 1:
			return "Teleop";
		case 2:
			return "Post-match";
		default:
			return "ERROR INVALID POSITION";
		}
	}

}
