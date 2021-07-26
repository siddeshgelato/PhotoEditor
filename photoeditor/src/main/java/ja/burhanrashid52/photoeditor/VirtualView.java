package ja.burhanrashid52.photoeditor;

import android.graphics.Bitmap;

public class VirtualView {
    public boolean isText;
    public boolean isDeleted;
    public boolean isRemoved; // User deletion
    public String uuid;
    public float x;
    public float y;
    public double height;
    public double width;
    public float rotation;
    public int color;

    public String fontFamily;
    public float fontSize;
    public String text;
    public int textAlign;

    public Bitmap bitmap;
}
