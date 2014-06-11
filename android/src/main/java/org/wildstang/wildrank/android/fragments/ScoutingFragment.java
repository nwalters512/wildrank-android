package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.wildstang.wildrank.android.customviews.SerializableCheckboxView;
import org.wildstang.wildrank.android.customviews.SerializableCounterView;
import org.wildstang.wildrank.android.customviews.SerializableNumberView;
import org.wildstang.wildrank.android.customviews.SerializableSpinnerView;
import org.wildstang.wildrank.android.customviews.SerializableTextView;
import org.wildstang.wildrank.android.interfaces.IJSONSerializable;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;

public abstract class ScoutingFragment extends Fragment {

    private String name;
    private boolean isComplete = true;
    private boolean shouldHighlightIncomplete = false;

    public ScoutingFragment(String name) {
        if (name != null) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("ScoutingFragments must be created with a valid name");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        shouldHighlightIncomplete = true;
        // Start under the assumption that we are complete
        isComplete = true;
        ViewGroup v = (ViewGroup) getView();
        if (v == null) {
            return true;
        }
        int childCount = v.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof IJSONSerializable) {
                if (!((IJSONSerializable) view).isComplete(true)) {
                    isComplete = false;
                }
            } else if (view instanceof ViewGroup) {
                isComplete((ViewGroup) view);
            }
        }
        return isComplete;
    }

    private boolean isComplete(ViewGroup v) {
        if (v == null) {
            return true;
        }
        int childCount = v.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof IJSONSerializable) {
                if (!((IJSONSerializable) view).isComplete(true)) {
                    isComplete = false;
                }
            } else if (view instanceof ViewGroup) {
                isComplete((ViewGroup) view);
            }
        }
        return isComplete;
    }

    public void writeContentsToBundle(Bundle b) {
        // Get the ViewGroup holding all of the widgets
        ViewGroup vg = (ViewGroup) getView();
        if (vg == null) {
            // If the view has been destroyed, state should already be saved
            // to parent activity
            return;
        }
        int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = vg.getChildAt(i);
            if (view instanceof IJSONSerializable) {
                ((IJSONSerializable) view).writeContentsToBundle(b);
            } else if (view instanceof ViewGroup) {
                writeContentsToBundle(b, (ViewGroup) view);
            }
        }
    }

    public void writeContentsToBundle(Bundle b, ViewGroup vg) {
        int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = vg.getChildAt(i);
            if (view instanceof IJSONSerializable) {
                ((IJSONSerializable) view).writeContentsToBundle(b);
            } else if (view instanceof ViewGroup) {
                writeContentsToBundle(b, (ViewGroup) view);
            }
        }
    }

    public void restoreViewsFromBundle(Bundle b, ViewGroup v) {
        int childCount = v.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof IJSONSerializable) {
                String key = ((IJSONSerializable) view).getKey();
                if (view instanceof SerializableCounterView) {
                    int count = b.getInt(key);
                    ((SerializableCounterView) view).setCount(count);
                } else if (view instanceof SerializableCheckboxView) {
                    boolean state = b.getBoolean(key);
                    ((SerializableCheckboxView) view).setState(state);
                } else if (view instanceof SerializableNumberView) {
                    double value = b.getDouble(key, -1);
                    if (value != -1) {
                        // Only set the value if a value has previously been
                        // stored, otherwise the field will start with 0.0 if it
                        // was empty when the activity was paused
                        ((SerializableNumberView) view).setValue(value);
                    }
                } else if (view instanceof SerializableTextView) {
                    String text = b.getString(key);
                    ((SerializableTextView) view).setText(text);
                } else if (view instanceof SerializableSpinnerView) {
                    String text = b.getString(key);
                    if (text != null) {
                        ((SerializableSpinnerView) view).setSelectionBasedOnText(text);
                    }
                }
            } else if (view instanceof ViewGroup) {
                restoreViewsFromBundle(b, (ViewGroup) view);
            }
        }
    }

    protected void restoreViewsFromJSON(String json, ViewGroup v) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject scoring = jsonObject.getJSONObject("scoring");
            int childCount = v.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = v.getChildAt(i);
                try {
                    if (view instanceof IJSONSerializable) {
                        String key = ((IJSONSerializable) view).getKey();
                        ((IJSONSerializable) view).restoreFromJSONObject(scoring.get(key));
                    } else if (view instanceof ViewGroup) {
                        restoreViewsFromJSON(json, (ViewGroup) view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Bundle b = ((IScoutingFragmentHost) getActivity()).getScoutingViewStateBundle();
        writeContentsToBundle(b, (ViewGroup) getView());
    }

    // If we have previously requested to highlight incomplete views,
    // do so when we are resumed
    @Override
    public void onResume() {
        super.onResume();
        if (shouldHighlightIncomplete) {
            isComplete(null);
        }
    }
}
