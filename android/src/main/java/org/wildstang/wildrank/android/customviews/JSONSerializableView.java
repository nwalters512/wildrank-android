package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.interfaces.IJSONSerializable;

public abstract class JSONSerializableView extends RelativeLayout implements IJSONSerializable {

	protected String key;
	protected int defaultBackground;
	protected boolean isComplete = true;

	public JSONSerializableView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
		key = a.getString(R.styleable.JSONSerializableView_key);
		a.recycle();
	}

	@Override
	public String getKey() {
		return key;
	}

	public boolean isComplete(boolean highlight) {
		// Android resets the padding when we manually set a background
		// We save the padding beforehand and re-add it after we set a new background
		int l = getChildAt(0).getPaddingLeft();
		int r = getChildAt(0).getPaddingRight();
		int t = getChildAt(0).getPaddingTop();
		int b = getChildAt(0).getPaddingBottom();
		if (highlight && !isComplete) {
			getChildAt(0).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
		} else {
			getChildAt(0).setBackgroundResource(R.drawable.widget_color_list);
		}
		getChildAt(0).setPadding(l, t, r, b);
		return isComplete;
	}

}
