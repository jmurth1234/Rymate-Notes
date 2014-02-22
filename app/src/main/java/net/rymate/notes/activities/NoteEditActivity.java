package net.rymate.notes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.rymate.notes.R;
import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.fragments.NoteEditFragment;

/**
 * Created by Ryan on 07/08/13.
 */
public class NoteEditActivity extends FragmentActivity {
    Long mRowId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPref.getString("theme_list", "").equals("Dark")) {
            setTheme(R.style.AppDarkTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the note view fragment and add it to the activity
            // using a fragment transaction.
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }
        }
        NoteEditFragment fragment;

        if (mRowId != null) {
            Bundle arguments = new Bundle();
            arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
            fragment = new NoteEditFragment(false);
            fragment.setArguments(arguments);
        } else {
            fragment = new NoteEditFragment(true);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.note_container, fragment)
                .commit();

        NotesDbAdapter mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        getActionBar().setTitle(R.string.edit_note);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, NotesListActivity.class));
                return true;
            case R.id.save_note:
                NavUtils.navigateUpTo(this, new Intent(this, NotesListActivity.class));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}