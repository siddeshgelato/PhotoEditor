package ja.burhanrashid52.photoeditor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

public class GuideLineController {

    private static final String SNAPPING_VIEWS_TAG = "snapping_guidelines";

    private static final int MARGIN_MAGNET_SNAPPING_GUIDELINES = 10;
    private static final int MARGIN_MAGNET_MAIN_GUIDELINES = 20;
    private static final int GUIDELINE_THICKNESS = 4;

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

    private final PhotoEditorView parentView;

    public GuideLineController(PhotoEditorView parentView) {
        this.parentView = parentView;
    }

    void snappingVerticalViews(View view) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(view.getX()) >= Math.round((child.getX() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX());
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, false);
                }

                if (Math.round(view.getX()) >= Math.round((child.getX() + (child.getWidth() / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + (child.getWidth() / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX() + (child.getWidth() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, false);
                }

                if (Math.round(view.getX()) >= Math.round((child.getX() + (child.getWidth()) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX()) <= Math.round(child.getX() + (child.getWidth()) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX() + (child.getWidth()));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, false);
                }

                if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX() - (child.getWidth() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, false);
                }

                if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() + (child.getWidth() / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + (child.getWidth() / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, false);
                }

                if (Math.round(view.getX() + (view.getWidth() / 2.0)) >= Math.round((child.getX() + (child.getWidth()) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX() + (view.getWidth() / 2.0)) <= Math.round(child.getX() + (child.getWidth()) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, false);
                }

                if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX() - child.getWidth());
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, false);
                }

                if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() + (child.getWidth() / 2.0f) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + (child.getWidth() / 2.0f) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setX(child.getX() - (view.getWidth() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, false);
                }

                if (Math.round(view.getX() + (view.getWidth())) >= Math.round((child.getX() + (child.getWidth()) - 5)) &&
                        (Math.round(view.getX() + (view.getWidth())) <= Math.round(child.getX() + (child.getWidth()) + 5))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_BOTTOM, false);
                }
            }
        }
    }

    void snappingHorizontalViews(View view) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (view.getTag() != null && view.getTag() != child.getTag() && child instanceof FrameLayout) {
                if (Math.round(view.getY()) >= Math.round((child.getY() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY());
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_TOP, true);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight() / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight() / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY() + (child.getHeight() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_MIDDLE, true);
                }

                if (Math.round(view.getY()) >= Math.round((child.getY() + (child.getHeight()) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY()) <= Math.round(child.getY() + (child.getHeight()) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY() + (child.getHeight()));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.TOP_BOTTOM, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY() - (child.getHeight() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_TOP, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight() / 2.0) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight() / 2.0) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_MIDDLE, true);
                }

                if (Math.round(view.getY() + (view.getHeight() / 2.0)) >= Math.round((child.getY() + (child.getHeight()) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY() + (view.getHeight() / 2.0)) <= Math.round(child.getY() + (child.getHeight()) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.MIDDLE_BOTTOM, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY() - child.getHeight());
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_TOP, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight() / 2.0f) - MARGIN_MAGNET_SNAPPING_GUIDELINES)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight() / 2.0f) + MARGIN_MAGNET_SNAPPING_GUIDELINES))) {
                    view.setY(child.getY() - (view.getHeight() / 2.0f));
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                } else {
                    removeLine(SnappingViewGuidelinePosition.BOTTOM_MIDDLE, true);
                }

                if (Math.round(view.getY() + (view.getHeight())) >= Math.round((child.getY() + (child.getHeight()) - 5)) &&
                        (Math.round(view.getY() + (view.getHeight())) <= Math.round(child.getY() + (child.getHeight()) + 5))) {
                    generatePerticularLine(view, child, SnappingViewGuidelinePosition.BOTTOM_BOTTOM, true);
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
                    setCalculatedValuesForGuideView(horizontalTopView, heightWidth, Math.min(view.getX(), child.getX()), child.getY(), isHorizontal);
                } else {
                    if (verticalTopView == null) {
                        verticalTopView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalTopView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalTopView, heightWidth, child.getX(), Math.min(view.getY(), child.getY()), isHorizontal);
                }
                break;
            case TOP_MIDDLE:
                if (isHorizontal) {
                    if (horizontalMiddleView == null) {
                        horizontalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalMiddleView, isHorizontal);
                    setCalculatedValuesForGuideView(horizontalMiddleView, heightWidth, Math.min(view.getX(), child.getX()), child.getY() + (child.getHeight() / 2.0f), isHorizontal);
                } else {
                    if (verticalMiddleView == null) {
                        verticalMiddleView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalMiddleView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalMiddleView, heightWidth, child.getX() + child.getWidth() / 2.0f, Math.min(view.getY(), child.getY()), isHorizontal);
                }
                break;
            case TOP_BOTTOM:
                if (isHorizontal) {
                    if (horizontalBottomView == null) {
                        horizontalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(horizontalBottomView, isHorizontal);
                    setCalculatedValuesForGuideView(horizontalBottomView, (int) heightWidth, Math.min(view.getX(), child.getX()), child.getY() + child.getHeight(), isHorizontal);
                } else {
                    if (verticalBottomView == null) {
                        verticalBottomView = createSnappingGuidelines(isHorizontal);
                    }
                    setSnappingGuidelineUIProperties(verticalBottomView, isHorizontal);
                    setCalculatedValuesForGuideView(verticalBottomView, (int) heightWidth, child.getX() + child.getWidth(), Math.min(view.getY(), child.getY()), isHorizontal);
                }
                break;
            case MIDDLE_TOP:
                if (isHorizontal) {
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
                if (isHorizontal) {
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
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, view.getX() + (view.getWidth() / 2.0f), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalMiddleMiddleView, heightWidth, child.getX() + (child.getWidth() / 2.0f), child.getY(), isHorizontal);
                    }
                }
                break;
            case MIDDLE_BOTTOM:
                if (isHorizontal) {
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
                if (isHorizontal) {
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
                if (isHorizontal) {
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
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth, view.getX() + (view.getWidth()), view.getY(), isHorizontal);
                    } else {
                        setCalculatedValuesForGuideView(verticalBottomMiddleView, heightWidth, child.getX() + (child.getWidth() / 2.0f), child.getY(), isHorizontal);
                    }
                }
                break;

            case BOTTOM_BOTTOM:
                if (isHorizontal) {
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

    private int getHeightWidth(View view, View child, boolean isHorizontal) {
        int heightWidth;
        if (isHorizontal) {
            if (view.getX() < child.getX()) {
                heightWidth = (int) ((child.getX() + child.getWidth()) - view.getX());
            } else {
                heightWidth = (int) ((view.getX() + view.getWidth()) - child.getX());
            }
        } else {
            if (view.getY() < child.getY()) {
                heightWidth = (int) ((child.getY() + child.getHeight()) - view.getY());
            } else {
                heightWidth = (int) ((view.getY() + view.getHeight()) - child.getY());
            }
        }
        return heightWidth;
    }

    private void setVisibilityGone(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    private void setSnappingGuidelineUIProperties(View view, boolean isHorizontal) {
        if (isHorizontal) {
            view.setBackground(ContextCompat.getDrawable(parentView.getContext(), R.drawable.doted));
        } else {
            view.setBackground(ContextCompat.getDrawable(parentView.getContext(), R.drawable.doted_vertical));
        }
        view.setVisibility(View.VISIBLE);
    }

    private View createSnappingGuidelines(boolean isHorizontal) {
        View view = new View(parentView.getContext());
        view.setTag(SNAPPING_VIEWS_TAG);
        parentView.addView(view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (isHorizontal) {
            params.width = heightOrWidth;
        } else {
            params.height = heightOrWidth;
        }
        view.setLayoutParams(params);
        view.setX(x);
        view.setY(y);
    }

    void handleGuidelines(View view) {
        int yAxisCenter = Math.round(parentView.getHeight() / 2.0f);
        if (((Math.round(view.getY())) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round(view.getY())) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(yAxisCenter);
        } else if (((Math.round(view.getY() + (view.getHeight() / 2.0))) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round((view.getY() + (view.getHeight() / 2.0)))) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(Math.round(yAxisCenter - (view.getHeight() / 2.0)));
        } else if (((Math.round(view.getY() + (view.getHeight())) >= (yAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES)) && (Math.round((view.getY() + (view.getHeight()))) <= (yAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL);
            view.setY(Math.round(yAxisCenter - view.getHeight()));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.HORIZONTAL_GONE);
        }


        int xAxisCenter = Math.round(parentView.getWidth() / 2.0f);
        if (((Math.round(view.getX())) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round(view.getX())) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(xAxisCenter);
        } else if (((Math.round(view.getX() + (view.getWidth() / 2.0))) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES) && (Math.round((view.getX() + (view.getWidth() / 2.0)))) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(Math.round(xAxisCenter - (view.getWidth() / 2.0)));
        } else if (((Math.round(view.getX() + (view.getWidth())) >= (xAxisCenter - MARGIN_MAGNET_MAIN_GUIDELINES)) && (Math.round((view.getX() + (view.getWidth()))) <= (xAxisCenter + MARGIN_MAGNET_MAIN_GUIDELINES)))) {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL);
            view.setX(Math.round(xAxisCenter - view.getWidth()));
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.VERTICAL_GONE);
        }


        int yAxisTop = 0;
        if (Math.round(view.getY()) <= yAxisTop + MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(view.getY()) >= yAxisTop - MARGIN_MAGNET_MAIN_GUIDELINES) {
            view.setY(yAxisTop);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.TOP_GONE);
        }

        int yAxisBottom = Math.round(parentView.getHeight());
        if (Math.round(view.getY() + view.getHeight()) >= yAxisBottom - MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(view.getY()) + view.getHeight() <= yAxisBottom + MARGIN_MAGNET_MAIN_GUIDELINES) {
            view.setY(yAxisBottom - view.getHeight());
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.BOTTOM_GONE);
        }

        int xAxisLeft = 0;
        if (Math.round(view.getX()) <= xAxisLeft + MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(view.getX()) >= xAxisLeft - MARGIN_MAGNET_MAIN_GUIDELINES) {
            view.setX(xAxisLeft);
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.LEFT_GONE);
        }


        int xAxisRight = Math.round(parentView.getWidth());
        if (Math.round(view.getX() + view.getWidth()) >= xAxisRight - MARGIN_MAGNET_MAIN_GUIDELINES && Math.round(view.getX()) + view.getWidth() <= xAxisRight + MARGIN_MAGNET_MAIN_GUIDELINES) {
            view.setX(xAxisRight - view.getWidth());
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT);
        } else {
            parentView.setVisibilityOfGuideLines(GuidelineVisibility.RIGHT_GONE);
        }
    }

}
