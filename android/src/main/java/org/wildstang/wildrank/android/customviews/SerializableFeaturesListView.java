package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wildstang.wildrank.android.R;

import java.util.ArrayList;
import java.util.List;

public class SerializableFeaturesListView extends JSONSerializableView implements OnClickListener {

    private Button newFeatureButton;
    private List<FeatureView> featureViews = new ArrayList<>();

    public SerializableFeaturesListView(Context context, AttributeSet attributes) {
        super(context, attributes);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_view_features_list, this, true);
        newFeatureButton = (Button) findViewById(R.id.new_feature);
        newFeatureButton.setOnClickListener(this);
    }

    @Override
    public void writeContentsToBundle(Bundle b) {
        ArrayList<Bundle> features = new ArrayList<>();
        for (FeatureView featureView : featureViews) {
            Bundle feature = new Bundle();
            // If either feature field is empty, don't save the feature
            if (!featureView.getFeatureName().isEmpty() && !featureView.getActuator().isEmpty()) {
                feature.putString("name", featureView.getFeatureName());
                feature.putStringArray("actuators", new String[]{featureView.getActuator()});
                features.add(feature);
            }
        }
        Bundle[] featuresArray = features.toArray(new Bundle[features.size()]);
        b.putParcelableArray(key, featuresArray);
    }

    @Override
    public void onClick(View v) {
        if (v == newFeatureButton) {
            addFeatureToList(true);
        }
    }

    private void addFeatureToList(boolean scrollToEnd) {
        addFeatureToList("", "");
        if (scrollToEnd) {
            final ViewParent parent = getParent();
            if (parent instanceof ScrollView) {
                ((ScrollView) parent).post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView) parent).fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }
    }

    private void addFeatureToList(String featureName, String actuatorName) {
        FeatureView feature = new FeatureView(getContext(), null);
        if (!featureName.isEmpty()) {
            feature.setFeatureName(featureName);
        }
        if (!actuatorName.isEmpty()) {
            feature.setActuator(actuatorName);
        }
        feature.setId(featureViews.size() + 1);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 10;
        if (featureViews.size() == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        } else {
            params.addRule(RelativeLayout.BELOW, featureViews.get(featureViews.size() - 1).getId());
        }
        featureViews.add(feature);
        addView(feature, params);
        //Update button position
        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, feature.getId());
        updateViewLayout(newFeatureButton, params);
    }

    public void removeFeatureFromList(FeatureView view) {
        if (featureViews.contains(view)) {
            int indexOfView = featureViews.indexOf(view);
            if (featureViews.size() != 1 && indexOfView == featureViews.size() - 1) {
                //Update button position
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, featureViews.get(indexOfView - 1).getId());
                updateViewLayout(newFeatureButton, params);
                featureViews.remove(view);
                removeView(view);
            } else if (indexOfView == 0) {
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                if (featureViews.size() != 1) {
                    updateViewLayout(featureViews.get(indexOfView + 1), params);
                }
                featureViews.remove(view);
                removeView(view);
            } else {
                View previousView = featureViews.get(indexOfView - 1);
                View nextView = featureViews.get(indexOfView + 1);
                featureViews.remove(view);
                removeView(view);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, previousView.getId());
                updateViewLayout(nextView, params);
            }
        }
    }

    @Override
    public void restoreFromJSONObject(Object object) {
        if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject currentObject = array.getJSONObject(i);
                    String featureName = currentObject.getString("name");
                    String actuator = currentObject.getJSONArray("actuators").getString(0);
                    addFeatureToList(featureName, actuator);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
