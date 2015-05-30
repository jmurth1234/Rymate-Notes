package net.rymate.notes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.rymate.notes.R;
import net.rymate.notes.data.NotesDbAdapter;
import net.rymate.notes.fragments.NoteEditFragment;

/**
 * Created by Ryan on 07/08/13.
 */
public class NoteEditActivity extends AppCompatActivity {
    Long mRowId;
    NoteEditFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        // Show the Up button in the action bar.
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                Bundle arguments = new Bundle();
                arguments.putString(NotesDbAdapter.KEY_BODY, intent.getStringExtra(Intent.EXTRA_TEXT));
                fragment = new NoteEditFragment(true);
                fragment.setArguments(arguments);
            }
        } else if (("com.google.android.gm.action.AUTO_SEND").equals(action) && intent.getType() != null) {
            Bundle arguments = new Bundle();
            arguments.putString(NotesDbAdapter.KEY_BODY, intent.getStringExtra(Intent.EXTRA_TEXT));
            fragment = new NoteEditFragment(true);
            fragment.setArguments(arguments);
        } else if (savedInstanceState == null) {
            // Create the note view fragment and add it to the activity
            // using a fragment transaction.
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = intent.getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }
        }

        if (fragment == null) {
            if (mRowId != null) {
                Bundle arguments = new Bundle();
                arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                fragment = new NoteEditFragment(false);
                fragment.setArguments(arguments);
            } else {
                fragment = new NoteEditFragment(true);
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.note_container, fragment)
                .commit();

        NotesDbAdapter mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        //getActionBar().setTitle(R.string.edit_note);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_activity, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.swap_in_bottom_back, R.anim.swap_out_bottom_back);
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
                fragment.saveState(false);
                //NavUtils.navigateUpTo(this, new Intent(this, NotesListActivity.class));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}