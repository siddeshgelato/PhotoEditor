package ja.burhanrashid52.photoeditor;

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
                    snapViews(view);

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
                removeLine(SnappingViewGuidelinePosition.TOP_TOP);
                removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE);
                removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM);
                removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP);
                removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE);
                removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM);
                removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP);
                removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE);
                removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM);
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

    private void snapViews(View view) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {

                if (Math.round(view.getY()) >= Math.round((child.getY() - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + 10))) {
                    view.setY(child.getY());
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_TOP);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight()/2.0) - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight()/2.0) + 10))) {
                    view.setY(child.getY() + (child.getHeight()/2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight()) - 10)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight()) + 10))) {
                    view.setY(child.getY() + (child.getHeight()));
                    drawLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + 10))) {
                    view.setY(child.getY() - (child.getHeight()/2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight() / 2.0) - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight() / 2.0) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight()) - 10)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight()) + 10))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY()  - 10)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY()  + 10))) {
                    view.setY(child.getY() - child.getHeight());
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight()/2.0f)  - 10)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight()/2.0f)  + 10))) {
                    view.setY(child.getY() - (view.getHeight() / 2.0f));
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight())  - 5)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight())  + 5))) {
                    drawLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM);
                }
            }
        }
    }

    private void removeLine(SnappingViewGuidelinePosition position) {
        switch (position) {
            case TOP_TOP:
                if (horizontalTopView != null) {
                    horizontalTopView.setVisibility(View.GONE);
                }
                break;
            case TOP_MIDDLE:
                if (horizontalMiddleView != null) {
                    horizontalMiddleView.setVisibility(View.GONE);
                }
                break;
            case TOP_BOTTOM:
                if (horizontalBottomView != null) {
                    horizontalBottomView.setVisibility(View.GONE);
                }
                break;
            case MIDDLE_TOP:
                if(horizontalMiddleTopView != null) {
                    horizontalMiddleTopView.setVisibility(View.GONE);
                }
                break;

            case MIDDLE_MIDDLE:
                if(horizontalMiddleMiddleView != null) {
                    horizontalMiddleMiddleView.setVisibility(View.GONE);
                }
                break;
            case MIDDLE_BOTTOM:
                if(horizontalMiddleBottomView != null) {
                    horizontalMiddleBottomView.setVisibility(View.GONE);
                }
                break;

            case BOTTOM_TOP:
                if(horizontalBottomTopView != null) {
                    horizontalBottomTopView.setVisibility(View.GONE);
                }
                break;
            case BOTTOM_MIDDLE:
                if(horizontalBottomMiddleView != null) {
                    horizontalBottomMiddleView.setVisibility(View.GONE);
                }
                break;
            case BOTTOM_BOTTOM:
                if(horizontalBottomBottomView != null) {
                    horizontalBottomBottomView.setVisibility(View.GONE);
                }
                break;
        }

    }

    private void drawLine(View view, View child, SnappingViewGuidelinePosition position) {
        switch (position) {
            case TOP_TOP:
                if (horizontalTopView == null) {
                    horizontalTopView = createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalTopView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalTopView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX(), child.getY());
                } else {
                    setCalculatedValuesForGuideView(horizontalTopView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(), child.getY());
                }
                break;
            case TOP_MIDDLE:
                if (horizontalMiddleView == null) {
                    horizontalMiddleView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalMiddleView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalMiddleView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX(), child.getY() + (child.getHeight() / 2.0f));
                } else {
                    setCalculatedValuesForGuideView(horizontalMiddleView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(), child.getY() + (child.getHeight() / 2.0f));
                }
                break;
            case TOP_BOTTOM:
                if (horizontalBottomView == null) {
                    horizontalBottomView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalBottomView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalBottomView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX(), child.getY() + child.getHeight());
                } else {
                    setCalculatedValuesForGuideView(horizontalBottomView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(), child.getY() + child.getHeight());
                }
                break;
            case MIDDLE_TOP:
                if (horizontalMiddleTopView == null) {
                    horizontalMiddleTopView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalMiddleTopView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalMiddleTopView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()/2.0f));
                } else {
                    setCalculatedValuesForGuideView(horizontalMiddleTopView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY());
                }
                break;

            case MIDDLE_MIDDLE:
                if (horizontalMiddleMiddleView == null) {
                    horizontalMiddleMiddleView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalMiddleMiddleView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalMiddleMiddleView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()/2.0f));
                } else {
                    setCalculatedValuesForGuideView(horizontalMiddleMiddleView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY() + (child.getHeight()/2.0f));
                }
                break;
            case MIDDLE_BOTTOM:
                if (horizontalMiddleBottomView == null) {
                    horizontalMiddleBottomView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalMiddleBottomView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalMiddleBottomView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()/2.0f));
                } else {
                    setCalculatedValuesForGuideView(horizontalMiddleBottomView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY() + (child.getHeight()));
                }
                break;

            case BOTTOM_TOP:
                if (horizontalBottomTopView == null) {
                    horizontalBottomTopView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalBottomTopView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalBottomTopView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()));
                } else {
                    setCalculatedValuesForGuideView(horizontalBottomTopView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY());
                }
                break;

            case BOTTOM_MIDDLE:
                if (horizontalBottomMiddleView == null) {
                    horizontalBottomMiddleView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalBottomMiddleView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalBottomMiddleView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()));
                } else {
                    setCalculatedValuesForGuideView(horizontalBottomMiddleView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY() + (child.getHeight() / 2.0f));
                }
                break;

            case BOTTOM_BOTTOM:
                if (horizontalBottomBottomView == null) {
                    horizontalBottomBottomView =  createSnappingGuidelines();
                }
                setSnappingGuidelineUIProperties(horizontalBottomBottomView);
                if (view.getX() < child.getX()) {
                    setCalculatedValuesForGuideView(horizontalBottomBottomView, (int) ((child.getX() + child.getWidth()) - view.getX()), view.getX() , view.getY() + (view.getHeight()));
                } else {
                    setCalculatedValuesForGuideView(horizontalBottomBottomView, (int) ((view.getX() + view.getWidth()) - child.getX()), child.getX(),  child.getY() + (child.getHeight()));
                }
                break;

        }

    }

    private void setSnappingGuidelineUIProperties(View view) {
        view.setBackground(ContextCompat.getDrawable(parentView.getContext(), R.drawable.doted));
        view.setVisibility(View.VISIBLE);
    }

    private View createSnappingGuidelines() {
        View view = new View(parentView.getContext());
        parentView.addView(view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = 4;
        view.setLayoutParams(params);
        return view;
    }

    void setCalculatedValuesForGuideView(View view, int width, float x, float y) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
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