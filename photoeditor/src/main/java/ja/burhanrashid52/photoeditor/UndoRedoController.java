package ja.burhanrashid52.photoeditor;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoRedoController {

    private List<VirtualView> addedViews;
    private Stack<VirtualView> redoViews;
    private LayoutInflater mLayoutInflater;
    private PhotoEditorViewState viewState;
    private PhotoEditor photoEditor;

    UndoRedoController(PhotoEditorViewState viewState, PhotoEditor photoEditor) {
        addedViews = new ArrayList<>();
        redoViews = new Stack<>();
        this.viewState = viewState;
        this.photoEditor = photoEditor;
    }

    View getAddedView(int index, PhotoEditorView parentView) {
        VirtualView virtualView = addedViews.get(index);
        return getViewFromVirtualView(virtualView, parentView, true);
    }

    void addAddedView(final View view) {
        redoViews.clear();
        addedViews.add(getVirtualViewFromView(view));
    }

    void addAddedView(final View view, boolean isRemoved, boolean isFromRedo) {
        if(!isFromRedo) {
            redoViews.clear();
        }
        addedViews.add(getVirtualViewFromView(view, false, isRemoved));
    }

    void clearAddedViews() {
        addedViews.clear();
    }

    void removeAddedView(final VirtualView view) {
        addedViews.remove(view);
    }

    VirtualView removeAddedView(final int index) {
        return addedViews.remove(index);
    }

    void clearRedoViews() {
        redoViews.clear();
    }

    void pushRedoView(final View view, boolean isDeleted, boolean isRemoved) {
        redoViews.push(getVirtualViewFromView(view, isDeleted, isRemoved));
    }

    VirtualView popRedoView() {
        return redoViews.pop();
    }

    View getRedoView(int index, PhotoEditorView parentView) {
        VirtualView virtualView = redoViews.get(index);
        return getViewFromVirtualView(virtualView, parentView, false);
    }

    int getRedoViewsCount() {
        return redoViews.size();
    }

    int getAddedViewsCount() {
        return addedViews.size();
    }

    private VirtualView getVirtualViewFromView(View view) {
        return getVirtualViewFromView(view, false, false);
    }

    private VirtualView getVirtualViewFromView(View view, boolean isDeleted, boolean isRemoved) {
        ImageView imageView = view.findViewById(R.id.imgPhotoEditorImage);
        VirtualView virtualView = new VirtualView();
        virtualView.x = view.getX();
        virtualView.y = view.getY();
        virtualView.rotation = view.getRotation();
        virtualView.uuid = view.getTag().
                toString();
        virtualView.isDeleted = isDeleted;
        virtualView.isRemoved = isRemoved;
        if (imageView != null) {
            virtualView.isText = false;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            virtualView.bitmap = drawable.getBitmap();
        } else {
            virtualView.isText = true;
            TextView textView = view.findViewById(R.id.tvPhotoEditorText);
            virtualView.text = textView.getText().toString();
            virtualView.fontSize = textView.getTextSize();
            if (textView.getTag() != null) {
                virtualView.fontFamily = textView.getTag().toString();
            }
            virtualView.textAlign = textView.getTextAlignment();
            virtualView.color = textView.getCurrentTextColor();
        }
        return virtualView;
    }


    private View getViewFromVirtualView(VirtualView virtualView, PhotoEditorView parentView, boolean isFromUndo) {
        View view = null;

        if (virtualView.isDeleted) {
            if (mLayoutInflater == null) {
                mLayoutInflater = (LayoutInflater) parentView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            if (virtualView.isText) {
                view = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
            } else {
                view = mLayoutInflater.inflate(R.layout.view_photo_editor_image, null);
            }
            view.setTag(virtualView.uuid);
            final View finalView = view;
            setViewAsSelected(view);
            MultiTouchListener multiTouchListener = getMultiTouchListener(finalView);
            view.setOnTouchListener(multiTouchListener);

            if (virtualView.isText) {
                final TextView textView = view.findViewById(R.id.tvPhotoEditorText);
                EditText editText = view.findViewById(R.id.etPhotoEditorText);
                editText.addTextChangedListener(getTextWatcher(textView));
            }

            parentView.addView(view);
        }
        view = parentView.findViewWithTag(virtualView.uuid);
        if (virtualView.isRemoved && !isFromUndo) {
            parentView.removeView(view);
        }
        if (view != null) {
            view.setTag(virtualView.uuid);
            view.setX(virtualView.x);
            view.setY(virtualView.y);
            view.setRotation(virtualView.rotation);
            if (virtualView.isText) {
                TextView textView = view.findViewById(R.id.tvPhotoEditorText);
                textView.setText(virtualView.text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((int) virtualView.fontSize));
                textView.setTextAlignment(virtualView.textAlign);
                textView.setTextColor(virtualView.color);
                if (virtualView.fontFamily != null) {
                    textView.setTypeface(
                            Typeface.createFromAsset(
                                    textView.getContext().getAssets(),
                                    "fonts/" + virtualView.fontFamily + ".ttf"
                            )
                    );
                    textView.setTag(virtualView.fontFamily);
                }
            } else {
                ImageView imageView = view.findViewById(R.id.imgPhotoEditorImage);
                imageView.setImageBitmap(virtualView.bitmap);
            }
        }
        return view;
    }

    private MultiTouchListener getMultiTouchListener(final View finalView) {
        MultiTouchListener multiTouchListener = photoEditor.getMultiTouchListener(true);
        multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
            @Override
            public void onClick() {
                setViewAsSelected(finalView);
            }

            @Override
            public void onLongClick() {
                photoEditor.editText(finalView);
            }
        });
        return multiTouchListener;
    }

    private void setViewAsSelected(View view) {
        photoEditor.clearHelperBox();
        final FrameLayout frmBorder = view.findViewById(R.id.frmBorder);
        frmBorder.setBackgroundResource(R.drawable.rounded_border_tv);
        frmBorder.setTag(true);
        viewState.setCurrentSelectedView(view);
        if (photoEditor.getElementSelectionListener() != null) {
            photoEditor.getElementSelectionListener().onElementSelectedDeselected(view, true);
        }
    }

    public int getFreqOfItemInAddedViews(String uuid) {
        int i, count = 0;
        for (i = 0; i <= addedViews.size() - 1; i++) {
            if (uuid.equals(addedViews.get(i).uuid)) {
                count++;
            }
        }
        return count;
    }

    public void markAsDeleted(int index) {
        addedViews.get(index).isDeleted = true;
    }

    public View getLatestAddedViewWithUuid(String uuid, PhotoEditorView parentView) {
        VirtualView virtualView = getVirtualViewFromAddedViews(uuid);
        return getViewFromVirtualView(virtualView, parentView, true);
    }

    public VirtualView getVirtualViewFromAddedViews(String uuid) {
        VirtualView virtualView = null;
        for (int i = addedViews.size() - 1; i >= 0; i--) {
            if (uuid.equals(addedViews.get(i).uuid)) {
                virtualView = addedViews.get(i);
                break;
            }
        }
        return virtualView;
    }

    public TextWatcher getTextWatcher(final TextView textView) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textView.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
    }

}
