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

    // check for 3.0+
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static Typeface getRobotoLight(final Context context) {
        if (mRobotoLightTypeface == null) {
            mRobotoLightTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }
        return mRobotoLightTypeface;
    }

    public static void setRobotoLight(final Context context, final TextView textView) {
        final Typeface font = getRobotoLight(context);
        textView.setTypeface(font);
    }

    public static Typeface getRobotoLightItalics(final Context context) {
        if (mRobotoLightTypefaceItalics == null) {
            mRobotoLightTypefaceItalics = Typeface.createFromAsset(context.getAssets(), "Roboto-LightItalic.ttf");
        }
        return mRobotoLightTypefaceItalics;
    }

    public static void setRobotoLightItalics(final Context context, final TextView textView) {
        final Typeface font = getRobotoLightItalics(context);
        textView.setTypeface(font);
    }
}
