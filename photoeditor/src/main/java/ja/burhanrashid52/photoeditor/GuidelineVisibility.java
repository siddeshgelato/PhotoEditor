package ja.burhanrashid52.photoeditor;

public enum GuidelineVisibility {
    TOP("top"),
    TOP_GONE("top"),
    BOTTOM("bottom"),
    BOTTOM_GONE("bottom"),
    LEFT("left"),
    LEFT_GONE("left"),
    RIGHT("right"),
    RIGHT_GONE("right"),
    HORIZONTAL("horizontal"),
    HORIZONTAL_GONE("horizontal"),
    VERTICAL("vertical"),
    VERTICAL_GONE("vertical"),
    ALL("all"),
    NONE("none");

    public final String value;

    GuidelineVisibility(String value) {
        this.value = value;
    }
}
