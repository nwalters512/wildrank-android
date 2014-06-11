package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.interfaces.IListFragmentCallbacks;
import org.wildstang.wildrank.android.utils.Keys;

/*
 * Activity that displays a list of matches to be scouted. User can select a match and see
 * what team they are designated to scout for that match. When the user decides to scout a
 * match, this activity prompts them for their name and launches a match scouting activity.
 */

public class MatchScoutingMainFragment extends Fragment implements IListFragmentCallbacks {

    public static final int MATCH_SCOUTING_FINISHED = 0;
    public static final int MATCH_SCOUTING_SUCCESSFUL = 1;
    private int matchPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayMatchList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_selection, container, false);
    }

    /*
     * Adds a Fragment to display the list of matches
     */
    private void displayMatchList() {
        // Display loaded list in fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new ViewQueryResultsInListFragment();
        ((ViewQueryResultsInListFragment) f).setCallbacks(this);
        Bundle args = new Bundle();
        args.putInt(ViewQueryResultsInListFragment.QUERY_TYPE, ViewQueryResultsInListFragment.MATCH_LIST);
        args.putString(ViewQueryResultsInListFragment.QUERY_KEY, PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Keys.CONFIGURED_EVENT, "null"));
        f.setArguments(args);
        transaction.add(R.id.list_container, f, "matchList");
        transaction.commitAllowingStateLoss();
    }

    // Called by hosting activity to advance to next match after completing the previous match
    public void advanceToNextMatch() {
        ViewQueryResultsInListFragment f = (ViewQueryResultsInListFragment) getFragmentManager().findFragmentByTag("matchList");
        ListView list = f.getListView();
        if (matchPosition + 1 < list.getAdapter().getCount()) {
            f.getListView().performItemClick(list.getAdapter().getView(matchPosition + 1, null, null), matchPosition + 1, list.getAdapter().getItemId(matchPosition + 1));
        }
    }

    /*
     * When a match is clicked, add another fragment to display that match's
     * details
     */
    @Override
    public void onListItemClick(String fragmentTag, int position, long id) {
        if (fragmentTag.equals("matchList")) {
            FragmentManager fm = getFragmentManager();
            ViewQueryResultsInListFragment f = (ViewQueryResultsInListFragment) fm.findFragmentByTag(fragmentTag);
            Cursor c = (Cursor) f.getListAdapter().getItem(position);
            String matchKey = c.getString(c.getColumnIndex(DatabaseContract.Match.KEY));
            MatchDetailsFragment mf = new MatchDetailsFragment();
            Bundle args = new Bundle();
            args.putString("matchKey", matchKey);
            mf.setArguments(args);
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("matchDetails") != null) {
                ft.remove(fm.findFragmentByTag("matchDetails"));
            }
            ft.add(R.id.details_container, mf, "matchDetails").commit();
            matchPosition = position;
        }
    }
}
