package net.rymate.notes.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import net.rymate.notes.R;
import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.fragments.NoteViewFragment;
import net.rymate.notes.fragments.NotesListFragment;

/**
 * Created by Ryan on 05/07/13.
 */
public class NotesListActivity extends FragmentActivity
        implements NotesListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notes_list);

        if (findViewById(R.id.note_view_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            debugToast("It's working!");

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NotesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.note_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link NotesListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Long RowID) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            debugToast("Tablet UI ON");
            Bundle arguments = new Bundle();
            arguments.putLong(NotesDbAdapter.KEY_ROWID, RowID);
            NoteViewFragment fragment = new NoteViewFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_view_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            debugToast("Tablet UI OFF");
            Intent detailIntent = new Intent(this, NoteViewActivity.class);
            detailIntent.putExtra(NotesDbAdapter.KEY_ROWID, RowID);
            startActivity(detailIntent);
        }
    }

    public void debugToast(String s) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, s, duration);
        toast.show();

    }
}
