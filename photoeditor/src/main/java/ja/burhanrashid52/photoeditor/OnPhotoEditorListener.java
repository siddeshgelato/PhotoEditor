package ja.burhanrashid52.photoeditor;

import android.view.View;

/**
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 18/01/2017
 * <p>
 * This are the callbacks when any changes happens while editing the photo to make and custimization
 * on client side
 * </p>
 */
public interface OnPhotoEditorListener {

    /**
     * When user long press the existing text this event will trigger implying that user want to
     * edit the current {@link android.widget.TextView}
     *
     * @param rootView  view on which the long press occurs
     * @param text      current text set on the view
     * @param colorCode current color value set on view
     */
    void onEditTextChangeListener(View rootView, String text, int colorCode);

    /**
     * This is a callback when user adds any view on the {@link PhotoEditorView} it can be
     * brush,text or sticker i.e bitmap on parent view
     *
     * @param viewType           enum which define type of view is added
     * @see ViewType
     */
    void onAddViewListener(ViewType viewType);

    /**
     * This is a callback when user remove any view on the {@link PhotoEditorView} it happens when usually
     * undo and redo happens or text is removed
     *
     * @param viewType           enum which define type of view is added
     * @param numberOfAddedViews number of views currently added
     */
    void onRemoveViewListener(ViewType viewType, int numberOfAddedViews);

    /**
     * A callback when user start dragging a view which can be
     * any of {@link ViewType}
     *
     */
    void onStartViewChangeListener(String tag);


    /**
     * A callback when user stop/up touching a view which can be
     * any of {@link ViewType}
     *
     */
    void onStopViewChangeListener(String tag);

    void onTransformationStarted();

    void onTransformationEnd();

    void beginEditText();

    void endEditText();
}
