package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.interfaces.ITemplatedTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nathan on 6/10/2014.
 */
public class TemplatedTextView extends TextView implements ITemplatedTextView {

    private String text;
    private String trueString, falseString;

    public TemplatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TemplatedTextView, 0, 0);
        text = a.getString(R.styleable.TemplatedTextView_text);
        trueString = a.getString(R.styleable.TemplatedTextView_trueString);
        if (trueString == null) {
            trueString = "true";
        }
        falseString = a.getString(R.styleable.TemplatedTextView_falseString);
        if (falseString == null) {
            falseString = "false";
        }
        a.recycle();
    }

    @Override
    public void populateFromData(JSONObject data) {
        // First, parse through the text
        ArrayList<String> keys = new ArrayList<>();
        // Regex that matches against keys in the form {{levels-of-json_keys}}
        // dashes denote different levels in the json hierarchy
        String patternString = "\\{\\{([0-9a-zA-Z\\-_]+)\\}\\}";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        Log.d("TemplatedTextView", "Matcher found groups: " + matcher.groupCount());

        while (matcher.find()) {
            keys.add(matcher.group(1));
        }

        for (String key : keys) {
            String replacement;
            try {
                Object o = getFromJSONWithKeyString(data, key);
                if (o instanceof Integer || o instanceof String) {
                    replacement = o.toString();
                } else if (o instanceof Double) {
                    replacement = new DecimalFormat("#.##").format(o);
                } else if (o instanceof Boolean) {
                    if (((Boolean) o) == true) {
                        replacement = trueString;
                    } else {
                        replacement = falseString;
                    }
                } else {
                    Log.d("TemplatedTextView", "Object found of type " + o.getClass().getName());
                    replacement = o.toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                replacement = "NOT FOUND";
            }
            text = text.replace("{{" + key + "}}", replacement);
        }

        setText(Html.fromHtml(text));
    }

    /**
     * Searches through a JSONObject for a specific object with a string of keys. The keys list should
     * be a list of keys representing subsequent levels of the JSON object hierarchy separated by hyphens.
     * <p/>
     * For instance, take the following JSON object:
     * <p/>
     * {
     *     "foo": {
     *         "bar":"foo"
     *         "bar2":"foo2"
     *     }
     * }
     * <p/>
     * To get the value of "bar", you would provide the key string "foo-bar".
     * <p/>
     * Note that this means that the supplied JSONObject cannot contain any keys that themselves contain a hyphen.
     *
     * @param json      a JSONObject that will be searched using the supplied string of keys
     * @param keyString A string of keys that will be used to search through the JSONObject
     * @return The object at the bottom of the JSON hierarchy
     * @throws JSONException If no object is found with the specified key
     */
    private Object getFromJSONWithKeyString(JSONObject json, String keyString) throws JSONException {
        String[] tokens = keyString.split("-");
        JSONObject currentObject = json;
        if (tokens.length == 1) {
            return currentObject.get(tokens[0]);
        }
        for (int i = 0; i < tokens.length - 1; i++) {
            currentObject = currentObject.getJSONObject(tokens[i]);
        }
        return currentObject.get(tokens[tokens.length - 1]);
    }
}
