package net.rymate.notes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoLightTextView extends TextView {
    public RobotoLightTextView(Context context) {
        super(context);
        UIUtils.setRobotoLight(getContext(), this);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        UIUtils.setRobotoLight(getContext(), this);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UIUtils.setRobotoLight(getContext(), this);
    }
}