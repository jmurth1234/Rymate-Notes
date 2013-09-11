package net.rymate.notes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.espiandev.showcaseview.ShowcaseView;

import net.rymate.notes.R;
import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.fragments.CategoriesListFragment;
import net.rymate.notes.fragments.DeleteNoteDialogFragment;
import net.rymate.notes.fragments.NoteEditFragment;
import net.rymate.notes.fragments.NoteViewFragment;
import net.rymate.notes.fragments.NotesListFragment;
import net.rymate.notes.ui.DrawerToggle;
import net.rymate.notes.ui.ShowcaseViewGB;

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
    private DrawerLayout mDrawerLayout;
    private DrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ShowcaseViewGB sv;
    private SharedPreferences pref;


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

        mDrawerToggle = new DrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle("Rymate Notes");
                supportInvalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Categories");
                supportInvalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        pref = getSharedPreferences("rymatenotesprefs", MODE_PRIVATE);

        SampleAdapter adapter = new SampleAdapter(getApplicationContext());
        adapter.add(new SampleItem("Notes", true));
        adapter.add(new SampleItem("All Notes", false));
        adapter.add(new SampleItem("Uncategorised", false));
        adapter.add(new SampleItem("Categories", true));

        for (int i = 1; i < 21; i++) {
            adapter.add(new SampleItem("Dummy Category " + i, false));
        }
        mDrawerList.setAdapter(adapter);

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
            supportInvalidateOptionsMenu();

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
        if(pref.getBoolean("firststart", true)) {

            // update sharedpreference - another start wont be the first
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("firststart", false);
            editor.commit(); // apply changes

            // fancy 3.0+ welcome view
            ShowcaseViewGB.ConfigOptions co = new ShowcaseViewGB.ConfigOptions();
            co.hideOnClickOutside = true;

            sv = ShowcaseViewGB.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, R.id.new_note, this,
                    R.string.showcase_note_title, R.string.showcase_note_message, co);
            sv.show();
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_activity_navdrawer, menu);
            return true;
        }

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

                    supportInvalidateOptionsMenu();
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
                supportInvalidateOptionsMenu();
                return true;
            case R.id.delete_note:
                ((NotesListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.note_list))
                        .showDeleteDialog(mRowId);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public Long getID() {
        return mRowId;
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


    public class SampleItem {
        public String tag;
        public boolean title;
        public SampleItem(String tag, boolean title) {
            this.tag = tag;
            this.title = title;
        }
    }

    public class SampleAdapter extends ArrayAdapter<SampleItem> {

        public SampleAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (this.getItem(position).title) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_row_title, null);
                } else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_row, null);
                }
            }

            TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            title.setText(getItem(position).tag);

            return convertView;
        }

    }
}
