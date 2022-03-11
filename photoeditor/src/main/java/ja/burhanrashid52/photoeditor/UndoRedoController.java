package ja.burhanrashid52.photoeditor;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.oginotihiro.cropview.CropView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoRedoController {

    private View currentSelectedView;
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
        if (!isFromRedo) {
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

        // This is trick to bypass the issue where element gives getX as 0
        FrameLayout frmBorder = view.findViewById(R.id.frmBorder);
        Pair<Float, Float> coordinates = (Pair<Float, Float>) frmBorder.getTag();

        if(view.getX() == 0.0 && coordinates != null) {
            virtualView.x = coordinates.first;
        } else {
            virtualView.x = view.getX();
        }

        if(view.getY() == 0.0 && coordinates != null) {
            virtualView.y = coordinates.second;
        } else {
            virtualView.y = view.getY();
        }

        virtualView.z = view.getZ();

        Log.i("UNDOREDO", "VirtualViewFromView view.getX(): " + view.getX() + " view.getY(): " + view.getY());
        virtualView.scaleX = view.getScaleX();
        virtualView.scaleY = view.getScaleY();
        virtualView.rotation = view.getRotation();
        virtualView.uuid = view.getTag().
                toString();
        virtualView.params = (FrameLayout.LayoutParams) view.getLayoutParams();
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        virtualView.height = view.getMeasuredHeight();
        virtualView.width = view.getMeasuredWidth();
        virtualView.isDeleted = isDeleted;
        virtualView.isRemoved = isRemoved;

        if (imageView != null) {
            CropView cropView = view.findViewById(R.id.cropZoomView);
            virtualView.pictureCoordinateRect = new RectF(cropView.getImageRect());
            virtualView.isText = false;
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            BitmapDrawable cropDrawable = (BitmapDrawable) cropView.getDrawable();
            virtualView.bitmap = cropDrawable.getBitmap();
            virtualView.croppedBitmap = drawable.getBitmap();
            virtualView.flipHorizontal = imageView.getScaleX();
            virtualView.flipVertical = imageView.getScaleY();
        } else {
            virtualView.isText = true;
            TextView textView = view.findViewById(R.id.tvPhotoEditorText);
            virtualView.text = textView.getText().toString();
            virtualView.fontSize = textView.getTextSize();
            if (textView.getTag() != null) {
                virtualView.fontFamily = textView.getTag().toString();
            }
            virtualView.textAlign = textView.getGravity();
            virtualView.color = textView.getCurrentTextColor();
        }
        return virtualView;
    }


    private View getViewFromVirtualView(VirtualView virtualView, PhotoEditorView parentView, boolean isFromUndo) {
        View view = null;

        if (virtualView.isDeleted || (virtualView.isRemoved && isFromUndo)) {
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
            } else {
                CropView cropView = view.findViewById(R.id.cropZoomView);
                cropView.of(virtualView.bitmap).asSquare().initialize(cropView.getContext());
            }

            parentView.addView(view);
        }
        view = parentView.findViewWithTag(virtualView.uuid);
        if (virtualView.isRemoved && !isFromUndo) {
            parentView.removeView(view);
        }
        if (view != null) {
            View finalView1 = view;
            FrameLayout frmBorder = view.findViewById(R.id.frmBorder);
            Log.i("UNDOREDO", "ViewVirtualFromView virtualView.X: " + virtualView.x + " virtualView.Y: " + virtualView.y);
            Pair<Float, Float> coordinates = new Pair<>(virtualView.x, virtualView.y);
            frmBorder.setTag(coordinates);
            view.post(new Runnable() {
                @Override
                public void run() {
                    FrameLayout frmBorder = finalView1.findViewById(R.id.frmBorder);
                    Pair<Float, Float> coordinates = (Pair<Float, Float>) frmBorder.getTag();
                    finalView1.setX(coordinates.first);
                    finalView1.setY(coordinates.second);
                }
            });

            view.setTag(virtualView.uuid);

            //view.setLayoutParams(virtualView.params);
            ViewGroup.LayoutParams params;
            if (virtualView.isText) {
                TextView textView = view.findViewById(R.id.tvPhotoEditorText);
                params = (ViewGroup.LayoutParams) textView.getLayoutParams();
                params.height = (int) virtualView.height;
                params.width = (int) virtualView.width;
                textView.setLayoutParams(params);
            } else {
                ImageView imageView = view.findViewById(R.id.imgPhotoEditorImage);
                params = (ViewGroup.LayoutParams) imageView.getLayoutParams();
                params.height = (int) virtualView.height;
                params.width = (int) virtualView.width;
                imageView.setLayoutParams(params);
            }
            view.setLayoutParams(params);

            setZ(virtualView, view, parentView);
            view.setScaleX(virtualView.scaleX);
            view.setScaleY(virtualView.scaleY);
            view.setRotation(virtualView.rotation);
            if (virtualView.isText) {
                TextView textView = view.findViewById(R.id.tvPhotoEditorText);
                textView.setText(virtualView.text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((int) virtualView.fontSize));
                textView.setGravity(virtualView.textAlign);
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
                CropView cropView = view.findViewById(R.id.cropZoomView);

                //imageView.setImageBitmap(virtualView.bitmap);
                //TODO need to change logic as per new crop px, py
               /* final CropImageView cropImageView = view.findViewById(R.id.imgCropImage);
                cropImageView.setImageBitmap(virtualView.bitmap);*/


                imageView.setImageBitmap(virtualView.croppedBitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                imageView.setScaleX(virtualView.flipHorizontal);
                imageView.setScaleY(virtualView.flipVertical);


                //cropView.of(virtualView.bitmap).asSquare().initialize(cropView.getContext());
                cropView.setExternalImageProperties(virtualView.pictureCoordinateRect);

            }
        }
        return view;
    }

    private void setZ(VirtualView virtualView, View view, PhotoEditorView parentView) {
        float prevZ = virtualView.z;
        float curZ = view.getZ();
        ArrayList<View> mediaList = getMediaElements(parentView);
        for (View v : mediaList) {
            if (v.getZ() == prevZ) {
                v.setZ(curZ);
                view.setZ(prevZ);
                break;
            }
        }
    }

    private MultiTouchListener getMultiTouchListener(final View finalView) {
        MultiTouchListener multiTouchListener = photoEditor.getMultiTouchListener(true, true);
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
        viewState.setCurrentSelectedView(view);
        if (photoEditor.getElementSelectionListener() != null) {
            photoEditor.getElementSelectionListener().onElementSelectedDeselected(view, true, false);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textView.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public ArrayList<View> getMediaElements(PhotoEditorView parentView) {
        ArrayList<View> mediaList = new ArrayList<>();
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View view = parentView.getChildAt(i);
            if (view.getTag() != null && view instanceof FrameLayout) {
                mediaList.add(view);
            }
        }

        return mediaList;
    }

}
