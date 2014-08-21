package net.rymate.notes.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import net.rymate.notes.R;

/**
 * Created by Ryan on 22/07/2014.
 */
public class BaseNoteActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setIcon(R.drawable.ic_launcher_outline);

        super.onCreate(savedInstanceState);
    }
}
