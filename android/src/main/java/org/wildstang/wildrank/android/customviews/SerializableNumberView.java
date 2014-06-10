package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.wildstang.wildrank.R;

import java.util.Locale;

public class SerializableNumberView extends JSONSerializableView {

	private TextView labelView;
	private EditText valueView;

	public SerializableNumberView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custom_view_number, this, true);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
		String label = a.getString(R.styleable.JSONSerializableView_label);
		a.recycle();

		labelView = (TextView) findViewById(R.id.label);
		labelView.setText(label);

		valueView = (EditText) findViewById(R.id.value);
		valueView.setClickable(false);
		// When we receive focus after pressing "Next", pass focus onto the edittext
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					valueView.requestFocus();
				}

			}
		});
	}

	public void setValue(double value) {
		valueView.setText(formatDouble(value));
	}

	@Override
	public void writeContentsToBundle(Bundle b) {
		if (valueView.getText().toString().trim().length() != 0) {
			// Only save the value if it isn't empty
			b.putDouble(key, Double.parseDouble(valueView.getText().toString()));
		} else {
			b.putDouble(key, Double.valueOf(0.0));
		}
	}

	public static String formatDouble(double d) {
		if (d == (int) d) {
			return String.format(Locale.US, "%d", (int) d);
		} else {
			return String.format(Locale.US, "%s", d);
		}
	}

	@Override
	public void restoreFromJSONObject(Object object) {
		if (object instanceof Integer) {
			setValue((double) ((Integer) object).intValue());
		} else if (object instanceof Double) {
			setValue((Double) object);
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
