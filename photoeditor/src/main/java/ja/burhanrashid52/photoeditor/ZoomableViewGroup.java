package ja.burhanrashid52.photoeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ZoomableViewGroup extends ViewGroup {

    private boolean doubleTap = false;

    private boolean isEnabledZooming = true;


    private final float MIN_ZOOM = 1f;
    private final float MAX_ZOOM = 20f;
    private final float[] topLeftCorner = {0, 0};
    private float scaleFactor;

    // States.
    public static final byte NONE = 0;
    public static final byte DRAG = 1;
    public static final byte ZOOM = 2;
    private static byte y =3;

    private byte mode = NONE;
    private static final long TOTAL_DOWN_TIME = 250L;
    private static final float MINIMUM_DISPLACEMENT = 40.f;

    // Matrices used to move and zoom image.
    private Matrix matrix = new Matrix();
    private Matrix matrixInverse = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // Parameters for zooming.
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float[] lastEvent = null;
    private long lastDownTime = 0L;
    private long downTime = 0L;

    private float[] mDispatchTouchEventWorkingArray = new float[2];
    private float[] mOnTouchEventWorkingArray = new float[2];

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDispatchTouchEventWorkingArray[0] = ev.getX();
        mDispatchTouchEventWorkingArray[1] = ev.getY();
        screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
        ev.setLocation(mDispatchTouchEventWorkingArray[0], mDispatchTouchEventWorkingArray[1]);
        return super.dispatchTouchEvent(ev);
    }

    public ZoomableViewGroup(Context context) {
        super(context);
    }

    public ZoomableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomableViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float[] scaledPointsToScreenPoints(float[] a) {
        matrix.mapPoints(a);
        return a;
    }

    private float[] screenPointsToScaledPoints(float[] a) {
        matrixInverse.mapPoints(a);
        return a;
    }


    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        float[] values = new float[9];
        matrix.getValues(values);
        canvas.save();
        canvas.translate(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]);
        canvas.scale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);
        topLeftCorner[0] = values[Matrix.MTRANS_X];
        topLeftCorner[1] = values[Matrix.MTRANS_Y];
        scaleFactor = values[Matrix.MSCALE_X];
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public void dispatchTouch(MotionEvent even) {
        mDispatchTouchEventWorkingArray[0] = even.getX();
        mDispatchTouchEventWorkingArray[1] = even.getY();
        screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
        even.setLocation(mDispatchTouchEventWorkingArray[0], mDispatchTouchEventWorkingArray[1]);
        onTouchEvent(even);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // handle touch events here
        mOnTouchEventWorkingArray[0] = event.getRawX();
        mOnTouchEventWorkingArray[1] = event.getRawY();

        scaledPointsToScreenPoints(mOnTouchEventWorkingArray);

        event.setLocation(mOnTouchEventWorkingArray[0], mOnTouchEventWorkingArray[1]);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                mode = DRAG;
                lastEvent = null;
                downTime = SystemClock.elapsedRealtime();
                if (downTime - lastDownTime < TOTAL_DOWN_TIME) {
                    doubleTap = true;
                    float density = getResources().getDisplayMetrics().density;
                    if (Math.max(Math.abs(start.x - event.getRawX()), Math.abs(start.y - event.getY())) < MINIMUM_DISPLACEMENT * density) {
                        savedMatrix.set(matrix);                                                         //repetition of savedMatrix.setmatrix
                        mid.set(event.getRawX(), event.getY());
                        mode = ZOOM;
                        lastEvent = new float[4];
                        lastEvent[0] = lastEvent[1] = event.getRawX();
                        lastEvent[2] = lastEvent[3] = event.getRawY();
                    }
                    lastDownTime = 0L;
                } else {
                    doubleTap = false;
                    lastDownTime = downTime;
                }
                start.set(event.getRawX(), event.getRawY());

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                break;
            case MotionEvent.ACTION_UP:


                if (doubleTap && scaleFactor < 1.8f){
                    matrix.postScale(2.5f/scaleFactor, 2.5f/scaleFactor, mid.x, mid.y);
                } else if(doubleTap && scaleFactor >= 1.8f){
                    matrix.postScale(1.0f/scaleFactor, 1.0f/scaleFactor, mid.x, mid.y);
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if(topLeftCorner[0] >= 0){
                        matrix.postTranslate(-topLeftCorner[0],0);
                    } else if (topLeftCorner[0] < -getWidth()*(scaleFactor-1)){
                        matrix.postTranslate((-topLeftCorner[0]) - getWidth()*(scaleFactor-1) ,0);
                    }
                    if(topLeftCorner[1] >= 0){
                        matrix.postTranslate(0,-topLeftCorner[1]);
                    } else if (topLeftCorner[1] < -getHeight()*(scaleFactor-1)){
                        matrix.postTranslate(0,(-topLeftCorner[1]) - getHeight()*(scaleFactor-1));
                    }
                    matrix.invert(matrixInverse);
                    invalidate();
                }, 1);

                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:

                final float density = getResources().getDisplayMetrics().density;
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getRawX() - start.x;
                    float dy = event.getRawY() - start.y;
                    matrix.postTranslate(dx, dy);
                    matrix.invert(matrixInverse);
                    if (Math.max(Math.abs(start.x - event.getRawX()), Math.abs(start.y - event.getRawY())) > 20.f * density) {
                        lastDownTime = 0L;
                    }
                } else if (mode == ZOOM) {
                    if(!isEnabledZooming) {
                        return true;
                    }
                    if (event.getPointerCount() > 1) {
                        float newDist = spacing(event);
                        if (newDist > 10f * density) {
                            matrix.set(savedMatrix);
                            float scale = (newDist / oldDist);
                            float[] values = new float[9];
                            matrix.getValues(values);
                            if (scale * values[Matrix.MSCALE_X] >= MAX_ZOOM) {
                                scale = MAX_ZOOM / values[Matrix.MSCALE_X];
                            }
                            if (scale * values[Matrix.MSCALE_X] <= MIN_ZOOM) {
                                scale = MIN_ZOOM / values[Matrix.MSCALE_X];
                            }
                            matrix.postScale(scale, scale, mid.x, mid.y);
                            matrix.invert(matrixInverse);
                        }
                    } else {
                        if ( SystemClock.elapsedRealtime() - downTime > TOTAL_DOWN_TIME) {
                            doubleTap = false;
                        }
                        matrix.set(savedMatrix);
                        float scale = event.getY() / start.y;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        if (scale * values[Matrix.MSCALE_X] >= MAX_ZOOM) {
                            scale = MAX_ZOOM / values[Matrix.MSCALE_X];
                        }
                        if (scale * values[Matrix.MSCALE_X] <= MIN_ZOOM) {
                            scale = MIN_ZOOM / values[Matrix.MSCALE_X];
                        }
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        matrix.invert(matrixInverse);
                    }
                }
                break;
        }

        invalidate();
        return true;
    }

    public void reset(){
        matrix = new Matrix();
        matrixInverse = new Matrix();
        savedMatrix = new Matrix();
        scaleFactor = 1;
        invalidate();
    }

    public boolean isEnabledZooming() {
        return isEnabledZooming;
    }

    public void setEnabledZooming(boolean enabledZooming) {
        isEnabledZooming = enabledZooming;
    }
}