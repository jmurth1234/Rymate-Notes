package net.rymate.notes.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import net.rymate.notes.R;
import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.fragments.DeleteNoteDialogFragment;
import net.rymate.notes.fragments.NoteEditFragment;
import net.rymate.notes.fragments.NoteViewFragment;
import net.rymate.notes.fragments.NotesListFragment;

/**
 * Created by Ryan on 05/07/13.
 */
public class NotesListActivity extends ActionBarActivity
        implements NotesListFragment.Callbacks, DeleteNoteDialogFragment.DeleteNoteDialogListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Long mRowId;
    private boolean selected;
    private boolean editing;
    private Menu menu;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        if (findViewById(R.id.note_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            FragmentManager fm = getSupportFragmentManager();
            ((NotesListFragment) fm.findFragmentById(R.id.note_list))
                    .setActivateOnItemClick(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // the layout
        mDrawerList = (ListView) findViewById(R.id.left_drawer); // list of categories

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("Rymate Notes");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Categories");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
            Bundle arguments = new Bundle();
            arguments.putLong(NotesDbAdapter.KEY_ROWID, RowID);
            NoteViewFragment fragment = new NoteViewFragment();
            fragment.setArguments(arguments);
            mRowId = RowID;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_container, fragment)
                    .commit();

            selected = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // check for 3.0+
                invalidateOptionsMenu();
            } else {
                updateMenu();
            }

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, NoteViewActivity.class);
            detailIntent.putExtra(NotesDbAdapter.KEY_ROWID, RowID);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (selected) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.noteview_menu_tablet, menu);
            selected = false;
        } else if (editing) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_activity, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.new_note:
                Intent detailIntent = new Intent(this, NoteEditActivity.class);
                startActivity(detailIntent);
                return true;
            case R.id.edit_note:
                if (mTwoPane) {
                    // In two-pane mode, show the detail view in this activity by
                    // adding or replacing the detail fragment using a
                    // fragment transaction.
                    Bundle arguments = new Bundle();
                    arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                    NoteEditFragment fragment = new NoteEditFragment(false);
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.note_container, fragment)
                            .commit();

                    editing = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        // check for 3.0+
                        invalidateOptionsMenu();
                    } else {
                        updateMenu();
                    }


                } else {
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
                    Intent intent = new Intent(this, NoteEditActivity.class);
                    intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
                    startActivity(intent);
                }
                return true;
            case R.id.save_note:
                if (editing = true) {
                    // In two-pane mode, show the detail view in this activity by
                    // adding or replacing the detail fragment using a
                    // fragment transaction.
                    Bundle arguments = new Bundle();
                    arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                    NoteViewFragment fragment = new NoteViewFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.note_container, fragment)
                            .commit();
                    editing = false;
                }
                selected = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    // check for 3.0+
                    invalidateOptionsMenu();
                } else {
                    updateMenu();
                }

                return true;
            case R.id.delete_note:
                ((NotesListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.note_list))
                        .showDeleteDialog(mRowId);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Long getID() {
        return mRowId;
    }

    private void updateMenu() {
        if (selected) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.noteview_menu_tablet, menu);
            selected = false;
        } else if (editing) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_activity, menu);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        ((NotesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.note_list))
                .onDialogPositiveClick(dialog);
        ((NoteViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.note_container))
                .setText("");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        ((NotesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.note_list))
                .onDialogNegativeClick(dialog);

    }


}
