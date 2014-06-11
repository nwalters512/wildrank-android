package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.adapters.SummariesFragmentPagerAdapter;
import org.wildstang.wildrank.android.interfaces.IListFragmentCallbacks;

public class TeamSummariesMainFragment extends Fragment implements IListFragmentCallbacks {

    public static final String SUMMARIES_MODE = "mode";
    public static final String MATCH_NUMBER = "matchnum";
    public static final int MODE_ALL = 1;
    public static final int MODE_CURRENT_MATCH = 2;

    private int summariesMode;
    private int matchNumber;

    private ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        summariesMode = MODE_ALL;
        if (getArguments() != null) {
            summariesMode = getArguments().getInt(SUMMARIES_MODE, MODE_ALL);
            matchNumber = getArguments().getInt(MATCH_NUMBER, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_team_summaries_main, container, false);
        // Create a FragmentPagerAdapter
        pager = (ViewPager) v.findViewById(R.id.pager);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFragmentManager().beginTransaction().replace(R.id.pick_list_container, new PickListFragment(), "pickList").commit();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        update();
    }

    public TeamSummariesMainFragment setMode(int mode) {
        this.summariesMode = mode;
        update();
        return this;
    }

    public TeamSummariesMainFragment setMatchNumber(int matchNum) {
        this.matchNumber = matchNum;
        return this;
    }

    public void update() {
        displayTeamList();
    }

    private void displayTeamList() {
        // Display loaded list in fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new ViewQueryResultsInListFragment();
        ((ViewQueryResultsInListFragment) f).setCallbacks(this);
        Bundle args = new Bundle();
        switch (summariesMode) {
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
            Log.d("onListItemClick", "team clicked with id: " + id);
            loadInfoForTeam(id);
        }
    }

    private void loadInfoForTeam(long teamID) {
        int pagerPosition = pager.getCurrentItem();
        if (pager.getAdapter() == null) {
            pager.setAdapter(new SummariesFragmentPagerAdapter(getFragmentManager(), teamID));
        } else {
            ((SummariesFragmentPagerAdapter) pager.getAdapter()).changeTeamID(teamID);
        }
        pager.setCurrentItem(pagerPosition);
    }

}
