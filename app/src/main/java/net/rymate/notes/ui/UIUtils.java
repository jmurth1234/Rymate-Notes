package net.rymate.notes.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.widget.TextView;

/**
 * Created by Ryan on 08/09/13.
 */
public class UIUtils {
    private static Typeface mRobotoLightTypeface = null;
    private static Typeface mRobotoLightTypefaceItalics;

    // check for 4.0+
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
}
