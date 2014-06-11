package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.interfaces.IJSONSerializable;

import java.util.StringTokenizer;

public class FieldPositionView extends View implements IJSONSerializable {

    private Paint linePaint;
    private Paint backgroundFillPaint;
    private Paint boxFillPaint;
    private Paint checkedBoxFillPaint;
    private Paint textPaint;

    private GestureDetector detector;

    private RectF backgroundRect;
    private Rect textRect;
    private RectF[] zoneRects = new RectF[3];
    private boolean[] zoneStates = new boolean[3];

    private String key;

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("onDown", "down");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d("onShow", "show pressed");
            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d("onTap", "tapped!");
            for (int i = 0; i < zoneRects.length; i++) {
                if (isInRectangle(e.getX(), e.getY(), zoneRects[i])) {
                    zoneStates[i] = !zoneStates[i];
                    invalidate();
                    playSoundEffect(SoundEffectConstants.CLICK);
                    return true;
                }
            }
            return false;
        }

        private boolean isInRectangle(float x, float y, RectF rect) {
            return (x > rect.left && x < rect.right && y > rect.top && y < rect.bottom);
        }
    }

    public FieldPositionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
        key = a.getString(R.styleable.JSONSerializableView_key);
        a.recycle();

        init();
    }

    public FieldPositionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JSONSerializableView, 0, 0);
        key = a.getString(R.styleable.JSONSerializableView_key);
        a.recycle();

        init();
    }

    public FieldPositionView(Context context) {
        super(context);

        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.black));

        backgroundFillPaint = new Paint();
        backgroundFillPaint.setColor(getResources().getColor(R.color.white));
        backgroundFillPaint.setAlpha(255);

        boxFillPaint = new Paint();
        boxFillPaint.setColor(getResources().getColor(R.color.light_gray));
        boxFillPaint.setAlpha(255);

        checkedBoxFillPaint = new Paint();
        checkedBoxFillPaint.setColor(getResources().getColor(R.color.dark_gray));
        checkedBoxFillPaint.setAlpha(255);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.black));
        textPaint.setTextSize(15);

        for (int i = 0; i < zoneStates.length; i++) {
            zoneStates[i] = false;
        }

        detector = new GestureDetector(this.getContext(), new GestureListener());

        textRect = new Rect();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("onTouch", "touched!");
        return detector.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        backgroundRect = new RectF(0, 0, w, h);

        textPaint.getTextBounds("Goal", 0, "Goal".length(), textRect);
        float goalTextHeight = textRect.height() + 10;
        textPaint.getTextBounds("Truss", 0, "Truss".length(), textRect);
        float trussTextHeight = textRect.height() + 10;
        float availableHeight = h - (goalTextHeight + trussTextHeight);

        zoneRects[0] = new RectF(0, goalTextHeight, w, goalTextHeight + availableHeight / 3);
        zoneRects[1] = new RectF(0, zoneRects[0].bottom, w, zoneRects[0].bottom + availableHeight / 3);
        zoneRects[2] = new RectF(0, zoneRects[1].bottom, w, zoneRects[1].bottom + availableHeight / 3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Fill background
        canvas.drawRect(backgroundRect, backgroundFillPaint);

        //Draw labels
        textPaint.getTextBounds("Goal", 0, "Goal".length(), textRect);
        float textWidth = textRect.width();
        float textHeight = textRect.height();
        canvas.drawText("Goal", (viewWidth / 2) - (textWidth / 2), textHeight, textPaint);
        textPaint.getTextBounds("Truss", 0, "Truss".length(), textRect);
        textWidth = textRect.width();
        textHeight = textRect.height();
        canvas.drawText("Truss", (viewWidth / 2) - (textWidth / 2), viewHeight, textPaint);

        //Draw boxes and box labels
        for (int i = 0; i < zoneRects.length; i++) {
            String boxLabel = "" + (i + 1);
            textPaint.getTextBounds(boxLabel, 0, boxLabel.length(), textRect);
            textWidth = textRect.width();
            textHeight = textRect.height();
            float textX = zoneRects[i].left + (zoneRects[i].width() / 2) - (textWidth / 2);
            float textY = zoneRects[i].top + (zoneRects[i].height() / 2) + (textHeight / 2);
            if (zoneStates[i] == true) {
                canvas.drawRect(zoneRects[i], checkedBoxFillPaint);
            } else {
                canvas.drawRect(zoneRects[i], boxFillPaint);
            }
            canvas.drawText(boxLabel, textX, textY, textPaint);
        }

        //Draw dividing lines
        canvas.drawLine(0, zoneRects[0].bottom, viewWidth, zoneRects[0].bottom, linePaint);
        canvas.drawLine(0, zoneRects[1].bottom, viewWidth, zoneRects[1].bottom, linePaint);
    }

    @Override
    public void writeContentsToBundle(Bundle b) {
        StringBuilder zones = new StringBuilder();
        boolean firstIteration = true;
        for (int i = 0; i < zoneStates.length; i++) {
            if (zoneStates[i] == true) {
                if (firstIteration) {
                    zones.append(i + 1);
                    firstIteration = false;
                } else {
                    zones.append(',').append(i + 1);
                }
            }
        }
        b.putString(key, zones.toString());
    }

    @Override
    public void restoreFromJSONObject(Object object) {
        if (object instanceof String) {
            for (int i = 0; i < zoneStates.length; i++) {
                zoneStates[i] = false;
            }
            StringTokenizer tokenizer = new StringTokenizer((String) object, ",");
            while (tokenizer.hasMoreTokens()) {
                zoneStates[Integer.parseInt(tokenizer.nextToken())] = true;
            }
        }
        invalidate();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isComplete(boolean highlight) {
        return true;
    }

}
