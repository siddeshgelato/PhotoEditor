package ja.burhanrashid52.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * <p>
 * This ViewGroup will have the {@link BrushDrawingView} to draw paint on it with {@link ImageView}
 * which our source image
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 1/18/2018
 */

public class PhotoEditorView extends RelativeLayout {

    private static final String TAG = "PhotoEditorView";

    private FilterImageView mImgSource;
    private BrushDrawingView mBrushDrawingView;
    private ImageFilterView mImageFilterView;
    private static final int imgSrcId = 1, brushSrcId = 2, glFilterId = 3;
    private View mHorizontalGuideLine = new View(getContext());
    private View mVerticalGuideLine = new View(getContext());
    private View mTopGuideLine = new View(getContext());
    private View mBottomGuideLine = new View(getContext());
    private View mLeftGuideLine = new View(getContext());
    private View mRightGuideLine = new View(getContext());

    public PhotoEditorView(Context context) {
        super(context);
        init(null);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @SuppressLint("Recycle")
    private void init(@Nullable AttributeSet attrs) {
        //Setup image attributes
        mImgSource = new FilterImageView(getContext());
        mImgSource.setId(imgSrcId);
        mImgSource.setAdjustViewBounds(true);
        LayoutParams imgSrcParam = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imgSrcParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PhotoEditorView);
            Drawable imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src);
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable);
                mImgSource.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }

        //Setup brush view
        mBrushDrawingView = new BrushDrawingView(getContext());
        mBrushDrawingView.setVisibility(GONE);
        mBrushDrawingView.setId(brushSrcId);
        //Align brush to the size of image view
        LayoutParams brushParam = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        brushParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        brushParam.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        brushParam.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);

        //Setup GLSurface attributes
        mImageFilterView = new ImageFilterView(getContext());
        mImageFilterView.setId(glFilterId);
        mImageFilterView.setVisibility(GONE);

        //Align brush to the size of image view
        LayoutParams imgFilterParam = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imgFilterParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imgFilterParam.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        imgFilterParam.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);

        mImgSource.setOnImageChangedListener(new FilterImageView.OnImageChangedListener() {
            @Override
            public void onBitmapLoaded(@Nullable Bitmap sourceBitmap) {
                mImageFilterView.setFilterEffect(PhotoFilter.NONE);
                mImageFilterView.setSourceBitmap(sourceBitmap);
                Log.d(TAG, "onBitmapLoaded() called with: sourceBitmap = [" + sourceBitmap + "]");
            }
        });

        //Add image source
        addView(mImgSource, imgSrcParam);

        //Add Gl FilterView
        addView(mImageFilterView, imgFilterParam);

        //Add brush view
        addView(mBrushDrawingView, brushParam);

        createGuideLine(mHorizontalGuideLine, ViewGroup.LayoutParams.MATCH_PARENT, 2, RelativeLayout.CENTER_IN_PARENT);
        createGuideLine(mVerticalGuideLine, 2, ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.CENTER_IN_PARENT);
        createGuideLine(mLeftGuideLine, ViewGroup.LayoutParams.MATCH_PARENT, 4, RelativeLayout.ALIGN_PARENT_START);
        createGuideLine(mRightGuideLine, ViewGroup.LayoutParams.MATCH_PARENT, 4, RelativeLayout.ALIGN_PARENT_END);
        createGuideLine(mTopGuideLine, 4, ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.ALIGN_PARENT_TOP);
        createGuideLine(mBottomGuideLine, 4, ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.ALIGN_PARENT_BOTTOM);

    }

    private void createGuideLine(View view, int height, int width, int constraint) {
        LayoutParams params = new LayoutParams(
                width, height);
        params.addRule(constraint, RelativeLayout.TRUE);
        view.setBackgroundColor(Color.MAGENTA);
        view.setLayoutParams(params);
        addView(view, params);
        view.setVisibility(View.GONE);
    }

    void setVisibilityOfGuideLines(boolean isVisible) {
        if (isVisible) {
            mHorizontalGuideLine.setVisibility(VISIBLE);
            mVerticalGuideLine.setVisibility(VISIBLE);
            mTopGuideLine.setVisibility(VISIBLE);
            mBottomGuideLine.setVisibility(VISIBLE);
            mLeftGuideLine.setVisibility(VISIBLE);
            mRightGuideLine.setVisibility(VISIBLE);
        } else {
            mHorizontalGuideLine.setVisibility(GONE);
            mVerticalGuideLine.setVisibility(GONE);
            mTopGuideLine.setVisibility(GONE);
            mBottomGuideLine.setVisibility(GONE);
            mLeftGuideLine.setVisibility(GONE);
            mRightGuideLine.setVisibility(GONE);
        }
    }


    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    public ImageView getSource() {
        return mImgSource;
    }

    BrushDrawingView getBrushDrawingView() {
        return mBrushDrawingView;
    }


    void saveFilter(@NonNull final OnSaveBitmap onSaveBitmap) {
        if (mImageFilterView.getVisibility() == VISIBLE) {
            mImageFilterView.saveBitmap(new OnSaveBitmap() {
                @Override
                public void onBitmapReady(final Bitmap saveBitmap) {
                    Log.e(TAG, "saveFilter: " + saveBitmap);
                    mImgSource.setImageBitmap(saveBitmap);
                    mImageFilterView.setVisibility(GONE);
                    onSaveBitmap.onBitmapReady(saveBitmap);
                }

                @Override
                public void onFailure(Exception e) {
                    onSaveBitmap.onFailure(e);
                }
            });
        } else {
            onSaveBitmap.onBitmapReady(mImgSource.getBitmap());
        }


    }

    void setFilterEffect(PhotoFilter filterType) {
        mImageFilterView.setVisibility(VISIBLE);
        mImageFilterView.setSourceBitmap(mImgSource.getBitmap());
        mImageFilterView.setFilterEffect(filterType);
    }

    void setFilterEffect(CustomEffect customEffect) {
        mImageFilterView.setVisibility(VISIBLE);
        mImageFilterView.setSourceBitmap(mImgSource.getBitmap());
        mImageFilterView.setFilterEffect(customEffect);
    }
}
