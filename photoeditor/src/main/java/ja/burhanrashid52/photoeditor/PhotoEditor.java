package ja.burhanrashid52.photoeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <p>
 * This class in initialize by {@link Builder} using a builder pattern with multiple
 * editing attributes
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 18/01/2017
 */
public class PhotoEditor implements BrushViewChangeListener {

    private static final String TAG = "PhotoEditor";
    private static final int ZINDEX_MEDIA = 1000;
    private static final int MARGIN = 50;
    private static final int DEFAULT_EXTRA_WIDTH_HEIGHT = 8;
    private final LayoutInflater mLayoutInflater;
    private Context context;
    private PhotoEditorView parentView;
    private PhotoEditorViewState viewState;
    public UndoRedoController undoRedoController;
    private ImageView backgroundImageView;
    private View deleteView;
    private BrushDrawingView brushDrawingView;
    private OnPhotoEditorListener mOnPhotoEditorListener;
    private OnElementSelectionListener elementSelectionListener;
    private boolean isTextPinchScalable;
    private Typeface mDefaultTextTypeface;
    private Typeface mDefaultEmojiTypeface;
    private float posX = -1;
    private float posY = -1;
    private float rotation = 0.0f;
    private float px = -1;
    private float py = -1;
    private float zIndexCount = ZINDEX_MEDIA;
    private double editorScale = 1.0;


    protected PhotoEditor(Builder builder) {
        this.context = builder.context;
        this.parentView = builder.parentView;
        this.backgroundImageView = builder.imageView;
        this.deleteView = builder.deleteView;
        this.editorScale = builder.editorScale;
        this.brushDrawingView = builder.brushDrawingView;
        this.isTextPinchScalable = builder.isTextPinchScalable;
        this.mDefaultTextTypeface = builder.textTypeface;
        this.mDefaultEmojiTypeface = builder.emojiTypeface;
        this.viewState = new PhotoEditorViewState();
        //  this.undoRedoController = new UndoRedoController(viewState, this);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        brushDrawingView.setBrushViewChangeListener(this);

        final GestureDetector mDetector = new GestureDetector(
                context,
                new PhotoEditorImageViewListener(
                        this.viewState,
                        new PhotoEditorImageViewListener.OnSingleTapUpCallback() {
                            @Override
                            public void onSingleTapUp() {
                                clearHelperBox();
                            }
                        }
                )
        );


        backgroundImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               /* HideEditTextWhileBlur();
                clearHelperBox();*/
                return mDetector.onTouchEvent(event);
            }
        });
    }

    private void HideEditTextWhileBlur() {
        View view = viewState.getCurrentSelectedView();
        if (view != null) {
            final TextView textInputTv = view.findViewById(R.id.tvPhotoEditorText);
            final EditText textInputEt = view.findViewById(R.id.etPhotoEditorText);
            if (textInputTv != null && textInputEt != null && textInputEt.getVisibility() == View.VISIBLE) {
                InputMethodManager imm = (InputMethodManager) view.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                textInputEt.setVisibility(View.GONE);
                textInputTv.setVisibility(View.VISIBLE);
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int height = view.getMeasuredHeight();
                int width = view.getMeasuredWidth();
                view.setLayoutParams(new FrameLayout.LayoutParams(width, height));
                mOnPhotoEditorListener.endEditText();
            }
        }
    }

    /**
     * This will add image on {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param desiredImage bitmap image you want to add
     */
    public void addImage(Bitmap desiredImage, float x, float y, String uuid, float height, float width, float rotation, float px, float py, RectF cropRect, float pr, boolean isMovable, RectF mediaBox) {
        posX = x;
        posY = y;
        this.px = px;
        this.py = py;
        this.rotation = rotation;
        final View imageRootView = getLayout(ViewType.IMAGE);
        imageRootView.setTag(uuid);

        final ImageView imageView = imageRootView.findViewById(R.id.imgPhotoEditorImage);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();

        if (height > 0.0 && width > 0.0) {
            params.height = (int) height;
            params.width = (int) width;
        } else {
            imageRootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            params.height = imageView.getHeight();
            params.width = imageView.getWidth();
        }

        imageRootView.setLayoutParams(params);
        if (desiredImage != null) {
            imageView.setImageBitmap(desiredImage);
        }

        if (cropRect != null && (!(cropRect.height() == height && cropRect.width() == width && cropRect.left == 0.0 && cropRect.top == 0.0))) {
            FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams((int) cropRect.width(), (int) cropRect.height());
            imageView.setLayoutParams(parms);
            imageView.setRotation(pr);

            imageView.post(new Runnable() {
                @Override
                public void run() {
                    ViewUtil.setTransformedX(imageView, px);
                    ViewUtil.setTransformedY(imageView, py);
                }
            });
        } else {
            FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams((int) width, (int) height);
            imageView.setLayoutParams(parms);
            imageView.setX(0.0f);
            imageView.setY(0.0f);
        }

        imageRootView.setOnTouchListener(getMultiTouchListener(imageRootView, isMovable));

        clearHelperBox();
        addViewToParent(imageRootView, ViewType.IMAGE, mediaBox);
        viewState.setCurrentSelectedView(imageRootView);
        if (elementSelectionListener != null) {
            elementSelectionListener.onElementSelectedDeselected(imageRootView, true, false);
        }

        imageRootView.post(new Runnable() {
            @Override
            public void run() {
                handleDefaultImage(imageRootView);
            }
        });
    }

    public void handleDefaultImage(View imageRootView) {
        ImageView imageView = imageRootView.findViewById(R.id.imgPhotoEditorImage);
        FrameLayout defaultImageFrameLayout = imageRootView.findViewById(R.id.defaultImageFrameLayout);
        if (imageView.getDrawable() == null) {
            defaultImageFrameLayout.setVisibility(View.VISIBLE);
            ImageView defaultImageView = imageRootView.findViewById(R.id.imagePhotoEditorDefaultImage);
            defaultImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_default_image));
        } else {
            defaultImageFrameLayout.setVisibility(View.GONE);
        }
    }

    private MultiTouchListener getMultiTouchListener(View rootView, boolean isMovable) {
        final FrameLayout frmBorder = rootView.findViewById(R.id.border);
        MultiTouchListener multiTouchListener = getMultiTouchListener(true, isMovable);
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                clearHelperBox();
                if (frmBorder != null) {
                    frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
                }
                viewState.setCurrentSelectedView(rootView);
                if (elementSelectionListener != null) {
                    elementSelectionListener.onElementSelectedDeselected(rootView, true, true);
                }
            }

            @Override
            public void onLongClick() {

            }
        });
        return multiTouchListener;
    }

    public void addImage(Bitmap desiredImage) {
        addImage(desiredImage, -1, -1, "" + System.currentTimeMillis(), desiredImage.getHeight(), desiredImage.getWidth(), 0, 0.0f, 0.0f, null, 0.0f, true, null);
    }


    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text, final int colorCodeTextView) {
        addText(null, text, colorCodeTextView);
    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param textTypeface      typeface for custom font in the text
     * @param text              text to display
     * @param colorCodeTextView text color to be displayed
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(@Nullable Typeface textTypeface, String text, final int colorCodeTextView) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();

        styleBuilder.withTextColor(colorCodeTextView);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        addText(text, styleBuilder);
    }

    /**
     * This add the text on the {@link PhotoEditorView} with provided parameters
     * by default {@link TextView#setText(int)} will be 18sp
     *
     * @param text         text to display
     * @param styleBuilder text style builder with your style
     */
    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text, @Nullable TextStyleBuilder styleBuilder) {
        addText(text, styleBuilder, -1, -1, "" + System.currentTimeMillis(), 0, 0.0f, 0.0f, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true, null);
    }

    public void addText(String text, TextStyleBuilder styleBuilder, float x, float y, String uuid, float rotation, float px, float py, float height, float width, boolean isMovable, RectF mediaBox) {
        posX = x;
        posY = y;
        this.rotation = rotation;
        this.px = px;
        this.py = py;
        brushDrawingView.setBrushDrawingMode(false);
        final View textRootView = getLayout(ViewType.TEXT);
        textRootView.setTag(uuid);
        final TextView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);
        final EditText textInputEt = textRootView.findViewById(R.id.etPhotoEditorText);
        //  final ImageView imgClose = textRootView.findViewById(R.id.imgPhotoEditorClose);
        final FrameLayout frmBorder = textRootView.findViewById(R.id.frmBorder);


        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textInputTv.getLayoutParams();

        if (height > 0.0) {
            params.height = (int) height + DEFAULT_EXTRA_WIDTH_HEIGHT;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        if (width > 0.0) {
            params.width = (int) width;
        }

        textRootView.setLayoutParams(params);


        textInputTv.setText(text);
        textInputTv.setGravity(Gravity.CENTER);
        textInputEt.setText(text);
        textInputEt.setGravity(Gravity.CENTER);

        if (styleBuilder != null) {
            styleBuilder.applyStyle(textInputTv);
            styleBuilder.applyStyle(textInputEt);
        }

        MultiTouchListener multiTouchListener = getMultiTouchListener(textRootView, isMovable);


        textInputEt.addTextChangedListener(getTextWatcher(textInputTv));

        textRootView.setOnTouchListener(multiTouchListener);
        clearHelperBox();
        addViewToParent(textRootView, ViewType.TEXT, mediaBox);

        textRootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            params.height = textRootView.getMeasuredHeight() + DEFAULT_EXTRA_WIDTH_HEIGHT;
        }
        if (width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            params.width = textRootView.getMeasuredWidth() + DEFAULT_EXTRA_WIDTH_HEIGHT;
        }
        textRootView.setLayoutParams(params);

        // Change the in-focus view
        viewState.setCurrentSelectedView(textRootView);
        if (elementSelectionListener != null) {
            elementSelectionListener.onElementSelectedDeselected(textRootView, true, false);
        }
    }

    private TextWatcher getTextWatcher(TextView textInputTv) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputTv.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public OnElementSelectionListener getElementSelectionListener() {
        return elementSelectionListener;
    }

    public void editText(View view) {
        mOnPhotoEditorListener.beginEditText();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        view.setLayoutParams(params);

        final TextView textInputTv = view.findViewById(R.id.tvPhotoEditorText);
        final EditText textInputEt = view.findViewById(R.id.etPhotoEditorText);
        textInputEt.setText(textInputTv.getText());
        textInputEt.setTypeface(textInputTv.getTypeface());
        textInputEt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textInputTv.getTextSize());
        textInputEt.setTextColor(textInputTv.getTextColors());
        textInputEt.setGravity(textInputTv.getGravity());
        textInputEt.setVisibility(View.VISIBLE);
        textInputTv.setVisibility(View.GONE);
        textInputEt.requestFocus();
        textInputTv.setLayoutParams(params);
        InputMethodManager imm = (InputMethodManager) textInputEt.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(textInputEt, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * This will update text and color on provi
     * ded view
     *
     * @param view      view on which you want update
     * @param inputText text to update {@link TextView}
     * @param colorCode color to update on {@link TextView}
     */
    public void editText(@NonNull View view, String inputText, @NonNull int colorCode) {
        editText(view, null, inputText, colorCode);
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param textTypeface update typeface for custom font in the text
     * @param inputText    text to update {@link TextView}
     * @param colorCode    color to update on {@link TextView}
     */
    public void editText(@NonNull View view, @Nullable Typeface textTypeface, String inputText, @NonNull int colorCode) {
        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
        styleBuilder.withTextColor(colorCode);
        if (textTypeface != null) {
            styleBuilder.withTextFont(textTypeface);
        }

        editText(view, inputText, styleBuilder);
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param inputText    text to update {@link TextView}
     * @param styleBuilder style to apply on {@link TextView}
     */
    public void editText(@NonNull View view, String inputText, @Nullable TextStyleBuilder styleBuilder) {
        TextView inputTextView = view.findViewById(R.id.tvPhotoEditorText);
        if (inputTextView != null && viewState.containsAddedView(view) && !TextUtils.isEmpty(inputText)) {
            inputTextView.setText(inputText);
            if (styleBuilder != null)
                styleBuilder.applyStyle(inputTextView);

            parentView.updateViewLayout(view, view.getLayoutParams());
            viewState.replaceAddedView(view);
        }
    }

    /**
     * Adds emoji to the {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param emojiName unicode in form of string to display emoji
     */
    public void addEmoji(String emojiName) {
        addEmoji(null, emojiName);
    }

    /**
     * Adds emoji to the {@link PhotoEditorView} which you drag,rotate and scale using pinch
     * if {@link Builder#setPinchTextScalable(boolean)} enabled
     *
     * @param emojiTypeface typeface for custom font to show emoji unicode in specific font
     * @param emojiName     unicode in form of string to display emoji
     */
    public void addEmoji(Typeface emojiTypeface, String emojiName) {
        brushDrawingView.setBrushDrawingMode(false);
        final View emojiRootView = getLayout(ViewType.EMOJI);
        final TextView emojiTextView = emojiRootView.findViewById(R.id.tvPhotoEditorText);
        final FrameLayout frmBorder = emojiRootView.findViewById(R.id.frmBorder);
        //  final ImageView imgClose = emojiRootView.findViewById(R.id.imgPhotoEditorClose);

        if (emojiTypeface != null) {
            emojiTextView.setTypeface(emojiTypeface);
        }
        emojiTextView.setTextSize(56);
        emojiTextView.setText(emojiName);
        MultiTouchListener multiTouchListener = getMultiTouchListener(true, true);
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                clearHelperBox();
                frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
                //imgClose.setVisibility(View.VISIBLE);

                // Change the in-focus view
                viewState.setCurrentSelectedView(emojiRootView);
                if (elementSelectionListener != null) {
                    elementSelectionListener.onElementSelectedDeselected(emojiRootView, true, true);
                }
            }

            @Override
            public void onLongClick() {
            }
        });
        emojiRootView.setOnTouchListener(multiTouchListener);
        clearHelperBox();
        addViewToParent(emojiRootView, ViewType.EMOJI, null);

        // Change the in-focus view
        viewState.setCurrentSelectedView(emojiRootView);
        if (elementSelectionListener != null) {
            elementSelectionListener.onElementSelectedDeselected(emojiRootView, true, false);
        }
    }


    /**
     * Add to root view from image,emoji and text to our parent view
     *
     * @param rootView rootview of image,text and emoji
     */
    private void addViewToParent(View rootView, ViewType viewType, RectF mediaBox) {
        if (px != -1 || py != -1) {
            rootView.setPivotX(px);
            rootView.setPivotY(py);
        }
        rootView.setRotation(rotation);
        px = -1;
        py = -1;
        rotation = -1;


        parentView.addView(rootView);
        rootView.setZ(zIndexCount);
        rootView.setVisibility(View.INVISIBLE);
        zIndexCount++;
        FrameLayout frmBorder = rootView.findViewById(R.id.frmBorder);
        if (posX != -1 && posY != -1) {
            int leftMargin = ((FrameLayout.LayoutParams) frmBorder.getLayoutParams()).leftMargin;
            int topMargin = ((FrameLayout.LayoutParams) frmBorder.getLayoutParams()).topMargin;
            Pair<Float, Float> coordinates = new Pair<>(posX - leftMargin, posY - topMargin);
            frmBorder.setTag(coordinates);

            rootView.post(new Runnable() {
                @Override
                public void run() {
                    FrameLayout frmBorder = rootView.findViewById(R.id.frmBorder);
                    Pair<Float, Float> coordinates = (Pair<Float, Float>) frmBorder.getTag();
                    rootView.setX(coordinates.first - leftMargin);
                    rootView.setY(coordinates.second - topMargin);
                    rootView.setVisibility(View.VISIBLE);
                    if (mOnPhotoEditorListener != null)
                        mOnPhotoEditorListener.onAddViewListener(viewType);
                }
            });


            posX = -1;
            posY = -1;


        } else {

            Pair<Float, Float> coordinates = new Pair<>((float) ((parentView.getWidth() / 2.0) - (rootView.getWidth() / 2.0)), (float) ((parentView.getHeight() / 2.0) - (rootView.getHeight() / 2.0)));
            frmBorder.setTag(coordinates);

            rootView.post(new Runnable() {
                @Override
                public void run() {
                    if (mediaBox != null && mediaBox.width() < parentView.getWidth()) {
                        if (mediaBox.left > 0) {
                            //Right visible area
                            rootView.setX((float) ((mediaBox.width() / 2.0) - (rootView.getWidth() / 2.0)) + mediaBox.left);
                        } else {
                            //Left visible area
                            rootView.setX((float) ((mediaBox.width() / 2.0) - (rootView.getWidth() / 2.0)));
                        }
                    } else {
                        rootView.setX((float) ((parentView.getWidth() / 2.0) - (rootView.getWidth() / 2.0)));

                    }

                    rootView.setY((float) ((parentView.getHeight() / 2.0) - (rootView.getHeight() / 2.0)));
                    rootView.setVisibility(View.VISIBLE);
                    if (mOnPhotoEditorListener != null)
                        mOnPhotoEditorListener.onAddViewListener(viewType);
                }
            });


        }
        viewState.addAddedView(rootView);
    }

    public ArrayList<View> getMediaElements() {
        ArrayList<View> mediaList = new ArrayList<>();
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View view = parentView.getChildAt(i);
            if (view.getTag() != null && view instanceof FrameLayout) {
                mediaList.add(view);
            }
        }

        return mediaList;
    }

    private void sortMedia(ArrayList<View> mediaList) {
        Collections.sort(mediaList, (s1, s2) -> Float.compare(s1.getZ(), s2.getZ()));
    }

    private void setZIndexToViews(ArrayList<View> mediaList) {
        for (int i = 0; i < mediaList.size(); i++) {
            mediaList.get(i).setZ(ZINDEX_MEDIA + i);
        }
    }

    public void toFront() {
        ArrayList<View> mediaList = getMediaElements();
        View currentSelectedView = viewState.getCurrentSelectedView();
        if (currentSelectedView != null) {
            currentSelectedView.setZ(currentSelectedView.getZ() + 1.5f);
            sortMedia(mediaList);
            setZIndexToViews(mediaList);
        }
    }

    public void toBack() {
        ArrayList<View> mediaList = getMediaElements();
        View currentSelectedView = viewState.getCurrentSelectedView();
        if (currentSelectedView != null) {
            currentSelectedView.setZ(currentSelectedView.getZ() - 1.5f);
            sortMedia(mediaList);
            setZIndexToViews(mediaList);
        }
    }

    public void toForward() {
        ArrayList<View> mediaList = getMediaElements();
        View currentSelectedView = viewState.getCurrentSelectedView();
        if (currentSelectedView != null) {
            currentSelectedView.setZ(Integer.MAX_VALUE);
            sortMedia(mediaList);
            setZIndexToViews(mediaList);
        }
    }

    public void toBackward() {
        ArrayList<View> mediaList = getMediaElements();
        View currentSelectedView = viewState.getCurrentSelectedView();
        if (currentSelectedView != null) {
            currentSelectedView.setZ(ZINDEX_MEDIA - 1);
            sortMedia(mediaList);
            setZIndexToViews(mediaList);
        }
    }

    /**
     * Create a new instance and scalable touchview
     *
     * @param isPinchScalable true if make pinch-scalable, false otherwise.
     * @return scalable multitouch listener
     */
    @NonNull
    public MultiTouchListener getMultiTouchListener(final boolean isPinchScalable, final boolean isMovable) {
        MultiTouchListener multiTouchListener = new MultiTouchListener(
                deleteView,
                parentView,
                this.backgroundImageView,
                isPinchScalable,
                mOnPhotoEditorListener,
                this.viewState,
                this.undoRedoController,
                isMovable);

        //multiTouchListener.setOnMultiTouchListener(this);

        return multiTouchListener;
    }

    /**
     * Get root view by its type i.e image,text and emoji
     *
     * @param viewType image,text or emoji
     * @return rootview
     */
    private View getLayout(final ViewType viewType) {
        View rootView = null;
        switch (viewType) {
            case TEXT:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextView txtText = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtText != null && mDefaultTextTypeface != null) {
                    txtText.setGravity(Gravity.CENTER);
                    if (mDefaultEmojiTypeface != null) {
                        txtText.setTypeface(mDefaultTextTypeface);
                    }
                }
                break;
            case IMAGE:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_image, null);
                break;
            case EMOJI:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextView txtTextEmoji = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtTextEmoji != null) {
                    if (mDefaultEmojiTypeface != null) {
                        txtTextEmoji.setTypeface(mDefaultEmojiTypeface);
                    }
                    txtTextEmoji.setGravity(Gravity.CENTER);
                    txtTextEmoji.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                break;
        }

        if (rootView != null) {
            //We are setting tag as ViewType to identify what type of the view it is
            //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
            rootView.setTag(viewType);
            /*final ImageView imgClose = rootView.findViewById(R.id.imgPhotoEditorClose);
            final View finalRootView = rootView;
            if (imgClose != null) {
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewUndo(finalRootView, viewType);
                    }
                });
            }*/
        }
        return rootView;
    }

    /**
     * Enable/Disable drawing mode to draw on {@link PhotoEditorView}
     *
     * @param brushDrawingMode true if mode is enabled
     */
    public void setBrushDrawingMode(boolean brushDrawingMode) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushDrawingMode(brushDrawingMode);
    }

    /**
     * @return true is brush mode is enabled
     */
    public Boolean getBrushDrawableMode() {
        return brushDrawingView != null && brushDrawingView.getBrushDrawingMode();
    }

    /**
     * set the size of bursh user want to paint on canvas i.e {@link BrushDrawingView}
     *
     * @param size size of brush
     */
    public void setBrushSize(float size) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushSize(size);
    }

    /**
     * set opacity/transparency of brush while painting on {@link BrushDrawingView}
     *
     * @param opacity opacity is in form of percentage
     */
    public void setOpacity(@IntRange(from = 0, to = 100) int opacity) {
        if (brushDrawingView != null) {
            opacity = (int) ((opacity / 100.0) * 255.0);
            brushDrawingView.setOpacity(opacity);
        }
    }

    /**
     * set brush color which user want to paint
     *
     * @param color color value for paint
     */
    public void setBrushColor(@ColorInt int color) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushColor(color);
    }

    /**
     * set the eraser size
     * <br></br>
     * <b>Note :</b> Eraser size is different from the normal brush size
     *
     * @param brushEraserSize size of eraser
     */
    public void setBrushEraserSize(float brushEraserSize) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushEraserSize(brushEraserSize);
    }

    void setBrushEraserColor(@ColorInt int color) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushEraserColor(color);
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushEraserSize(float)
     */
    public float getEraserSize() {
        return brushDrawingView != null ? brushDrawingView.getEraserSize() : 0;
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushSize(float)
     */
    public float getBrushSize() {
        if (brushDrawingView != null)
            return brushDrawingView.getBrushSize();
        return 0;
    }

    /**
     * @return provide the size of eraser
     * @see PhotoEditor#setBrushColor(int)
     */
    public int getBrushColor() {
        if (brushDrawingView != null)
            return brushDrawingView.getBrushColor();
        return 0;
    }

    /**
     * <p>
     * Its enables eraser mode after that whenever user drags on screen this will erase the existing
     * paint
     * <br>
     * <b>Note</b> : This eraser will work on paint views only
     * <p>
     */
    public void brushEraser() {
        if (brushDrawingView != null)
            brushDrawingView.brushEraser();
    }

    private void viewUndo(View removedView, ViewType viewType) {
        if (viewState.containsAddedView(removedView)) {
            parentView.removeView(removedView);
            viewState.removeAddedView(removedView);
            viewState.pushRedoView(removedView);
            if (mOnPhotoEditorListener != null) {
                mOnPhotoEditorListener.onRemoveViewListener(
                        viewType,
                        viewState.getAddedViewsCount()
                );
            }
        }
    }

    public void deleteSelectedView() {
        //viewUndo(viewState.getCurrentSelectedView(), null);
        // undoRedoController.addAddedView(viewState.getCurrentSelectedView(), true, false);
        parentView.removeView(viewState.getCurrentSelectedView());
        clearHelperBox();
    }

    public void undo() {
        if (undoRedoController.getAddedViewsCount() > 0) {
            View removeView = undoRedoController.getAddedView(
                    undoRedoController.getAddedViewsCount() - 1, parentView);
            int freqOfItem = undoRedoController.getFreqOfItemInAddedViews(removeView.getTag().toString());
            if (freqOfItem == 1) {
                undoRedoController.markAsDeleted(undoRedoController.getAddedViewsCount() - 1);
                undoRedoController.removeAddedView(undoRedoController.getAddedViewsCount() - 1);
                parentView.removeView(removeView);
                undoRedoController.pushRedoView(removeView, true, false);
            } else {
                VirtualView virtualView = undoRedoController.removeAddedView(undoRedoController.getAddedViewsCount() - 1);
                undoRedoController.pushRedoView(removeView, virtualView.isDeleted, virtualView.isRemoved);
                View latestView = undoRedoController.getLatestAddedViewWithUuid(
                        removeView.getTag().toString(), parentView);

            }
        }
    }

    public void redo() {
        if (undoRedoController.getRedoViewsCount() > 0) {
            View redoView = undoRedoController.getRedoView(
                    undoRedoController.getRedoViewsCount() - 1, parentView);
            VirtualView virtualView = undoRedoController.popRedoView();
            // undoRedoController.addAddedView(redoView, virtualView.isRemoved, true);
        }
    }


    /**
     * Undo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to undo
     */
   /* public boolean undo() {
        if (viewState.getAddedViewsCount() > 0) {
            View removeView = viewState.getAddedView(
                    viewState.getAddedViewsCount() - 1
            );
            if (removeView instanceof BrushDrawingView) {
                return brushDrawingView != null && brushDrawingView.undo();
            } else {
                viewState.removeAddedView(viewState.getAddedViewsCount() - 1);
                parentView.removeView(removeView);
                viewState.pushRedoView(removeView);

                View nextView = viewState.getAddedView(
                        viewState.getAddedViewsCount() - 1
                );
                if(nextView.getTag() != null && nextView.getTag().toString().equals(removeView.getTag().toString()) ) {
                    parentView.addView(nextView);
                }
            }
            if (mOnPhotoEditorListener != null) {
                Object viewTag = removeView.getTag();
                if (viewTag != null && viewTag instanceof ViewType) {
                    mOnPhotoEditorListener.onRemoveViewListener(
                            (ViewType) viewTag,
                            viewState.getAddedViewsCount()
                    );
                }
            }
        }
        return viewState.getAddedViewsCount() != 0;
    }

    */

    /**
     * Redo the last operation perform on the {@link PhotoEditor}
     *
     * @return true if there nothing more to redo
     *//*
    public boolean redo() {
        if (viewState.getRedoViewsCount() > 0) {
            View redoView = viewState.getRedoView(
                    viewState.getRedoViewsCount() - 1
            );
            if (redoView instanceof BrushDrawingView) {
                return brushDrawingView != null && brushDrawingView.redo();
            } else {
                viewState.popRedoView();
                parentView.addView(redoView);
                viewState.addAddedView(redoView);
            }
            Object viewTag = redoView.getTag();
            if (mOnPhotoEditorListener != null && viewTag != null && viewTag instanceof ViewType) {
                mOnPhotoEditorListener.onAddViewListener(
                        (ViewType) viewTag,
                        viewState.getAddedViewsCount()
                );
            }
        }
        return viewState.getRedoViewsCount() != 0;
    }
*/
    private void clearBrushAllViews() {
        if (brushDrawingView != null)
            brushDrawingView.clearAll();
    }

    /**
     * Removes all the edited operations performed {@link PhotoEditorView}
     * This will also clear the undo and redo stack
     */
    public void clearAllViews() {
        for (int i = 0; i < viewState.getAddedViewsCount(); i++) {
            parentView.removeView(viewState.getAddedView(i));
        }
        if (viewState.containsAddedView(brushDrawingView)) {
            parentView.addView(brushDrawingView);
        }
        viewState.clearAddedViews();
        viewState.clearRedoViews();
        clearBrushAllViews();
    }

    /**
     * Remove all helper boxes from views
     */
    @UiThread
    public void clearHelperBox() {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View childAt = parentView.getChildAt(i);
            FrameLayout frmBorder = childAt.findViewById(R.id.border);
            if (frmBorder != null) {
                frmBorder.setBackgroundResource(0);
            }
            /*ImageView imgClose = childAt.findViewById(R.id.imgPhotoEditorClose);
            if (imgClose != null) {
                imgClose.setVisibility(View.GONE);
            }*/
        }
        if (elementSelectionListener != null) {
            elementSelectionListener.onElementSelectedDeselected(viewState.getCurrentSelectedView(), false, false);
        }
        viewState.clearCurrentSelectedView();

    }

    /**
     * Setup of custom effect using effect type and set parameters values
     *
     * @param customEffect {@link CustomEffect.Builder#setParameter(String, Object)}
     */
    public void setFilterEffect(CustomEffect customEffect) {
        parentView.setFilterEffect(customEffect);
    }

    /**
     * Set pre-define filter available
     *
     * @param filterType type of filter want to apply {@link PhotoEditor}
     */
    public void setFilterEffect(PhotoFilter filterType) {
        parentView.setFilterEffect(filterType);
    }

    public void setBleedThickness(int bleedThickness) {
        parentView.setBleedThickness(bleedThickness);
    }

    public void setEditorScale(double smallestScaleFactor) {
        this.editorScale = smallestScaleFactor;
        ViewUtil.setScale(smallestScaleFactor);
    }

    /**
     * A callback to save the edited image asynchronously
     */
    public interface OnSaveListener {

        /**
         * Call when edited image is saved successfully on given path
         *
         * @param imagePath path on which image is saved
         */
        void onSuccess(@NonNull String imagePath);

        /**
         * Call when failed to saved image on given path
         *
         * @param exception exception thrown while saving image
         */
        void onFailure(@NonNull Exception exception);
    }

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath,
                           @NonNull final OnSaveListener onSaveListener) {
        saveAsFile(imagePath, new SaveSettings.Builder().build(), onSaveListener);
    }

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param saveSettings   builder for multiple save options {@link SaveSettings}
     * @param onSaveListener callback for saving image
     * @see OnSaveListener
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void saveAsFile(@NonNull final String imagePath,
                           @NonNull final SaveSettings saveSettings,
                           @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Exception>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        brushDrawingView.destroyDrawingCache();
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    protected Exception doInBackground(String... strings) {
                        // Create a media file name
                        File file = new File(imagePath);
                        try {
                            FileOutputStream out = new FileOutputStream(file, false);
                            if (parentView != null) {
                                Bitmap capturedBitmap = saveSettings.isTransparencyEnabled()
                                        ? BitmapUtil.removeTransparency(captureView(parentView))
                                        : captureView(parentView);
                                capturedBitmap.compress(saveSettings.getCompressFormat(), saveSettings.getCompressQuality(), out);
                            }
                            out.flush();
                            out.close();
                            Log.d(TAG, "Filed Saved Successfully");
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save File");
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e == null) {
                            //Clear all views if its enabled in save settings
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveListener.onSuccess(imagePath);
                        } else {
                            onSaveListener.onFailure(e);
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveListener.onFailure(e);
            }
        });
    }

    /**
     * Save the edited image as bitmap
     *
     * @param onSaveBitmap callback for saving image as bitmap
     * @see OnSaveBitmap
     */
    @SuppressLint("StaticFieldLeak")
    public void saveAsBitmap(@NonNull final OnSaveBitmap onSaveBitmap) {
        saveAsBitmap(new SaveSettings.Builder().build(), onSaveBitmap);
    }

    /**
     * Save the edited image as bitmap
     *
     * @param saveSettings builder for multiple save options {@link SaveSettings}
     * @param onSaveBitmap callback for saving image as bitmap
     * @see OnSaveBitmap
     */
    @SuppressLint("StaticFieldLeak")
    public void saveAsBitmap(@NonNull final SaveSettings saveSettings,
                             @NonNull final OnSaveBitmap onSaveBitmap) {
        parentView.saveFilter(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                new AsyncTask<String, String, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        clearHelperBox();
                        brushDrawingView.destroyDrawingCache();
                    }

                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        if (parentView != null) {
                            return saveSettings.isTransparencyEnabled() ?
                                    BitmapUtil.removeTransparency(captureView(parentView))
                                    : captureView(parentView);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null) {
                            if (saveSettings.isClearViewsEnabled()) clearAllViews();
                            onSaveBitmap.onBitmapReady(bitmap);
                        } else {
                            onSaveBitmap.onFailure(new Exception("Failed to load the bitmap"));
                        }
                    }

                }.execute();
            }

            @Override
            public void onFailure(Exception e) {
                onSaveBitmap.onFailure(e);
            }
        });
    }

    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Callback on editing operation perform on {@link PhotoEditorView}
     *
     * @param onPhotoEditorListener {@link OnPhotoEditorListener}
     */
    public void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener) {
        this.mOnPhotoEditorListener = onPhotoEditorListener;
    }


    public void setOnElementSelectionListener(@NonNull OnElementSelectionListener
                                                      onElementSelectionListener) {
        this.elementSelectionListener = onElementSelectionListener;
    }

    /**
     * Check if any changes made need to save
     *
     * @return true if nothing is there to change
     */
    public boolean isCacheEmpty() {
        return viewState.getAddedViewsCount() == 0 && viewState.getRedoViewsCount() == 0;
    }


    @Override
    public void onViewAdd(BrushDrawingView brushDrawingView) {
        if (viewState.getRedoViewsCount() > 0) {
            viewState.popRedoView();
        }
        viewState.addAddedView(brushDrawingView);
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onAddViewListener(
                    ViewType.BRUSH_DRAWING
            );
        }
    }

    @Override
    public void onViewRemoved(BrushDrawingView brushDrawingView) {
        if (viewState.getAddedViewsCount() > 0) {
            View removeView = viewState.removeAddedView(
                    viewState.getAddedViewsCount() - 1
            );
            if (!(removeView instanceof BrushDrawingView)) {
                parentView.removeView(removeView);
            }
            viewState.pushRedoView(removeView);
        }
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onRemoveViewListener(
                    ViewType.BRUSH_DRAWING,
                    viewState.getAddedViewsCount()
            );
        }
    }

    @Override
    public void onStartDrawing() {
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onStartViewChangeListener("");
        }
    }

    @Override
    public void onStopDrawing() {
        if (mOnPhotoEditorListener != null) {
            mOnPhotoEditorListener.onStopViewChangeListener("");
        }
    }


    /**
     * Builder pattern to define {@link PhotoEditor} Instance
     */
    public static class Builder {

        private Context context;
        private PhotoEditorView parentView;
        private ImageView imageView;
        private View deleteView;
        private BrushDrawingView brushDrawingView;
        private Typeface textTypeface;
        private Typeface emojiTypeface;
        // By default, pinch-to-scale is enabled for text
        private boolean isTextPinchScalable = true;
        private double editorScale = 1.0;


        /**
         * Building a PhotoEditor which requires a Context and PhotoEditorView
         * which we have setup in our xml layout
         *
         * @param context         context
         * @param photoEditorView {@link PhotoEditorView}
         */
        public Builder(Context context, PhotoEditorView photoEditorView) {
            this.context = context;
            parentView = photoEditorView;
            imageView = photoEditorView.getSource();
            brushDrawingView = photoEditorView.getBrushDrawingView();
        }

        Builder setDeleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        Builder setEditorScale(double scale) {
            this.editorScale = scale;
            return this;
        }

        /**
         * set default text font to be added on image
         *
         * @param textTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultTextTypeface(Typeface textTypeface) {
            this.textTypeface = textTypeface;
            return this;
        }

        /**
         * set default font specific to add emojis
         *
         * @param emojiTypeface typeface for custom font
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setDefaultEmojiTypeface(Typeface emojiTypeface) {
            this.emojiTypeface = emojiTypeface;
            return this;
        }

        /**
         * Set false to disable pinch-to-scale for text inserts.
         * Set to "true" by default.
         *
         * @param isTextPinchScalable flag to make pinch to zoom for text inserts.
         * @return {@link Builder} instant to build {@link PhotoEditor}
         */
        public Builder setPinchTextScalable(boolean isTextPinchScalable) {
            this.isTextPinchScalable = isTextPinchScalable;
            return this;
        }

        /**
         * @return build PhotoEditor instance
         */
        public PhotoEditor build() {
            return new PhotoEditor(this);
        }

    }

    /**
     * Provide the list of emoji in form of unicode string
     *
     * @param context context
     * @return list of emoji unicode
     */
    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }

    public View getSelectedView() {
        return viewState.getCurrentSelectedView();
    }

    public void setSelectedView(View view) {
        clearHelperBox();
        if (view != null) {
            viewState.setCurrentSelectedView(view);
            final FrameLayout frmBorder = view.findViewById(R.id.frmBorder);
            if (frmBorder != null) {
                frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
            }
        }
    }


    public void addToUndoList(View view) {
        //undoRedoController.addAddedView(view);
    }

    public void duplicateMedia(String uuid) {
        boolean isText;
        FrameLayout selectedView = (FrameLayout) viewState.getCurrentSelectedView();
        ImageView imageView = selectedView.findViewById(R.id.imgPhotoEditorImage);
        FrameLayout duplicateMedia;
        if (imageView == null) {
            duplicateMedia = (FrameLayout) getLayout(ViewType.TEXT);
            isText = true;
        } else {
            duplicateMedia = (FrameLayout) getLayout(ViewType.IMAGE);
            isText = false;
        }

        copyCommonProperties(uuid, selectedView, duplicateMedia);

        if (isText) {
            copyTextProperties(selectedView, duplicateMedia);
        } else {
            copyImageProperties(selectedView, duplicateMedia);
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedView.getLayoutParams();
        duplicateMedia.setLayoutParams(params);
        duplicateMedia.setOnTouchListener(getMultiTouchListener(duplicateMedia, true));
        clearHelperBox();
        viewState.setCurrentSelectedView(duplicateMedia);
        parentView.addView(duplicateMedia);
        duplicateMedia.post(new Runnable() {
            @Override
            public void run() {
                duplicateMedia.setX(selectedView.getX() + MARGIN);
                duplicateMedia.setY(selectedView.getY() + MARGIN);
            }
        });

        viewState.addAddedView(duplicateMedia);
        //undoRedoController.addAddedView(duplicateMedia);
        if (elementSelectionListener != null) {
            elementSelectionListener.onElementSelectedDeselected(duplicateMedia, true, false);
        }

    }

    private void copyCommonProperties(String uuid, FrameLayout selectedView, FrameLayout duplicateMedia) {
        duplicateMedia.setX(selectedView.getX() + MARGIN);
        duplicateMedia.setY(selectedView.getY() + MARGIN);
        duplicateMedia.setZ(zIndexCount++);
        duplicateMedia.setTag("" + uuid);
        duplicateMedia.setScaleX(selectedView.getScaleX());
        duplicateMedia.setScaleY(selectedView.getScaleY());
        duplicateMedia.setRotation(selectedView.getRotation());
    }

    private void copyImageProperties(FrameLayout selectedView, FrameLayout duplicateMedia) {
        ImageView selectedImageView = selectedView.findViewById(R.id.imgPhotoEditorImage);
        ImageView duplicateImageView = duplicateMedia.findViewById(R.id.imgPhotoEditorImage);
        BitmapDrawable drawable = (BitmapDrawable) selectedImageView.getDrawable();
        duplicateImageView.setLayoutParams(selectedImageView.getLayoutParams());
        duplicateImageView.setRotation(selectedImageView.getRotation());
        duplicateImageView.setX(selectedImageView.getX());
        duplicateImageView.setY(selectedImageView.getY());
        duplicateImageView.setImageBitmap(drawable.getBitmap());
    }

    private void copyTextProperties(FrameLayout selectedView, FrameLayout duplicateMedia) {
        TextView textView = selectedView.findViewById(R.id.tvPhotoEditorText);
        TextView duplicateTextView = duplicateMedia.findViewById(R.id.tvPhotoEditorText);
        duplicateTextView.setText(textView.getText().toString());
        duplicateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize());
        if (textView.getTag() != null) {
            duplicateTextView.setTypeface(
                    Typeface.createFromAsset(
                            duplicateTextView.getContext().getAssets(),
                            "fonts/" + textView.getTag() + ".ttf"
                    )
            );
            duplicateTextView.setTag(textView.getTag());
        }
        duplicateTextView.setGravity(textView.getGravity());
        duplicateTextView.setTextColor(textView.getCurrentTextColor());
        duplicateTextView.setWidth(textView.getWidth());
        EditText duplicateEditText = duplicateMedia.findViewById(R.id.etPhotoEditorText);
        duplicateEditText.setGravity(textView.getGravity());
        duplicateEditText.setTextColor(textView.getCurrentTextColor());
        duplicateEditText.addTextChangedListener(getTextWatcher(duplicateTextView));
    }

    public void centerSelectedView() {
        View selectedView = viewState.getCurrentSelectedView();
        ViewUtil.setTransformedX(selectedView, (float) ((parentView.getWidth() / 2.0) - (ViewUtil.getTransformedWidth(selectedView) / 2.0)));
        ViewUtil.setTransformedY(selectedView, (float) ((parentView.getHeight() / 2.0) - (ViewUtil.getTransformedHeight(selectedView) / 2.0)));
    }

    public PhotoEditorView getParent() {
        return parentView;
    }

}
