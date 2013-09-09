package net.rymate.notes.ui;

import android.os.Build;

/**
 * Created by Ryan on 08/09/13.
 */
public class UIUtils {
    // check for 3.0+
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
