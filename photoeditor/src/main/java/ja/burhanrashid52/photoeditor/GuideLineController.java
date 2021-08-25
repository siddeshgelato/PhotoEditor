package ja.burhanrashid52.photoeditor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

public class GuideLineController {

    private static final String SNAPPING_VIEWS_TAG = "snapping_guidelines";

    private static final int MARGIN_MAGNET_SNAPPING_GUIDELINES = 10;
    private static final int MARGIN_MAGNET_MAIN_GUIDELINES = 20;
    private static final int GUIDELINE_THICKNESS = 4;

    private DividerView horizontalTopView;
    private DividerView horizontalMiddleView;
    private DividerView horizontalBottomView;
    private DividerView horizontalMiddleTopView;
    private DividerView horizontalMiddleMiddleView;
    private DividerView horizontalMiddleBottomView;
    private DividerView horizontalBottomTopView;
    private DividerView horizontalBottomMiddleView;
    private DividerView horizontalBottomBottomView;

    private DividerView verticalTopView;
    private DividerView verticalMiddleView;
    private DividerView verticalBottomView;
    private DividerView verticalMiddleTopView;
    private DividerView verticalMiddleMiddleView;
    private DividerView verticalMiddleBottomView;
    private DividerView verticalBottomTopView;
    private DividerView verticalBottomMiddleView;
    private DividerView verticalBottomBottomView;

    private final PhotoEditorView parentView;

    public GuideLineController(PhotoEditorView parentView) {
        this.parentView = parentView;
    }

    void snappingVerticalViews(View view) {
        boolean isMatched = false;
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(ViewUtil.getTransformedX(view)) >= Math.round((ViewUtil.getTransformedX(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view)) <= Math.round(ViewUtil.getTransformedX(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view, ViewUtil.getTransformedX(child));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view)) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view)) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view, ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view)) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view)) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) >= Math.round((ViewUtil.getTransformedX(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) <= Math.round(ViewUtil.getTransformedX(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,ViewUtil.getTransformedX(child) - (ViewUtil.getTransformedWidth(view) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,(ViewUtil.getTransformedX(child) - ViewUtil.getTransformedWidth(view)) + (ViewUtil.getTransformedWidth(child) / 2.0f) + (ViewUtil.getTransformedWidth(view) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,((ViewUtil.getTransformedX(child) + ViewUtil.getTransformedWidth(child)) - ViewUtil.getTransformedWidth(view)) + (ViewUtil.getTransformedWidth(view)/2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) >= Math.round((ViewUtil.getTransformedX(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) <= Math.round(ViewUtil.getTransformedX(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,ViewUtil.getTransformedX(child) - ViewUtil.getTransformedWidth(view));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0f) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0f) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,(ViewUtil.getTransformedX(child) - ViewUtil.getTransformedWidth(view)) + (ViewUtil.getTransformedWidth(child) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                }

                if (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) >= Math.round((ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) - 5)) &&
                        (Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) <= Math.round(ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)) + 5))) {
                    isMatched = true;
                    ViewUtil.setTransformedX(view,(ViewUtil.getTransformedX(child) + ViewUtil.getTransformedWidth(child)) - ViewUtil.getTransformedWidth(view));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                }
                if(isMatched) {
                    break;
                }
            }
        }
    }

    void snappingHorizontalViews(View view) {
        boolean isMatched = false;
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(ViewUtil.getTransformedY(view)) >= Math.round((ViewUtil.getTransformedY(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view)) <= Math.round(ViewUtil.getTransformedY(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view, ViewUtil.getTransformedY(child));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view)) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view)) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view)) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view)) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) >= Math.round((ViewUtil.getTransformedY(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) <= Math.round(ViewUtil.getTransformedY(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,ViewUtil.getTransformedY(child) - (ViewUtil.getTransformedHeight(view) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,(ViewUtil.getTransformedY(child) - ViewUtil.getTransformedHeight(view)) + (ViewUtil.getTransformedHeight(child) / 2.0f) + (ViewUtil.getTransformedHeight(view) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,((ViewUtil.getTransformedY(child) + ViewUtil.getTransformedHeight(child)) - ViewUtil.getTransformedHeight(view)) + (ViewUtil.getTransformedHeight(view)/2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) >= Math.round((ViewUtil.getTransformedY(child) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) <= Math.round(ViewUtil.getTransformedY(child) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,ViewUtil.getTransformedY(child) - ViewUtil.getTransformedHeight(view));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,(ViewUtil.getTransformedY(child) - ViewUtil.getTransformedHeight(view)) + (ViewUtil.getTransformedHeight(child) / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                }

                if (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) >= Math.round((ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) - 5)) &&
                        (Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) <= Math.round(ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)) + 5))) {
                    isMatched = true;
                    ViewUtil.setTransformedY(view,(ViewUtil.getTransformedY(child) + ViewUtil.getTransformedHeight(child)) - ViewUtil.getTransformedHeight(view));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
                }

                if(isMatched) {
                    break;
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

    void hideAllGuidelines() {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View view = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag().equals(SNAPPING_VIEWS_TAG)) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void generatePerticularLine(View view, View child, SnappingViewGuidelinePosition position, boolean isHorizontal) {
        int heightWidth = getHeightWidth(view, child, isHorizontal);

        switch (position) {
            case TOP_TOP:
                if (isHorizontal) {
                    if (horizontalTopView == null) {
                        horizontalTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalTopView, isHorizontal);
                    setCalculatedValuesForGuideView(horizontalTopView, heightWidth, Math.min(ViewUtil.getTransformedX(view), ViewUtil.getTransformedX(child)), ViewUtil.getTransformedY(child), isHorizontal);
                } else {
                    if (verticalTopView == null) {
                        verticalTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalTopView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalTopView, heightWidth, ViewUtil.getTransformedX(child), Math.min(ViewUtil.getTransformedY(view), ViewUtil.getTransformedY(child)), isHorizontal);
                }
                break;
            case TOP_MIDDLE:
                if (isHorizontal) {
                    if (horizontalMiddleView == null) {
                        horizontalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleView, isHorizontal);
                    setCalculatedValuesForGuideView(horizontalMiddleView, heightWidth, Math.min(ViewUtil.getTransformedX(view), ViewUtil.getTransformedX(child)), ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f), isHorizontal);
                } else {
                    if (verticalMiddleView == null) {
                        verticalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalMiddleView, heightWidth, ViewUtil.getTransformedX(child) + ViewUtil.getTransformedWidth(child) / 2.0f, Math.min(ViewUtil.getTransformedY(view), ViewUtil.getTransformedY(child)), isHorizontal);
                }
                break;
            case TOP_BOTTOM:
                if (isHorizontal) {
                    if (horizontalBottomView == null) {
                        horizontalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomView, isHorizontal);
                    setCalculatedValuesForGuideView(horizontalBottomView, (int) heightWidth, Math.min(ViewUtil.getTransformedX(view), ViewUtil.getTransformedX(child)), ViewUtil.getTransformedY(child) + ViewUtil.getTransformedHeight(child), isHorizontal);
                } else {
                    if (verticalBottomView == null) {
                        verticalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalBottomView, (int) heightWidth, ViewUtil.getTransformedX(child) + ViewUtil.getTransformedWidth(child), Math.min(ViewUtil.getTransformedY(view), ViewUtil.getTransformedY(child)), isHorizontal);
                }
                break;
            case MIDDLE_TOP:
                if (isHorizontal) {
                    if (horizontalMiddleTopView == null) {
                        horizontalMiddleTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleTopView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalMiddleTopView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleTopView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                } else {
                    if (verticalMiddleTopView == null) {
                        verticalMiddleTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleTopView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalMiddleTopView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0f), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleTopView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;

            case MIDDLE_MIDDLE:
                if (isHorizontal) {
                    if (horizontalMiddleMiddleView == null) {
                        horizontalMiddleMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleMiddleView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalMiddleMiddleView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleMiddleView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f), isHorizontal);
                    }
                } else {
                    if (verticalMiddleMiddleView == null) {
                        verticalMiddleMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleMiddleView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0f), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0f), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;
            case MIDDLE_BOTTOM:
                if (isHorizontal) {
                    if (horizontalMiddleBottomView == null) {
                        horizontalMiddleBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleBottomView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalMiddleBottomView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0f), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalMiddleBottomView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)), isHorizontal);
                    }
                } else {
                    if (verticalMiddleBottomView == null) {
                        verticalMiddleBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleBottomView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalMiddleBottomView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0f), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleBottomView, heightWidth, ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;

            case BOTTOM_TOP:
                if (isHorizontal) {
                    if (horizontalBottomTopView == null) {
                        horizontalBottomTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomTopView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalBottomTopView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view)), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomTopView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                } else {
                    if (verticalBottomTopView == null) {
                        verticalBottomTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomTopView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalBottomTopView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view)), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomTopView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;

            case BOTTOM_MIDDLE:
                if (isHorizontal) {
                    if (horizontalBottomMiddleView == null) {
                        horizontalBottomMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomMiddleView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalBottomMiddleView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view)), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomMiddleView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child) / 2.0f), isHorizontal);
                    }
                } else {
                    if (verticalBottomMiddleView == null) {
                        verticalBottomMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomMiddleView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view)), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth, ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child) / 2.0f), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;

            case BOTTOM_BOTTOM:
                if (isHorizontal) {
                    if (horizontalBottomBottomView == null) {
                        horizontalBottomBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomBottomView, isHorizontal);
                    if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                        setCalculatedValuesForGuideView(horizontalBottomBottomView, heightWidth, ViewUtil.getTransformedX(view), ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view)), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(horizontalBottomBottomView, heightWidth, ViewUtil.getTransformedX(child), ViewUtil.getTransformedY(child) + (ViewUtil.getTransformedHeight(child)), isHorizontal);
                    }
                } else {
                    if (verticalBottomBottomView == null) {
                        verticalBottomBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomBottomView, isHorizontal);
                    if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                        setCalculatedValuesForGuideView(verticalBottomBottomView, heightWidth, ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view)), ViewUtil.getTransformedY(view), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomBottomView, heightWidth, ViewUtil.getTransformedX(child) + (ViewUtil.getTransformedWidth(child)), ViewUtil.getTransformedY(child), isHorizontal);
                    }
                }
                break;
        }
    }

    private int getHeightWidth(View view, View child, boolean isHorizontal) {
        int heightWidth;
        if (isHorizontal) {
            if (ViewUtil.getTransformedX(view) < ViewUtil.getTransformedX(child)) {
                heightWidth = (int) ((ViewUtil.getTransformedX(child) + ViewUtil.getTransformedWidth(child)) - ViewUtil.getTransformedX(view));
            } else {
                heightWidth = (int) ((ViewUtil.getTransformedX(view) + ViewUtil.getTransformedWidth(view)) - ViewUtil.getTransformedX(child));
            }
        } else {
            if (ViewUtil.getTransformedY(view) < ViewUtil.getTransformedY(child)) {
                heightWidth = (int) ((ViewUtil.getTransformedY(child) + ViewUtil.getTransformedHeight(child)) - ViewUtil.getTransformedY(view));
            } else {
                heightWidth = (int) ((ViewUtil.getTransformedY(view) + ViewUtil.getTransformedHeight(view)) - ViewUtil.getTransformedY(child));
            }
        }
        return heightWidth;
    }

    private void setVisibilityGone(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    private void setSnappingGuidelineUIProperties(DividerView view, boolean isHorizontal) {
        if (isHorizontal) {
            view.setOrientation(DividerView.ORIENTATION_HORIZONTAL);
        } else {
            view.setOrientation(DividerView.ORIENTATION_VERTICAL);
        }
        view.bringToFront();
        view.setVisibility(View.VISIBLE);
    }

    private DividerView createSnappingGuidelines(boolean isHorizontal) {
        DividerView view = new DividerView(parentView.getContext());
        view.setTag(SNAPPING_VIEWS_TAG);
        parentView.addView(view);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isHorizontal) {
            params.height = GUIDELINE_THICKNESS;
        } else {
            params.width = GUIDELINE_THICKNESS;
        }
        view.setLayoutParams(params);
        return view;
    }

    void setCalculatedValuesForGuideView(View view, int heightOrWidth, float x, float y, boolean isHorizontal) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        if (isHorizontal) {
            params.width = heightOrWidth;
        } else {
            params.height = heightOrWidth;
        }
        view.setLayoutParams(params);
        ViewUtil.setTransformedX(view,x);
        ViewUtil.setTransformedY(view,y);
    }

    void handleGuidelines(View view) {
        int yAxisCenter = Math.round(parentView.getHeight() / 2.0f);
        if (((Math.round(ViewUtil.getTransformedY(view))) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round(ViewUtil.getTransformedY(view))) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            ViewUtil.setTransformedY(view,yAxisCenter);
        } else if (((Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0))) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round((ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view) / 2.0)))) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            ViewUtil.setTransformedY(view,Math.round(yAxisCenter - (ViewUtil.getTransformedHeight(view) / 2.0)));
        } else if (((Math.round(ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view))) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES)) && (Math.round((ViewUtil.getTransformedY(view) + (ViewUtil.getTransformedHeight(view)))) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            ViewUtil.setTransformedY(view,Math.round(yAxisCenter - ViewUtil.getTransformedHeight(view)));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL_GONE);
        }


        int xAxisCenter = Math.round(parentView.getWidth() / 2.0f);
        if (((Math.round(ViewUtil.getTransformedX(view))) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round(ViewUtil.getTransformedX(view))) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            ViewUtil.setTransformedX(view,xAxisCenter);
        } else if (((Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0))) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round((ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view) / 2.0)))) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            ViewUtil.setTransformedX(view,Math.round(xAxisCenter - (ViewUtil.getTransformedWidth(view) / 2.0)));
        } else if (((Math.round(ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view))) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES)) && (Math.round((ViewUtil.getTransformedX(view) + (ViewUtil.getTransformedWidth(view)))) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            ViewUtil.setTransformedX(view,Math.round(xAxisCenter - ViewUtil.getTransformedWidth(view)));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL_GONE);
        }


        int yAxisTop = 0;
        if (Math.round(ViewUtil.getTransformedY(view)) <= yAxisTop + MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(ViewUtil.getTransformedY(view)) >= yAxisTop - MARGIN_MAGNET_MAIN_GUIDELINES) {
            ViewUtil.setTransformedY(view,yAxisTop);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP_GONE);
        }

        int yAxisBottom = Math.round(parentView.getHeight());
        if (Math.round(ViewUtil.getTransformedY(view) + ViewUtil.getTransformedHeight(view)) >= yAxisBottom - MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(ViewUtil.getTransformedY(view)) + ViewUtil.getTransformedHeight(view) <= yAxisBottom + MARGIN_MAGNET_MAIN_GUIDELINES) {
            ViewUtil.setTransformedY(view,yAxisBottom - ViewUtil.getTransformedHeight(view));
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM_GONE);
        }

        int xAxisLeft = 0;
        if (Math.round(ViewUtil.getTransformedX(view)) <= xAxisLeft + MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(ViewUtil.getTransformedX(view)) >= xAxisLeft - MARGIN_MAGNET_MAIN_GUIDELINES) {
            ViewUtil.setTransformedX(view,xAxisLeft);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT_GONE);
        }


        int xAxisRight = Math.round(parentView.getWidth());
        if (Math.round(ViewUtil.getTransformedX(view) + ViewUtil.getTransformedWidth(view)) >= xAxisRight - MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(ViewUtil.getTransformedX(view)) + ViewUtil.getTransformedWidth(view) <= xAxisRight + MARGIN_MAGNET_MAIN_GUIDELINES) {
            ViewUtil.setTransformedX(view,xAxisRight - ViewUtil.getTransformedWidth(view));
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT_GONE);
        }
    }
}
