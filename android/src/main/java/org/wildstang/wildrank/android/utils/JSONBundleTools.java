package org.wildstang.wildrank.android.utils;

import android.os.Bundle;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class JSONBundleTools {

    public static JSONObject writeBundleToJSONObject(Bundle b) {
        Set<String> keySet = b.keySet();
        JSONObject json = new JSONObject();
        try {
            for (String key : keySet) {
                Object o = b.get(key);
                if (o instanceof String) {
                    json.put(key, o);
                } else if (o instanceof Integer) {
                    json.put(key, o);
                } else if (o instanceof Boolean) {
                    json.put(key, o);
                } else if (o instanceof Double) {
                    json.put(key, o);
                } else if (o instanceof Bundle) {
                    json.put(key, writeBundleToJSONObject((Bundle) o));
                } else if (o instanceof Parcelable[]) {
                    JSONArray array = new JSONArray();
                    Parcelable[] objects = (Parcelable[]) o;
                    for (Parcelable object : objects) {
                        if (object instanceof Bundle) {
                            Bundle currentBundle = (Bundle) object;
                            array.put(writeBundleToJSONObject(currentBundle));
                        }
                    }
                    json.put(key, array);
                } else if (o instanceof String[]) {
                    JSONArray array = new JSONArray();
                    String[] strings = (String[]) o;
                    for (String string : strings) {
                        array.put(string);
                    }
                    json.put(key, array);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Bundle createHierarchicalBundle(Bundle b) {
        Bundle finalBundle = new Bundle();
        Set<String> keySet = b.keySet();
        for (String key : keySet) {
            String[] tokens = key.split("-");
            for (int i = 0; i < tokens.length; i++) {
                int currentLevel = 0;
                Bundle targetBundle = finalBundle;
                while (currentLevel != i) {
                    targetBundle = targetBundle.getBundle(tokens[currentLevel]);
                    currentLevel++;
                }
                if (i != tokens.length - 1) {
                    if (targetBundle.getBundle(tokens[i]) == null) {
                        targetBundle.putBundle(tokens[i], new Bundle());
                    }
                } else {
                    Object o = b.get(key);
                    if (o instanceof String) {
                        targetBundle.putString(tokens[i], (String) o);
                    } else if (o instanceof Integer) {
                        targetBundle.putInt(tokens[i], (Integer) o);
                    } else if (o instanceof Boolean) {
                        targetBundle.putBoolean(tokens[i], (Boolean) o);
                    } else if (o instanceof Double) {
                        targetBundle.putDouble(tokens[i], (Double) o);
                    } else if (o instanceof Parcelable[]) {
                        targetBundle.putParcelableArray(tokens[i], (Parcelable[]) o);
                    } else if (o instanceof String[]) {
                        targetBundle.putStringArray(tokens[i], (String[]) o);
                    }
                }
            }
        }
        return finalBundle;
    }

}
