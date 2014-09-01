package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;
import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.interfaces.IDataView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;

/**
 * A view used to display stored match data. A typical use cas would be constructing a list of averages of a team's
 * performances in matches, for instance average points scored or fouls made.
 *
 * Configuration for this class is done via attributes in the XML layout files. The "label" parameter
 * is a text string used to label the calculated data, and the "expression" attribute describes how this
 * view should calculate its displayed value.
 *
 * "expression" should be a standard mathematical formula; we use exp4j to parse and calculate it. The function
 * AVERAGE() is supported to make calculating averages over all matches possible. You calculate the average of a piece
 * of data as AVERAGE(key), with key being a descriptor of where the desired value is within the JSON hierarchy. It should
 * be a hyphen-separated list of sequential keys that are used to drill down into the hierarchy.
 *
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
 *
 * An example "expression" might look something like this:
 *
 * "10*AVERAGE(scoring-teleop-swag) - 5*AVERAGE(scoring-teleop-antiswag)"
 */
public class DataView extends RelativeLayout implements IDataView {

    private TextView labelView;
    private TextView valueView;

    private int format;

    private String expression;

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_view_data_view, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DataView, 0, 0);
        String label = a.getString(R.styleable.DataView_label);
        expression = a.getString(R.styleable.DataView_expression);
        // Default format is double
        format = a.getInt(R.styleable.DataView_format, 0);
        a.recycle();

        labelView = (TextView) findViewById(R.id.label);
        labelView.setText(label);

        valueView = (TextView) findViewById(R.id.value);
    }

    /**
     * This method will be called once the hosting IDataHost has data available. Data is loaded asynchronously
     * in another thread in order to prevent performance issues.
     *
     * @param data a List of JSONObjects containing the data this view should use.
     */
    @Override
    synchronized public void populateFromData(List<JSONObject> data) {
        // First, iterate through the expression and replace all functions
        // such as AVERAGE with their evaluated outputs
        Log.d("populateFromData", "expression: " + expression);

        // We use a copy of the expression to preserve the original one in case we need to recalculate
        String expressionToManipulate = expression;
        if (data.size() != 0) {
            while (expressionToManipulate.contains("AVERAGE")) {
                int startOfAverageIndex = expressionToManipulate.indexOf("AVERAGE");
                int endOfAverageIndex = expressionToManipulate.indexOf(")", startOfAverageIndex);
                Log.d("populateFromData", "AVERAGE expression: " + expression.subSequence(startOfAverageIndex, endOfAverageIndex + 1));
                String key = expressionToManipulate.substring(expressionToManipulate.indexOf("(", startOfAverageIndex) + 1, endOfAverageIndex);
                Log.d("populateFromData", "key: " + key);
                String[] tokens = key.split("-");
                double sum = 0;
                int count = 0;
                Log.d("calculate", "json size: " + data.size());
                for (JSONObject json : data) {
                    count++;
                    JSONObject currentObject = json;
                    try {
                        // Drill down through the JSON object to find the desired value.
                        for (int i = 0; i < tokens.length - 1; i++) {
                            currentObject = currentObject.getJSONObject(tokens[i]);
                        }
                        Object o = currentObject.get(tokens[tokens.length - 1]);
                        if (o instanceof Integer) {
                            sum += (Integer) o;
                        } else if (o instanceof Double) {
                            sum += (Double) o;
                        } else if (o instanceof Boolean) {
                            if (((Boolean) o) == true) {
                                sum += 1;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                double average = sum / count;
                Log.d("calculate", "sum: " + sum);
                expressionToManipulate = expressionToManipulate.replace(expressionToManipulate.substring(startOfAverageIndex, endOfAverageIndex + 1), "" + average);
            }

            try {
                Log.d("calculate", "expression: " + expressionToManipulate);
                Calculable calc = new ExpressionBuilder(expressionToManipulate).build();
                double result = calc.calculate();
                String resultString;
                switch (format) {
                    case 0:
                        //Double format
                        resultString = new DecimalFormat("#.##").format(result);
                        break;
                    case 1:
                        // Integer
                        resultString = String.valueOf((int) result);
                        break;
                    case 2:
                        NumberFormat format = NumberFormat.getPercentInstance();
                        format.setMaximumFractionDigits(2);
                        resultString = format.format(result);
                        break;
                    default:
                        resultString = "Invalid number format in XML file";
                        break;
                }
                Log.d("calculate", String.valueOf(result));
                valueView.setText(resultString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            valueView.setText("No data");
        }
    }
}
