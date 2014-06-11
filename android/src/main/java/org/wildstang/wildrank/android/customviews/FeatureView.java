package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.wildstang.wildrank.android.R;

public class FeatureView extends LinearLayout implements OnClickListener {

    SerializableSpinnerView featureName;
    SerializableSpinnerView featureActuator;
    ImageButton deleteButton;

    public FeatureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_view_feature, this, true);

        featureName = (SerializableSpinnerView) findViewById(R.id.feature_name);
        featureActuator = (SerializableSpinnerView) findViewById(R.id.feature_actuator);

        deleteButton = ((ImageButton) findViewById(R.id.delete_feature));
        deleteButton.setOnClickListener(this);
    }

    public String getFeatureName() {
        return featureName.getSelectedItem();
    }

    public void setFeatureName(String name) {
        featureName.setSelectionBasedOnText(name);
    }

    public String getActuator() {
        return featureActuator.getSelectedItem();
    }

    public void setActuator(String actuator) {
        featureActuator.setSelectionBasedOnText(actuator);
    }

    @Override
    public void onClick(View v) {
        if (v == deleteButton) {
            ViewParent parentList = getParent();
            while (!(parentList instanceof SerializableFeaturesListView)) {
                parentList = parentList.getParent();
            }
            SerializableFeaturesListView list = (SerializableFeaturesListView) parentList;
            list.removeFeatureFromList(this);
        }
    }
}
