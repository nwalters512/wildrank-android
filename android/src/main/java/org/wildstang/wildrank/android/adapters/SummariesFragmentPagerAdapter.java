package org.wildstang.wildrank.android.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

import org.wildstang.wildrank.android.fragments.TeamDataFragment;
import org.wildstang.wildrank.android.fragments.TeamSummaryFragment;
import org.wildstang.wildrank.android.utils.Keys;

public class SummariesFragmentPagerAdapter extends FragmentStatePagerAdapter {

    static final int NUM_FRAGMENTS = 2;
    private Bundle fragmentArgs;

    private TeamSummaryFragment summaryFragment;
    private TeamDataFragment dataFragment;

    public SummariesFragmentPagerAdapter(FragmentManager fm, long teamID) {
        super(fm);
        Log.d("SummariesFragmentPagerAdapter", "team id: " + teamID);
        initFragments(teamID);
    }

    private void initFragments(long teamID) {
        fragmentArgs = new Bundle();
        fragmentArgs.putLong(Keys.TEAM_ID, teamID);
        summaryFragment = new TeamSummaryFragment();
        summaryFragment.setArguments(fragmentArgs);
        dataFragment = new TeamDataFragment();
        dataFragment.setArguments(fragmentArgs);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return summaryFragment;
            case 1:
                return dataFragment;
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return NUM_FRAGMENTS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "General";
            case 1:
                return "Data";
            default:
                return "ERROR INVALID POSITION";
        }
    }

    public void changeTeamID(long newTeamID) {
        initFragments(newTeamID);
        notifyDataSetChanged();
    }

}
