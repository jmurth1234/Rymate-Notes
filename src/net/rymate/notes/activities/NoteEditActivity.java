package net.rymate.notes.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.rymate.notes.R;

/**
 * Created by Ryan on 07/08/13.
 */
public class NoteEditActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}