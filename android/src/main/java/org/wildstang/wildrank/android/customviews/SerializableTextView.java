package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import org.wildstang.wildrank.R;

public class SerializableTextView extends JSONSerializableView {

	private TextView labelView;
	private EditText valueView;

	public SerializableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custom_view_edit_text, this, true);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
		String label = a.getString(R.styleable.JSONSerializableView_label);
		a.recycle();

		labelView = (TextView) findViewById(R.id.label);
		labelView.setText(label);

		valueView = (EditText) findViewById(R.id.value);
	}

	public void setText(String text) {
		valueView.setText(text);
	}

	@Override
	public void writeContentsToBundle(Bundle b) {
		b.putString(key, valueView.getText().toString());

	}

	@Override
	public void restoreFromJSONObject(Object object) {
		if (object instanceof String) {
			valueView.setText((String) object);
		}
	}

	@Override
	public boolean isComplete(boolean highlight) {
		if (valueView.getText().toString().isEmpty()) {
			isComplete = false;
		} else {
			isComplete = true;
		}
		return super.isComplete(highlight);
	}

}
