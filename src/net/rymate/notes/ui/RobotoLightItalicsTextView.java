package net.rymate.notes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoLightItalicsTextView extends TextView {
    public RobotoLightItalicsTextView(Context context) {
        super(context);
        UIUtils.setRobotoLightItalics(getContext(), this);
    }

    public RobotoLightItalicsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        UIUtils.setRobotoLightItalics(getContext(), this);
    }

    public RobotoLightItalicsTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UIUtils.setRobotoLightItalics(getContext(), this);
    }
}