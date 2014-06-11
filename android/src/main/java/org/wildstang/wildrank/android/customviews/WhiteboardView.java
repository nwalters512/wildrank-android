package org.wildstang.wildrank.android.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.wildstang.wildrank.android.R;

import java.util.ArrayList;
import java.util.List;

public class WhiteboardView extends View {
    Paint redPaint;
    Paint bluePaint;
    Paint backgroundPaint;
    Paint buttonPaint;
    Paint selectedButtonPaint;
    Paint textPaint;
    List<Magnet> magnets = new ArrayList<>();
    Magnet activeMagnet;
    boolean magnetActive;
    Bitmap field;
    Line currentLine;
    boolean drawingLine;
    List<Line> lines = new ArrayList<>();
    Boolean penOn = false;

    public WhiteboardView(Context context) {
        super(context);
        init();
    }

    public WhiteboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WhiteboardView(Context context, AttributeSet attributes) {
        super(context, attributes);
        init();
    }

    private void init() {
        redPaint = new Paint();
        redPaint.setColor(Color.RED);

        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.light_gray));

        buttonPaint = new Paint();
        buttonPaint.setColor(getResources().getColor(R.color.light_gray));

        selectedButtonPaint = new Paint();
        selectedButtonPaint.setColor(Color.DKGRAY);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        magnets.add(new Magnet("1", R.drawable.red_robot, 1000, 200, Color.RED));
        magnets.add(new Magnet("2", R.drawable.red_robot, 1000, 300, Color.RED));
        magnets.add(new Magnet("3", R.drawable.red_robot, 1000, 400, Color.RED));
        magnets.add(new Magnet("1", R.drawable.blue_robot, 200, 200, Color.BLUE));
        magnets.add(new Magnet("2", R.drawable.blue_robot, 200, 300, Color.BLUE));
        magnets.add(new Magnet("3", R.drawable.blue_robot, 200, 400, Color.BLUE));
        magnets.add(new Magnet("", R.drawable.red_ball, 800, 300, Color.RED));
        magnets.add(new Magnet("", R.drawable.blue_ball, 400, 300, Color.BLUE));
        field = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.field);

        this.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("WrongCall")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                int action = event.getActionMasked();
                invalidate();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        checkMags(x, y);
                        if (!magnetActive) {
                            currentLine = new Line(Color.YELLOW);
                            drawingLine = true;
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (magnetActive) {
                            activeMagnet.setPosition(x, y);
                        } else {
                            if (drawingLine && penOn) {
                                currentLine.addPoint(new Point(x, y));
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (drawingLine && penOn) {
                            lines.add(currentLine);
                        }
                        drawingLine = false;
                        magnetActive = false;
                        break;
                }
                return false;
            }
        });
    }

    public void checkMags(int x, int y) {
        magnetActive = false;
        for (Magnet magnet : magnets) {
            magnetActive = magnet.checkTouch(x, y);
            if (magnetActive) {
                activeMagnet = magnet;
                break;
            }
        }
        if (x < 50 && y < 50) {
            clear();
            invalidate();
        }
        if (x > 52 && y < 50 && x < 102) {
            clear();
            magnets.clear();
            magnets.add(new Magnet("1", R.drawable.red_robot, 1000, 200, Color.RED));
            magnets.add(new Magnet("2", R.drawable.red_robot, 1000, 300, Color.RED));
            magnets.add(new Magnet("3", R.drawable.red_robot, 1000, 400, Color.RED));
            magnets.add(new Magnet("1", R.drawable.blue_robot, 200, 200, Color.BLUE));
            magnets.add(new Magnet("2", R.drawable.blue_robot, 200, 300, Color.BLUE));
            magnets.add(new Magnet("3", R.drawable.red_robot, 1000, 400, Color.RED));
            magnets.add(new Magnet("3", R.drawable.blue_robot, 200, 400, Color.BLUE));
            magnets.add(new Magnet("", R.drawable.red_ball, 800, 300, Color.RED));
            magnets.add(new Magnet("", R.drawable.blue_ball, 400, 300, Color.BLUE));
            invalidate();
        }
        if (x > 104 && x < 154 && y < 50) {
            penOn = !penOn;
        }
    }

    public void clear() {
        for (Magnet magnet : magnets) {
            magnet.clearLine();
        }
        lines.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw field background
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        canvas.drawRect(0, 0, viewWidth, viewHeight, backgroundPaint);
        canvas.drawBitmap(field, new Rect(0, 0, field.getWidth(), field.getHeight()), new Rect(0, 0, viewWidth, viewHeight), null);
        // Draw magnets
        for (Magnet magnet : magnets) {
            magnet.draw(canvas);
        }
        // Draw buttons
        canvas.drawRect(0, 0, 50, 50, buttonPaint);
        canvas.drawRect(52, 0, 102, 50, buttonPaint);
        if (penOn) {
            canvas.drawRect(104, 0, 154, 50, selectedButtonPaint);
        } else {
            canvas.drawRect(104, 0, 154, 50, buttonPaint);
        }
        canvas.drawText("Clear", 15, 15, textPaint);
        canvas.drawText("Reset", 67, 15, textPaint);
        canvas.drawText("Pen", 119, 15, textPaint);
        // Draw lines
        for (Line line : lines) {
            line.draw(canvas);
        }
        if (currentLine != null) {
            currentLine.draw(canvas);
        }

    }

    public class Magnet {
        Bitmap image;
        Paint textPaint;
        int x = 0;
        int y = 0;
        int width = 100;
        int height = 100;
        String title;
        Line line;

        public Magnet(String title, int resource, int x, int y, int color) {
            image = BitmapFactory.decodeResource(getContext().getResources(), resource);
            this.title = title;
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(20);
            line = new Line(color);
            this.x = x;
            this.y = y;
        }

        public boolean checkTouch(int x, int y) {
            if (x > this.x && y > this.y && x < this.x + width && y < this.y + height) {
                return true;
            }
            return false;
        }

        public void setPosition(int x, int y) {
            this.x = x - width / 2;
            this.y = y - height / 2;
            line.addPoint(new Point(x, y));
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void draw(Canvas canvas) {
            line.draw(canvas);
            // Scale the bitmap on the fly. If this doesn't work we can scale it beforehand
            canvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(x, y, x + width, y + height), null);
            canvas.drawText(title, x + (width / 2), y + (height / 2), textPaint);
        }

        public void clearLine() {
            line = new Line(line.getColor());
        }
    }

    private class Line {
        private List<Point> points = new ArrayList<>();
        private Paint paint;

        public Line(int color) {
            paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(7);
        }

        public int getColor() {
            return paint.getColor();
        }

        public void addPoint(Point p) {
            points.add(p);
        }

        public void draw(Canvas canvas) {
            if (points.size() > 1) {
                for (int i = 0; i < points.size(); i++) {
                    if (i < points.size() - 1) {
                        canvas.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, paint);
                    }
                }
            }
        }
    }
}
