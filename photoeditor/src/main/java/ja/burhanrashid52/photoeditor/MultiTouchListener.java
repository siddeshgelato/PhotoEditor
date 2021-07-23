package ja.burhanrashid52.photoeditor;

import android.graphics.Color;
import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

    private PhotoEditorViewState viewState;
    private UndoRedoController undoRedoController;

    private View horizontalTopView;
    private View horizontalMiddleView;
    private View horizontalBottomView;
    private View horizontalMiddleTopView;
    private View horizontalMiddleMiddleView;
    private View horizontalMiddleBottomView;
    private View horizontalBottomTopView;
    private View horizontalBottomMiddleView;
    private View horizontalBottomBottomView;

    private View verticalTopView;
    private View verticalMiddleView;
    private View verticalBottomView;
    private View verticalMiddleTopView;
    private View verticalMiddleMiddleView;
    private View verticalMiddleBottomView;
    private View verticalBottomTopView;
    private View verticalBottomMiddleView;
    private View verticalBottomBottomView;

    float prevX = -1, prevY = -1;

    MultiTouchListener(@Nullable View deleteView,
                       PhotoEditorView parentView,
                       ImageView photoEditImageView,
                       boolean isPinchScalable,
                       OnPhotoEditorListener onPhotoEditorListener,
                       PhotoEditorViewState viewState,
                       UndoRedoController undoRedoController
    ) {
        mIsPinchScalable = isPinchScalable;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
        mGestureListener = new GestureDetector(new GestureListener());
        this.deleteView = deleteView;
        this.parentView = parentView;
        this.photoEditImageView = photoEditImageView;
        this.mOnPhotoEditorListener = onPhotoEditorListener;
        this.undoRedoController = undoRedoController;
        if (deleteView != null) {
            outRect = new Rect(deleteView.getLeft(), deleteView.getTop(),
                    deleteView.getRight(), deleteView.getBottom());
        } else {
            outRect = new Rect(0, 0, 0, 0);
        }
        this.viewState = viewState;
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

        float scale = view.getScaleX() * info.deltaScale;
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
        view.setScaleX(scale);
        view.setScaleY(scale);

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
                view.bringToFront();
                firePhotoEditorSDKListener(view, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // Only enable dragging on focused stickers.
                if (view == viewState.getCurrentSelectedView()) {
                    isMoving = true;
                    int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                    if (pointerIndexMove != -1) {
                        float currX = event.getX(pointerIndexMove);
                        float currY = event.getY(pointerIndexMove);
                        if (!mScaleGestureDetector.isInProgress()) {
                            adjustTranslation(view, currX - mPrevX, currY - mPrevY);
                        }
                    }
                    handleGuidelines(view);
                    snapingHorizontalViews(view);
                    snapingVerticalViews(view);

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
                    view.animate().translationY(0).translationY(0);
                }
                if (deleteView != null) {
                    deleteView.setVisibility(View.GONE);
                }
                firePhotoEditorSDKListener(view, false);
                parentView.setVisibilityOfGuideLines(GuidelineVisibility.NONE);
                VirtualView virtualView = null;
                if (undoRedoController.getFreqOfItemInAddedViews(view.getTag().toString()) == 1) {
                    virtualView = undoRedoController.getVirtualViewFromAddedViews(view.getTag().toString());
                }
                if (prevX != -1 && prevY != -1 && (prevX == view.getX() && prevY == view.getY()) ||
                        (virtualView != null && virtualView.x == view.getX() && virtualView.y == view.getY())) {
                    //Dont add
                } else {
                    undoRedoController.addAddedView(view);
                }
                prevX = view.getX();
                prevY = view.getY();
                hideAllGuidelines();
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

    private void snapingVerticalViews(View view) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(view.getX()) >= Math.round((child.getX() - 10)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + 10))) {
                    view.setX(child.getX());
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, false);
                }

              if (Math.round(view.getX()) >= Math.round((child.getX() + (child.getWidth() / 2.0) - 10)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + (child.getWidth() / 2.0) + 10))) {
                    view.setX(child.getX() + (child.getWidth() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                }

                 if (Math.round(view.getX()) >= Math.round((child.getX() + (child.getWidth()) - 10)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + (child.getWidth()) + 10))) {
                    view.setX(child.getX() + (child.getWidth()));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                }

                 if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() - 10)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + 10))) {
                    view.setX(child.getX() - (child.getWidth() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                }

               if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() + (child.getWidth() / 2.0) - 10)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + (child.getWidth() / 2.0) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                }

                if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() + (child.getWidth()) - 10)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + (child.getWidth()) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                }

                 if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() - 10)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + 10))) {
                    view.setX(child.getX() - child.getWidth());
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                }

               if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() + (child.getWidth() / 2.0f) - 10)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + (child.getWidth() / 2.0f) + 10))) {
                    view.setX(child.getX() - (view.getWidth() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                }

                 if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() + (child.getWidth()) - 5)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + (child.getWidth()) + 5))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                }
            }
        }
    }

    private void hideAllGuidelines() {
        removeLine(SnappingViewGuidelinePosition.TOP_TOP, true);
        removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, true);
        removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, true);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, true);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, true);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
        removeLine(SnappingViewGuidelinePosition.TOP_TOP, false);
        removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, false);
        removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, false);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, false);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
        removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, false);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
        removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
    }

    private void snapingHorizontalViews(View view) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(view.getY()) >= Math.round((child.getY() - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + 10))) {
                    view.setY(child.getY());
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, true);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight() / 2.0) - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight() / 2.0) + 10))) {
                    view.setY(child.getY() + (child.getHeight() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight()) - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight()) + 10))) {
                    view.setY(child.getY() + (child.getHeight()));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + 10))) {
                    view.setY(child.getY() - (child.getHeight() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight() / 2.0) - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight() / 2.0) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight()) - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight()) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() - 10)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + 10))) {
                    view.setY(child.getY() - child.getHeight());
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight() / 2.0f) - 10)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight() / 2.0f) + 10))) {
                    view.setY(child.getY() - (view.getHeight() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight()) - 5)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight()) + 5))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
                }
            }
        }
    }

    private void removeLine(SnappingViewGuidelinePosition position, boolean isHorizontal) {
        switch (position) {
            case TOP_TOP:
                if (isHorizontal)
                    setVisibilityGone(horizontalTopView);
                else setVisibilityGone(verticalTopView);
                break;
            case TOP_MIDDLE:
                if (isHorizontal)
                    setVisibilityGone(horizontalMiddleView);
                else setVisibilityGone(verticalMiddleView);
                break;
            case TOP_BOTTOM:
                if (isHorizontal)
                    setVisibilityGone(horizontalBottomView);
                else setVisibilityGone(verticalBottomView);

                break;
            case MIDDLE_TOP:
                if (isHorizontal)
                    setVisibilityGone(horizontalMiddleTopView);
                else setVisibilityGone(verticalMiddleTopView);
                break;
            case MIDDLE_MIDDLE:
                if (isHorizontal)
                    setVisibilityGone(horizontalMiddleMiddleView);
                else setVisibilityGone(verticalMiddleMiddleView);
                break;
            case MIDDLE_BOTTOM:
                if (isHorizontal)
                    setVisibilityGone(horizontalMiddleBottomView);
                else setVisibilityGone(verticalMiddleBottomView);
                break;
            case BOTTOM_TOP:
                if (isHorizontal)
                    setVisibilityGone(horizontalBottomTopView);
                else setVisibilityGone(verticalBottomTopView);
                break;
            case BOTTOM_MIDDLE:
                if (isHorizontal)
                    setVisibilityGone(horizontalBottomMiddleView);
                else setVisibilityGone(verticalBottomMiddleView);
                break;
            case BOTTOM_BOTTOM:
                if (isHorizontal)
                    setVisibilityGone(horizontalBottomBottomView);
                else setVisibilityGone(verticalBottomBottomView);
                break;
        }

    }

    private void drawLine(View view, View child, SnappingViewGuidelinePosition position, boolean isHorizontal) {
        int heightWidth = 0;
        if (isHorizontal) {
            if (view.getX() < child.getX()) {
                heightWidth = (int) ((child.getX() + child.getWidth()) - view.getX());
            } else {
                heightWidth = (int) ((view.getX() + view.getWidth()) - child.getX());
            }
        } else {
            if(view.getY() < child.getY()) {
                heightWidth = (int) ((child.getY() + child.getHeight()) - view.getY());
            } else {
                heightWidth = (int) ((view.getY() + view.getHeight()) - child.getY());
            }
        }

        switch (position) {
            case TOP_TOP:
                if (isHorizontal) {
                    if (horizontalTopView == null) {
                        horizontalTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalTopView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalTopView, heightWidth, view.getX(), child.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                } else {
                    if (verticalTopView == null) {
                        verticalTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalTopView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalTopView, heightWidth, child.getX(), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                }
                break;
            case TOP_MIDDLE:
                if(isHorizontal) {
                    if (horizontalMiddleView == null) {
                        horizontalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalMiddleView, heightWidth, view.getX(), child.getY() + (child.getHeight() / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleView, heightWidth, child.getX(), child.getY() + (child.getHeight() / 2.0f), isHorizontal);
                    }
                } else {
                    if (verticalMiddleView == null) {
                        verticalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalMiddleView, heightWidth, child.getX() + child.getWidth()/2.0f, view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleView, heightWidth, child.getX() + child.getWidth()/2.0f, child.getY(), isHorizontal);
                    }
                }
                break;
            case TOP_BOTTOM:
                if(isHorizontal) {
                    if (horizontalBottomView == null) {
                        horizontalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalBottomView, (int) heightWidth, view.getX(), child.getY() + child.getHeight(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomView, (int) heightWidth, child.getX(), child.getY() + child.getHeight(), isHorizontal);
                    }
                } else {
                    if (verticalBottomView == null) {
                        verticalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalBottomView, (int) heightWidth, child.getX() + child.getWidth(), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomView, (int) heightWidth, child.getX() + child.getWidth(), child.getY(), isHorizontal);
                    }
                }
                break;
            case MIDDLE_TOP:
                if(isHorizontal) {
                    if (horizontalMiddleTopView == null) {
                        horizontalMiddleTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleTopView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalMiddleTopView, heightWidth, view.getX(), view.getY() + (view.getHeight() / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                } else {
                    if (verticalMiddleTopView == null) {
                        verticalMiddleTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleTopView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalMiddleTopView, heightWidth, view.getX() + (view.getWidth() / 2.0f), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                }
                break;

            case MIDDLE_MIDDLE:
                if(isHorizontal) {
                    if (horizontalMiddleMiddleView == null) {
                        horizontalMiddleMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleMiddleView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalMiddleMiddleView, heightWidth, view.getX(), view.getY() + (view.getHeight() / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleMiddleView, heightWidth, child.getX(), child.getY() + (child.getHeight() / 2.0f), isHorizontal);
                    }
                } else {
                    if (verticalMiddleMiddleView == null) {
                        verticalMiddleMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleMiddleView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, view.getX() + (view.getWidth() / 2.0f),view.getY() , isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, child.getX() + (child.getWidth() / 2.0f),child.getY() , isHorizontal);
                    }
                }
                break;
            case MIDDLE_BOTTOM:
                if(isHorizontal) {
                    if (horizontalMiddleBottomView == null) {
                        horizontalMiddleBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleBottomView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalMiddleBottomView, heightWidth, view.getX(), view.getY() + (view.getHeight() / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleBottomView, heightWidth, child.getX(), child.getY() + (child.getHeight()), isHorizontal);
                    }
                } else {
                    if (verticalMiddleBottomView == null) {
                        verticalMiddleBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleBottomView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalMiddleBottomView, heightWidth, view.getX() + (view.getWidth() / 2.0f), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleBottomView, heightWidth, child.getX() + (child.getWidth()), child.getY(), isHorizontal);
                    }
                }
                break;

            case BOTTOM_TOP:
                if(isHorizontal) {
                    if (horizontalBottomTopView == null) {
                        horizontalBottomTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomTopView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalBottomTopView, heightWidth, view.getX(), view.getY() + (view.getHeight()), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                } else {
                    if (verticalBottomTopView == null) {
                        verticalBottomTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomTopView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalBottomTopView, heightWidth, view.getX() + (view.getWidth()), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomTopView, heightWidth, child.getX(), child.getY(), isHorizontal);
                    }
                }
                break;

            case BOTTOM_MIDDLE:
                if(isHorizontal) {
                    if (horizontalBottomMiddleView == null) {
                        horizontalBottomMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomMiddleView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalBottomMiddleView, heightWidth, view.getX(), view.getY() + (view.getHeight()), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomMiddleView, heightWidth, child.getX(), child.getY() + (child.getHeight() / 2.0f), isHorizontal);
                    }
                } else {
                    if (verticalBottomMiddleView == null) {
                        verticalBottomMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomMiddleView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth,view.getX() + (view.getWidth()),view.getY() , isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth,child.getX() + (child.getWidth() / 2.0f), child.getY(), isHorizontal);
                    }
                }
                break;

            case BOTTOM_BOTTOM:
                if(isHorizontal) {
                    if (horizontalBottomBottomView == null) {
                        horizontalBottomBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomBottomView, isHorizontal);
                    if (view.getX() < child.getX()) {
                        setCalculatedValuesForGuideView(horizontalBottomBottomView, heightWidth, view.getX(), view.getY() + (view.getHeight()), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomBottomView, heightWidth, child.getX(), child.getY() + (child.getHeight()), isHorizontal);
                    }
                } else {
                    if (verticalBottomBottomView == null) {
                        verticalBottomBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomBottomView, isHorizontal);
                    if (view.getY() < child.getY()) {
                        setCalculatedValuesForGuideView(verticalBottomBottomView, heightWidth, view.getX() + (view.getWidth()), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomBottomView, heightWidth, child.getX() + (child.getWidth()), child.getY(), isHorizontal);
                    }
                }
                break;
        }
    }

    private void setVisibilityGone(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    private void setSnappingGuidelineUIProperties(View view, boolean isHorizontal) {
        if(isHorizontal) {
            view.setBackground(ContextCompat.getDrawable(parentView.getContext(), R.drawable.doted));
        } else {
            //view.setBackgroundColor(Color.MAGENTA);
            view.setBackground(ContextCompat.getDrawable(parentView.getContext(), R.drawable.doted_vertical));
        }
        ///view.setBackgroundColor(Color.MAGENTA);
        view.setVisibility(View.VISIBLE);
    }

    private View createSnappingGuidelines(boolean isHorizontal) {
        View view = new View(parentView.getContext());
        parentView.addView(view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isHorizontal) {
            params.height = 4;
        } else {
            params.width = 4;
        }
        view.setLayoutParams(params);
        return view;
    }

    void setCalculatedValuesForGuideView(View view, int width, float x, float y, boolean isHorizontal) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (isHorizontal) {
            params.width = width;
        } else {
            params.height = width;
        }
        view.setLayoutParams(params);
        view.setX(x);
        view.setY(y);
    }

    private void handleGuidelines(View view) {
        int yAxisCenter = Math.round(parentView.getHeight() / 2.0f);
        if (((Math.round(view.getY())) >= (yAxisCenter - 20) && (Math.round(view.getY())) <= (yAxisCenter + 20))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(yAxisCenter);
        } else if (((Math.round(view.getY() + (view.getHeight() / 2.0))) >= (yAxisCenter - 20) && (Math.round((view.getY() + (view.getHeight() / 2.0)))) <= (yAxisCenter + 20))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(Math.round(yAxisCenter - (view.getHeight() / 2.0)));
        } else if (((Math.round(view.getY() + (view.getHeight())) >= (yAxisCenter - 20)) && (Math.round((view.getY() + (view.getHeight()))) <= (yAxisCenter + 20)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(Math.round(yAxisCenter - view.getHeight()));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL_GONE);
        }


        int xAxisCenter = Math.round(parentView.getWidth() / 2.0f);
        if (((Math.round(view.getX())) >= (xAxisCenter - 20) && (Math.round(view.getX())) <= (xAxisCenter + 20))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(xAxisCenter);
        } else if (((Math.round(view.getX() + (view.getWidth() / 2.0))) >= (xAxisCenter - 20) && (Math.round((view.getX() + (view.getWidth() / 2.0)))) <= (xAxisCenter + 20))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(Math.round(xAxisCenter - (view.getWidth() / 2.0)));
        } else if (((Math.round(view.getX() + (view.getWidth())) >= (xAxisCenter - 20)) && (Math.round((view.getX() + (view.getWidth()))) <= (xAxisCenter + 20)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(Math.round(xAxisCenter - view.getWidth()));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL_GONE);
        }


        int yAxisTop = 0;
        if (Math.round(view.getY()) <= yAxisTop + 20 && Math.round(view.getY()) >= yAxisTop - 20) {
            view.setY(yAxisTop);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP_GONE);
        }

        int yAxisBottom = Math.round(parentView.getHeight());
        if (Math.round(view.getY() + view.getHeight()) >= yAxisBottom - 20 && Math.round(view.getY()) + view.getHeight() <= yAxisBottom + 20) {
            view.setY(yAxisBottom - view.getHeight());
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM_GONE);
        }

        int xAxisLeft = 0;
        if (Math.round(view.getX()) <= xAxisLeft + 20 && Math.round(view.getX()) >= xAxisLeft - 20) {
            view.setX(xAxisLeft);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT_GONE);
        }


        int xAxisRight = Math.round(parentView.getWidth());
        if (Math.round(view.getX() + view.getWidth()) >= xAxisRight - 20 && Math.round(view.getX()) + view.getWidth() <= xAxisRight + 20) {
            view.setX(xAxisRight - view.getWidth());
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT_GONE);
        }
    }

    private void firePhotoEditorSDKListener(View view, boolean isStart) {
        Object viewTag = view.getTag();
        if (mOnPhotoEditorListener != null && viewTag != null && viewTag instanceof ViewType) {
            if (isStart)
                mOnPhotoEditorListener.onStartViewChangeListener(((ViewType) view.getTag()));
            else
                mOnPhotoEditorListener.onStopViewChangeListener(((ViewType) view.getTag()));
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

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return mIsPinchScalable;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            TransformInfo info = new TransformInfo();
            info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
            info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
            info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
            info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;
            move(view, info);
            return !mIsPinchScalable;
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