package ja.burhanrashid52.photoeditor;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;

public class VirtualView {
    public boolean isText;
    public boolean isDeleted;
    public boolean isRemoved; // User deletion
    public String uuid;
    public float x;
    public float y;
    public float scaleX;
    public float scaleY;
    public float z;
    public double height = -99;
    public double width = -99;
    public float rotation;
    public int color;

    public String fontFamily;
    public float fontSize;
    public String text;
    public int textAlign;

    public Bitmap bitmap;
    public FrameLayout.LayoutParams params;
}
