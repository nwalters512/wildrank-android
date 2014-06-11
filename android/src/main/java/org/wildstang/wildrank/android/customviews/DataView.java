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

    @Override
    public void populateFromData(List<JSONObject> data) {
        // First, iterate through the expression and replace all functions
        // such as AVERAGE and SUM with their evaluated outputs
        Log.d("populateFromData", "expression: " + expression);
        if (data.size() != 0) {
            while (expression.contains("AVERAGE")) {
                int startOfAverageIndex = expression.indexOf("AVERAGE");
                int endOfAverageIndex = expression.indexOf(")", startOfAverageIndex);
                Log.d("populateFromData", "AVERAGE expression: " + expression.subSequence(startOfAverageIndex, endOfAverageIndex + 1));
                String key = expression.substring(expression.indexOf("(", startOfAverageIndex) + 1, endOfAverageIndex);
                Log.d("populateFromData", "key: " + key);
                String[] tokens = key.split("-");
                double sum = 0;
                int count = 0;
                Log.d("calculate", "json size: " + data.size());
                for (JSONObject json : data) {
                    count++;
                    JSONObject currentObject = json;
                    try {
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
                expression = expression.replace(expression.substring(startOfAverageIndex, endOfAverageIndex + 1), "" + average);
            }

            try {
                Log.d("calculate", "expression: " + expression);
                Calculable calc = new ExpressionBuilder(expression).build();
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
