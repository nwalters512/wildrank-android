package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.adapters.PickListAdapter;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.dragndrop.DragNDropCursorAdapter;
import org.wildstang.wildrank.android.dragndrop.DragNDropListView;
import org.wildstang.wildrank.android.dragndrop.DragNDropListView.OnItemDragNDropListener;

public class PickListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemDragNDropListener, OnItemLongClickListener, OnItemClickListener {

	private DragNDropListView listView;
	private DragNDropCursorAdapter listAdapter;
	private Thread t;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_pick_list, container, false);
		listView = (DragNDropListView) v.findViewById(R.id.list);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = new PickListAdapter(getActivity(), R.layout.list_item_draggable_team, null, new String[] { DatabaseContract.Team.NUMBER }, new int[] { R.id.number }, R.id.handler, R.id.top_button, R.id.bottom_button);
		listView.setDragNDropAdapter(listAdapter);
		listView.setOnItemDragNDropListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team"), DatabaseContract.Team.ALL_COLUMNS, null, null,
				DatabaseContract.Team.PICK_LIST_RANKING + " ASC, " + DatabaseContract.Team.NUMBER + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d("onLoadFinished", "Load finished! cursor size: " + data.getCount());
		listAdapter.swapCursor(data);
		Cursor c = listAdapter.getCursor();
		c.moveToPosition(-1);
		while (c.moveToNext()) {
			int teamNum = c.getInt(c.getColumnIndex(DatabaseContract.Team.NUMBER));
			int sortOrder = c.getInt(c.getColumnIndex(DatabaseContract.Team.PICK_LIST_RANKING));
			Log.d("onLoadFinished", "Team: " + teamNum + "; Sort order: " + sortOrder);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemDrop(DragNDropListView parent, View view, final int startPosition, final int endPosition, long id) {
		// Update the items in the appropriate range
		// This range is from the index of the starting position to the index of the ending position
		if (t != null) {
			if (t.isAlive()) {
				t.interrupt();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		t = new Thread(new Runnable() {
			final View progressBar = getView().findViewById(R.id.title_progress_bar);

			public void run() {
				progressBar.post(new Runnable() {
					public void run() {
						showSavingProgressBar();
					}
				});
				ContentValues cv;
				for (int i = 0; i < listAdapter.getCount(); i++) {
					if (Thread.interrupted()) {
						break;
					}
					long itemID = listAdapter.getItemId(i);
					cv = new ContentValues();
					cv.put(DatabaseContract.Team.PICK_LIST_RANKING, i);
					getActivity().getContentResolver().update(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + itemID), cv, null, null);
				}

				progressBar.post(new Runnable() {
					public void run() {
						hideSavingProgressBar();
					}
				});
			}
		});
		t.start();

	}

	private void showSavingProgressBar() {
		getView().findViewById(R.id.title_progress_bar).setVisibility(View.VISIBLE);
	}

	private void hideSavingProgressBar() {
		getView().findViewById(R.id.title_progress_bar).setVisibility(View.GONE);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("PickListFragment", "long click at " + position);
		Cursor c = listAdapter.getCursor();
		c.moveToPosition(position);
		int tier = c.getInt(c.getColumnIndex(DatabaseContract.Team.PICK_LIST_TIER));
		tier++;
		if(tier > 5) {
			tier = 0;
		}
		ContentValues cv = new ContentValues();
		cv.put(DatabaseContract.Team.PICK_LIST_TIER, tier);
		getActivity().getContentResolver().update(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + id), cv, null, null);
		Log.d("longClick", "tier: " + tier);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor c = listAdapter.getCursor();
		c.moveToPosition(position);
		int picked = c.getInt(c.getColumnIndex(DatabaseContract.Team.PICK_LIST_PICKED));
		if(picked != 0) {
			picked = 0;
		} else {
			picked = 1;
		}
		ContentValues cv = new ContentValues();
		cv.put(DatabaseContract.Team.PICK_LIST_PICKED, picked);
		getActivity().getContentResolver().update(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/" + id), cv, null, null);
	}

}
