package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;

public class SerializableCheckboxView extends JSONSerializableView {

    private TextView labelView;
    private CheckBox checkboxView;

    public SerializableCheckboxView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_view_checkbox, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
        String label = a.getString(R.styleable.JSONSerializableView_label);
        a.recycle();

        labelView = (TextView) findViewById(R.id.label);
        labelView.setText(label);

        checkboxView = (CheckBox) findViewById(R.id.checkbox);
        checkboxView.setClickable(false);
        // This conflicts with our custom state saving
        checkboxView.setSaveEnabled(false);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checkboxView.setChecked(!checkboxView.isChecked());
            }
        });
    }

    public void setState(boolean state) {
        checkboxView.setChecked(state);
    }

    @Override
    public void writeContentsToBundle(Bundle b) {
        b.putBoolean(key, checkboxView.isChecked());
    }

    @Override
    public void restoreFromJSONObject(Object object) {
        if (object instanceof Boolean) {
            setState((Boolean) object);
        }
    }
}
