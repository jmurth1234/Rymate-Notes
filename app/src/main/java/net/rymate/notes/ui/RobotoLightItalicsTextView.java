package net.rymate.notes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import net.rymate.notes.activities.NotesListActivity;

public class RobotoLightItalicsTextView extends TextView {
    public RobotoLightItalicsTextView(Context context) {
        super(context);
        this.setTypeface(NotesListActivity.ROBOTO_LIGHT_ITALICS);
    }

    public RobotoLightItalicsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(NotesListActivity.ROBOTO_LIGHT_ITALICS);
    }

    public RobotoLightItalicsTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(NotesListActivity.ROBOTO_LIGHT_ITALICS);
    }
}