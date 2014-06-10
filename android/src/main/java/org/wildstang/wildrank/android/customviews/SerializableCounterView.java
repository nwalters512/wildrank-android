package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.TextView;

import org.wildstang.wildrank.R;

public class SerializableCounterView extends JSONSerializableView {

	private TextView labelView;
	private TextView countView;
	private int count;

	public SerializableCounterView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custom_view_counter, this, true);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
		String label = a.getString(R.styleable.JSONSerializableView_label);
		a.recycle();

		labelView = (TextView) findViewById(R.id.label);
		labelView.setText(label);

		countView = (TextView) findViewById(R.id.count);
		countView.setText(Integer.toString(count));

		// Make view clickable
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				count++;
				countView.setText(Integer.toString(count));
			}
		});

		// Long clicks subtract from count
		this.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (count > 0) {
					count--;
					playSoundEffect(SoundEffectConstants.CLICK);
				}
				countView.setText(Integer.toString(count));
				return true;
			}
		});
	}

	public void setCount(int count) {
		this.count = count;
		countView.setText(Integer.toString(this.count));
	}

	@Override
	public void writeContentsToBundle(Bundle b) {
		b.putInt(key, count);
	}

	@Override
	public void restoreFromJSONObject(Object object) {
		if (object instanceof Integer) {
			setCount((Integer) object);
		}

	}
}
