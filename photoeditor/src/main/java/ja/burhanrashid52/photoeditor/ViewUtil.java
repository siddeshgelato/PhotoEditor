package ja.burhanrashid52.photoeditor;

import android.view.View;

public class ViewUtil {

    public static double scale = 1.0;

    public static void setScale(double s) {
        scale = s;
    }

    public static int getTransformedX(View view)  {
        View parent  =(View) view.getParent();
        int[] coordinatesParent = new int[2];
        parent.getLocationOnScreen(coordinatesParent);

        int[] coordinatesChild = new int[2];
        view.getLocationOnScreen(coordinatesChild);

        return (int) ((coordinatesChild[0] - coordinatesParent[0]) / scale);
    }

    public static int getTransformedY(View view)  {
        View parent  =(View) view.getParent();
        int[] coordinatesParent = new int[2];
        parent.getLocationOnScreen(coordinatesParent);

        int[] coordinatesChild = new int[2];
        view.getLocationOnScreen(coordinatesChild);

        return (int) ((coordinatesChild[1] - coordinatesParent[1]) / scale);
    }

    public static void setTransformedX(View view, float givenX) {
        int transformedX = getTransformedX(view);
        view.setX((givenX - (transformedX - view.getX())));
    }

    public static void setTransformedY(View view, float givenY) {
        int transformedY = getTransformedY(view);
        view.setY((givenY - (transformedY - view.getY())));
    }

    public static float getTransformedHeight(View view){
        return view.getHeight() * view.getScaleY();
    }

    public static float getTransformedWidth(View view){
        return view.getWidth() * view.getScaleX();
    }
}
