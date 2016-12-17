package org.wildstang.wildrank.android.fragments;

import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.interfaces.IListFragmentCallbacks;
import org.wildstang.wildrank.android.utils.Keys;

/*
 * Activity that displays a list of teams to be pit scouted. When the user selects a team and 
 * decides to scout a team, this activity prompts them for their name and launches
 *  a pit scouting activity.
 */

public class PitScoutingMainFragment extends Fragment implements IListFragmentCallbacks, OnNavigationListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayAllTeamList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_selection, container, false);
    }

    /*
     * Displays the list of teams
     */
    private void displayAssignedTeamList() {
        Fragment fragment = new ViewQueryResultsInListFragment();
        ((ViewQueryResultsInListFragment) fragment).setCallbacks(this);
        Bundle args = new Bundle();
        args.putInt(ViewQueryResultsInListFragment.QUERY_TYPE, ViewQueryResultsInListFragment.ASSIGNED_PIT_GROUP);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.list_container, fragment, "teamList").commitAllowingStateLoss();
    }

    /*
     * Displays the list of teams
     */
    private void displayAllTeamList() {
        Fragment fragment = new ViewQueryResultsInListFragment();
        ((ViewQueryResultsInListFragment) fragment).setCallbacks(this);
        Bundle args = new Bundle();
        args.putInt(ViewQueryResultsInListFragment.QUERY_TYPE, ViewQueryResultsInListFragment.ALL_PIT_LIST);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.list_container, fragment, "teamList").commitAllowingStateLoss();
    }

    @Override
    public void onListItemClick(String fragmentTag, int position, long id) {
        if (fragmentTag.equals("teamList")) {
            FragmentManager fm = getFragmentManager();
            TeamDetailsFragment mf = new TeamDetailsFragment();
            Bundle args = new Bundle();
            args.putLong(Keys.TEAM_ID, id);
            args.putInt(TeamDetailsFragment.DETAILS_MODE, TeamDetailsFragment.MODE_PIT);
            mf.setArguments(args);
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("teamDetails") != null) {
                ft.remove(fm.findFragmentByTag("teamDetails"));
            }
            ft.add(R.id.details_container, mf, "teamDetails").commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        switch (position) {
            case 0:
                displayAllTeamList();
                break;
            case 1:
                displayAssignedTeamList();
                break;
        }
        return true;
    }
}
