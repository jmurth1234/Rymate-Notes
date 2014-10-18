package net.rymate.notes.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import net.rymate.notes.R;

/**
 * Created by Ryan on 22/07/2014.
 */
public class BaseNoteActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


}
