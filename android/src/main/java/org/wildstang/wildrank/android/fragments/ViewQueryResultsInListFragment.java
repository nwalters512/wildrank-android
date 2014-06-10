package org.wildstang.wildrank.android.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.adapters.MatchAdapter;
import org.wildstang.wildrank.android.adapters.TeamAdapter;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.interfaces.IListFragmentCallbacks;
import org.wildstang.wildrank.android.utils.Keys;

public class ViewQueryResultsInListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	// Constants for use in creating the bundle passed to this fragment
	public static final String QUERY_TYPE = "type";
	public static final String QUERY_KEY = "key";
	public static final String QUERY_MATCH_NUMBER = "matchnum";
	public static final int ALL_PIT_LIST = 1;
	public static final int MATCH_LIST = 2;
	public static final int ASSIGNED_PIT_GROUP = 3;
	public static final int GENERAL_TEAM_LIST = 4;
	public static final int TEAMS_FROM_MATCH_LIST = 5;

	private static final int LOAD_LIST_OF_TEAMS = 6;
	private static final String LIST_OF_TEAMS = "listOfTeams";

	private CursorAdapter adapter;
	private int queryType;
	private String queryKey;
	private int matchNumber;

	private IListFragmentCallbacks callback;

	private OnSharedPreferenceChangeListener listener;

	public void setCallbacks(IListFragmentCallbacks callbacks) {
		this.callback = callbacks;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		queryType = args.getInt(QUERY_TYPE);
		queryKey = args.getString(QUERY_KEY, "null");
		matchNumber = args.getInt(QUERY_MATCH_NUMBER, -1);
		if (queryType == TEAMS_FROM_MATCH_LIST && matchNumber == -1) {
			Log.e("ViewQuereyResultsInListFragment.onCreate", "Invalid argument; you must supply a valid match number!");
		}

		// Watch for preference changes so we know when to reload the list
		listener = new OnSharedPreferenceChangeListener() {
			// If the team we are configured to scout for changes, reload the
			// list
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("pitGroup") && ViewQueryResultsInListFragment.this.queryType == ASSIGNED_PIT_GROUP) {
					if (ViewQueryResultsInListFragment.this.isAdded()) {
						restartLoader();
					}
				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
		View v = inflater.inflate(R.layout.fragment_list, group, false);
		// Show fast-scrolling bar
		((ListView) v.findViewById(android.R.id.list)).setFastScrollAlwaysVisible(true);
		((ListView) v.findViewById(android.R.id.list)).setFastScrollEnabled(true);
		getLoaderManager().initLoader(queryType, null, this);
		return v;

	}

	@Override
	public Loader<Cursor> onCreateLoader(int type, Bundle args) {
		switch (type) {
		case GENERAL_TEAM_LIST:
		case ALL_PIT_LIST:
			return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), DatabaseContract.Team.ALL_COLUMNS, null, null, DatabaseContract.Team.NUMBER);
		case MATCH_LIST:
			return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/" + queryKey + "/match"), DatabaseContract.Match.ALL_COLUMNS, null, null,
					DatabaseContract.Match.NUMBER);
		case ASSIGNED_PIT_GROUP:
			return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "pit"), DatabaseContract.Team.ALL_COLUMNS, null, null, DatabaseContract.Team.NUMBER);
		case TEAMS_FROM_MATCH_LIST:
			String selection = DatabaseContract.Match.NUMBER + "=?";
			queryKey = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Keys.CONFIGURED_EVENT, "null");
			return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/" + queryKey + "/match"), DatabaseContract.Match.ALL_COLUMNS, selection,
					new String[] { "" + matchNumber }, DatabaseContract.Match.NUMBER);
		case LOAD_LIST_OF_TEAMS:
			int[] teams = args.getIntArray(LIST_OF_TEAMS);
			System.out.println("teamslist");
			for (int i = 0; i < teams.length; i++) {
				System.out.println(teams[i]);
			}
			boolean firstIteration = true;
			StringBuilder selectionArgsBuilder = new StringBuilder();
			selectionArgsBuilder.append('(');
			for (int i = 0; i < teams.length; i++) {
				if (firstIteration) {
					selectionArgsBuilder.append(teams[i]);
					firstIteration = false;
				} else {
					selectionArgsBuilder.append(',').append(teams[i]);
				}
			}
			selectionArgsBuilder.append(')');
			selection = DatabaseContract.Team.NUMBER + " IN " + selectionArgsBuilder.toString();

			return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), DatabaseContract.Team.ALL_COLUMNS, selection, null, DatabaseContract.Team.NUMBER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (id) {
		case GENERAL_TEAM_LIST:
			adapter = new TeamAdapter(getActivity(), cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, TeamAdapter.MODE_GENERAL);
			setListAdapter(adapter);
			break;
		case MATCH_LIST:
			adapter = new MatchAdapter(getActivity(), cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			setListAdapter(adapter);
			break;
		case ALL_PIT_LIST:
		case ASSIGNED_PIT_GROUP:
			adapter = new TeamAdapter(getActivity(), cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, TeamAdapter.MODE_PIT);
			setListAdapter(adapter);
			break;
		case TEAMS_FROM_MATCH_LIST:
			int[] teams = new int[6];
			cursor.moveToFirst();
			teams[0] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.RED_1));
			teams[1] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.RED_2));
			teams[2] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.RED_3));
			teams[3] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.BLUE_1));
			teams[4] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.BLUE_2));
			teams[5] = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Match.BLUE_3));
			Bundle arguments = new Bundle();
			arguments.putIntArray(LIST_OF_TEAMS, teams);
			getLoaderManager().initLoader(LOAD_LIST_OF_TEAMS, arguments, this);
			break;
		case LOAD_LIST_OF_TEAMS:
			adapter = new TeamAdapter(getActivity(), cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, TeamAdapter.MODE_GENERAL);
			setListAdapter(adapter);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		adapter.changeCursor(null);
	}

	public void restartLoader() {
		getLoaderManager().restartLoader(queryType, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		callback.onListItemClick(this.getTag(), position, id);
	}

}
