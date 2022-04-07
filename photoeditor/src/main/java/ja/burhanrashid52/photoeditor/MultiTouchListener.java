package ja.burhanrashid52.photoeditor;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created on 18/01/2017.
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * <p></p>
 */
class MultiTouchListener implements OnTouchListener {

    private static final int INVALID_POINTER_ID = -1;
    private final GestureDetector mGestureListener;
    private boolean isRotateEnabled = true;
    private boolean isMoving;
    private boolean isTranslateEnabled = true;
    private boolean isScaleEnabled = true;
    private float minimumScale = 0.5f;
    private float maximumScale = 10.0f;

    private float minimumSize = 50f;
    private float maximumSize = 206f;

    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPrevX, mPrevY, mPrevRawX, mPrevRawY;
    private ScaleGestureDetector mScaleGestureDetector;

    private int[] location = new int[2];
    private Rect outRect;
    private View deleteView;
    private ImageView photoEditImageView;
    private PhotoEditorView parentView;

    private OnMultiTouchListener onMultiTouchListener;
    private OnGestureControl mOnGestureControl;
    private boolean mIsPinchScalable;
    private OnPhotoEditorListener mOnPhotoEditorListener;

    private final PhotoEditorViewState viewState;
    private final UndoRedoController undoRedoController;
    private GuideLineController guideLineController;

    private boolean isMovable = true;

    private float prevX = -1, prevY = -1;

    MultiTouchListener(@Nullable View deleteView,
                       PhotoEditorView parentView,
                       ImageView photoEditImageView,
                       boolean isPinchScalable,
                       OnPhotoEditorListener onPhotoEditorListener,
                       PhotoEditorViewState viewState,
                       UndoRedoController undoRedoController,
                       boolean isMovable) {
        mIsPinchScalable = isPinchScalable;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
        mGestureListener = new GestureDetector(new GestureListener());
        this.deleteView = deleteView;
        this.parentView = parentView;
        this.photoEditImageView = photoEditImageView;
        this.mOnPhotoEditorListener = onPhotoEditorListener;
        this.undoRedoController = undoRedoController;
        this.isMovable = isMovable;
        if (deleteView != null) {
            outRect = new Rect(deleteView.getLeft(), deleteView.getTop(),
                    deleteView.getRight(), deleteView.getBottom());
        } else {
            outRect = new Rect(0, 0, 0, 0);
        }
        this.viewState = viewState;
        this.guideLineController = new GuideLineController(parentView);
    }

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    private static void move(View view, TransformInfo info) {


        computeRenderOffset(view, info.pivotX, info.pivotY);
        adjustTranslation(view, info.deltaX, info.deltaY);

        TextView textView = view.findViewById(R.id.tvPhotoEditorText);
        if (textView == null) {
            float scale = view.getScaleX() * info.deltaScale;
            scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
            view.setScaleX(scale);
            view.setScaleY(scale);
        } else {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            if(params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            float scale = textView.getTextSize() * info.deltaScale;
            scale = Math.max(info.minimumSize, Math.min(info.maximumSize, scale));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scale);
        }

        float rotation = adjustAngle(view.getRotation() + info.deltaAngle);
        view.setRotation(rotation);
    }

    private static void adjustTranslation(View view, float deltaX, float deltaY) {
        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);
    }

    private static void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() == pivotX && view.getPivotY() == pivotY) {
            return;
        }

        float[] prevPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(prevPoint);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        float[] currPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(currPoint);

        float offsetX = currPoint[0] - prevPoint[0];
        float offsetY = currPoint[1] - prevPoint[1];

        view.setTranslationX(view.getTranslationX() - offsetX);
        view.setTranslationY(view.getTranslationY() - offsetY);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(view, event);
        mGestureListener.onTouchEvent(event);

        if (!isTranslateEnabled) {
            return true;
        }

        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (action & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                mActivePointerId = event.getPointerId(0);
                if (deleteView != null) {
                    deleteView.setVisibility(View.VISIBLE);
                }
                prevX = view.getX();
                prevY = view.getY();
                firePhotoEditorSDKListener(view, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // Only enable dragging on focused stickers.
                if (view == viewState.getCurrentSelectedView() && isMovable) {
                    isMoving = true;
                    int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                    if (pointerIndexMove != -1) {
                        float currX = event.getX(pointerIndexMove);
                        float currY = event.getY(pointerIndexMove);
                        if (!mScaleGestureDetector.isInProgress()) {
                            adjustTranslation(view, currX - mPrevX, currY - mPrevY);
                        }
                    }
                    guideLineController.handleGuidelines(view);
                    guideLineController.snappingHorizontalViews(view);
                    guideLineController.snappingVerticalViews(view);

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                mActivePointerId = INVALID_POINTER_ID;
                if (deleteView != null && isViewInBounds(deleteView, x, y)) {
                    if (onMultiTouchListener != null)
                        onMultiTouchListener.onRemoveViewListener(view);
                } else if (!isViewInBounds(photoEditImageView, x, y)) {
                    //view.animate().translationY(0).translationY(0);
                }
                if (deleteView != null) {
                    deleteView.setVisibility(View.GONE);
                }
                if ((prevX != view.getX() && prevY != view.getY())) {
                    firePhotoEditorSDKListener(view, false);
                }
                parentView.setVisibilityOfGuideLines(GuidelineVisibility.NONE);
                VirtualView virtualView = null;
               /* if (undoRedoController.getFreqOfItemInAddedViews(view.getTag().toString()) == 1) {
                    virtualView = undoRedoController.getVirtualViewFromAddedViews(view.getTag().toString());
                }*/
               /* if (prevX != -1 && prevY != -1 && (prevX == view.getX() && prevY == view.getY()) ||
                        (virtualView != null && virtualView.x == view.getX() && virtualView.y == view.getY())) {
                    //Dont add
                } else {
                   // undoRedoController.addAddedView(view);
                }*/
                prevX = view.getX();
                prevY = view.getY();
                guideLineController.hideAllGuidelines();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndexPointerUp = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndexPointerUp);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndexPointerUp == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }
        return true;
    }

    private void firePhotoEditorSDKListener(View view, boolean isStart) {
        Object viewTag = view.getTag();
        if (mOnPhotoEditorListener != null && viewTag != null ) {
            if (isStart)
                mOnPhotoEditorListener.onStartViewChangeListener(view.getTag().toString());
            else
                mOnPhotoEditorListener.onStopViewChangeListener(view.getTag().toString());
        }
    }

    private boolean isViewInBounds(View view, int x, int y) {
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    void setOnMultiTouchListener(OnMultiTouchListener onMultiTouchListener) {
        this.onMultiTouchListener = onMultiTouchListener;
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mPivotX;
        private float mPivotY;
        private Vector2D mPrevSpanVector = new Vector2D();
        private static final int SPAN_SLOP = 7;


        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            mOnPhotoEditorListener.onTransformationStarted();
            return mIsPinchScalable;
        }

        private boolean gestureTolerance(@NonNull ScaleGestureDetector detector) {
            final float spanDelta = Math.abs(detector.getCurrentSpan() - detector.getPreviousSpan());
            return spanDelta > SPAN_SLOP;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            if(gestureTolerance(detector)) {
                TransformInfo info = new TransformInfo();
                info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
                info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
                info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
                info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
                info.pivotX = mPivotX;
                info.pivotY = mPivotY;
                info.minimumScale = minimumScale;
                info.maximumScale = maximumScale;
                info.minimumSize = minimumSize;
                info.maximumSize = maximumSize;
                move(view, info);
            }
            return !mIsPinchScalable;
        }

        @Override
        public void onScaleEnd(View view, ScaleGestureDetector detector) {
            super.onScaleEnd(view, detector);
            mOnPhotoEditorListener.onTransformationEnd();
        }
    }

    private class TransformInfo {
        float deltaX;
        float deltaY;
        float deltaScale;
        float deltaAngle;
        float pivotX;
        float pivotY;
        float minimumScale;
        float maximumScale;
        float minimumSize;
        float maximumSize;
    }

    interface OnMultiTouchListener {
        void onEditTextClickListener(String text, int colorCode);

        void onRemoveViewListener(View removedView);
    }

    interface OnGestureControl {
        void onClick();

        void onLongClick();
    }

    void setOnGestureControl(OnGestureControl onGestureControl) {
        mOnGestureControl = onGestureControl;
    }

    OnGestureControl getOnGestureControl() {
        return mOnGestureControl;
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnGestureControl != null) {
                mOnGestureControl.onClick();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            if (mOnGestureControl != null) {
                if (!isMoving) {
                    mOnGestureControl.onLongClick();
                }
            }
        }
    }
}