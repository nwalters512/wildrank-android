/*
 * Copyright 2012 Terlici Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildstang.wildrank.android.dragndrop;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class DragNDropCursorAdapter extends SimpleCursorAdapter implements DragNDropAdapter {
	int mPosition[];
	int mHandler;
	int mMoveToTop;
	int mMoveToBottom;
	ListViewButtonListener buttonListener;

	public DragNDropCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int handler, int moveToTop, int moveToBottom) {
		super(context, layout, cursor, from, to, 0);

		mHandler = handler;
		mMoveToTop = moveToTop;
		mMoveToBottom = moveToBottom;
		setup();
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		Cursor cursor = super.swapCursor(c);

		mPosition = null;
		setup();

		return cursor;
	}

	private void setup() {
		Cursor c = getCursor();

		if (c == null || !c.moveToFirst())
			return;

		mPosition = new int[c.getCount()];

		for (int i = 0; i < mPosition.length; ++i)
			mPosition[i] = i;
	}

	@Override
	public View getDropDownView(int position, View view, ViewGroup group) {
		return super.getDropDownView(mPosition[position], view, group);
	}

	@Override
	public Object getItem(int position) {
		return super.getItem(mPosition[position]);
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(mPosition[position]);
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(mPosition[position]);
	}

	@Override
	public View getView(final int position, View view, ViewGroup group) {
		View v = super.getView(mPosition[position], view, group);
		Button top = ((Button) v.findViewById(mMoveToTop));
		if (top != null) {
			top.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (buttonListener != null) {
						buttonListener.onButtonClick(v.getId(), position);
					}
				}
			});
		}
		
		Button bottom = ((Button) v.findViewById(mMoveToBottom));
		if (bottom != null) {
			bottom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (buttonListener != null) {
						buttonListener.onButtonClick(v.getId(), position);
					}
				}
			});
		}
		return v;
	}

	@Override
	public boolean isEnabled(int position) {
		return super.isEnabled(mPosition[position]);
	}

	@Override
	public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

	}

	@Override
	public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
		int position = mPosition[startPosition];

		if (startPosition < endPosition) {
			for (int i = startPosition; i < endPosition; ++i) {
				mPosition[i] = mPosition[i + 1];
			}
		} else if (endPosition < startPosition) {
			for (int i = startPosition; i > endPosition; --i) {
				mPosition[i] = mPosition[i - 1];
			}
		}
		mPosition[endPosition] = position;
	}

	@Override
	public int getDragHandler() {
		return mHandler;
	}

	@Override
	public int getMoveToTopButton() {
		return mMoveToTop;
	}

	@Override
	public int getMoveToButtomButton() {
		return mMoveToBottom;
	}

	@Override
	public void setListItemButtonClickListener(ListViewButtonListener listener) {
		this.buttonListener = listener;
	}

}
