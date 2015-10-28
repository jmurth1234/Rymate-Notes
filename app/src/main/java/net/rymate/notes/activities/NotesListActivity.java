package net.rymate.notes.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.MetadataChangeSet;

import net.rymate.notes.R;
import net.rymate.notes.data.NotesDbAdapter;
import net.rymate.notes.fragments.DeleteNoteDialogFragment;
import net.rymate.notes.fragments.IntroFragment;
import net.rymate.notes.fragments.NoteEditDialogFragment;
import net.rymate.notes.fragments.NoteEditFragment;
import net.rymate.notes.fragments.NoteViewFragment;
import net.rymate.notes.fragments.NotesListFragment;
import net.rymate.notes.ui.UIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by Ryan on 05/07/13.
 */
public class NotesListActivity extends AppCompatActivity
        implements NotesListFragment.Callbacks,
        DeleteNoteDialogFragment.DeleteNoteDialogListener,
        IntroFragment.OnNewNoteClickedInIntroFragmentListener {



    public static Typeface ROBOTO_LIGHT;
    public static Typeface ROBOTO_LIGHT_ITALICS;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public boolean mTwoPane;
    private Long mRowId;
    private boolean selected;
    private boolean editing;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private SharedPreferences pref;
    private NotesDbAdapter mDbHelper;
    private NotesListFragment list;
    private LinearLayout mDrawerLinear;
    private NoteViewFragment fragment;
    private NoteEditFragment editFragment;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ROBOTO_LIGHT = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
        ROBOTO_LIGHT_ITALICS = Typeface.createFromAsset(this.getAssets(), "Roboto-LightItalic.ttf");

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
        }

        list = new NotesListFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.note_list_container, list)
                .commit();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // the layout
        mDrawerLinear = (LinearLayout) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,  /* toolbar */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e90ff")));

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        pref = getSharedPreferences("rymatenotesprefs", MODE_PRIVATE);

        mDrawerList = (ListView) findViewById(R.id.cat_list);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        if (mDbHelper.fetchAllNotes().getCount() == 0) {
            IntroFragment fragment = new IntroFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_list_container, fragment)
                    .commit();

        }

        getCategories();

    }


    /**
     * Callback method from {@link NotesListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Long RowID) {
        System.out.println("It's happening!");
        if (mTwoPane) {
            System.out.println("ID = " + RowID);
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            list.setActivateOnItemClick(true);
            //list.setActivatedPosition(RowID.intValue());
            Bundle arguments = new Bundle();
            arguments.putLong(NotesDbAdapter.KEY_ROWID, RowID);
            fragment = new NoteViewFragment();
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
            overridePendingTransition(R.anim.swap_in_bottom, R.anim.swap_out_bottom);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!mTwoPane)
            inflater.inflate(R.menu.main_activity, menu);
        else
            inflater.inflate(R.menu.main_activity_tablet, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                list.search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                list.search(query);
                return true;
            }

        });

        final Context context = this;

        Button catButton = (Button) findViewById(R.id.cat_button);
        catButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                final View addCategoryView = inflater.inflate(R.layout.dialog_addcategory, null);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText text = (EditText) addCategoryView.findViewById(R.id.category);
                        if (text.getText().toString().length() != 0) {
                            mDbHelper.addCategory(text.getText().toString());
                        }
                        getCategories();
                    }
                });

                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                });

                AlertDialog addCategoryDialog = dialogBuilder.create();
                addCategoryDialog.setView(addCategoryView, 0, 0, 0, 0);

                addCategoryDialog.show();
            }
        });


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_activity_navdrawer, menu);
            return true;
        } else {
            menu.clear();
            onCreateOptionsMenu(menu);
        }

        if (selected) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.noteview_menu_tablet, menu);
            selected = false;
        }
        if (fragment != null) {
            if (fragment.isEditing()) {
                menu.clear();
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.edit_activity, menu);
            } else {
                menu.clear();
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.noteview_menu_tablet, menu);
            }
        }

        if (editing) {
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
            case R.id.edit_note:
                DialogFragment dialog = NoteEditDialogFragment.newInstance(true);
                Bundle arguments = new Bundle();
                arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                dialog.setArguments(arguments);
                dialog.show(getSupportFragmentManager(), "dialog");
                return true;
            case R.id.save_note:
                if (editing) {
                    // In two-pane mode, show the detail view in this activity by
                    // adding or replacing the detail fragment using a
                    // fragment transaction.
                    arguments = new Bundle();
                    arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                    NoteViewFragment fragment = new NoteViewFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.note_container, fragment)
                            .commit();
                    editing = false;
                } else if (fragment.isEditing()) {
                    fragment.saveNote();
                }
                selected = true;
                supportInvalidateOptionsMenu();
                return true;
            case R.id.delete_note:
                list.showDeleteDialog(mRowId);
                return true;
            case R.id.export_notes:
                Intent detailIntent = new Intent(this, NotesBackupActivity.class);
                startActivity(detailIntent);
                return true;
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
    public void OnNewNoteClickedInIntroFragment() {
        if (!mTwoPane) {
            Intent detailIntent = new Intent(this, NoteEditActivity.class);
            startActivity(detailIntent);
        } else {
            NoteEditDialogFragment newFragment = NoteEditDialogFragment.newInstance();

            newFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        mDrawerLayout.closeDrawer(mDrawerLinear);
        if (mDbHelper.fetchAllNotes().getCount() == 0) {
            return;
        } else {
            list.fillData(position);
        }
    }

    public void getCategories() {
        Cursor catCursor = mDbHelper.fetchCategories();
        startManagingCursor(catCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{android.R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter cat =
                new SimpleCursorAdapter(this, R.layout.category_row, catCursor, from, to);

        if (mDrawerList != null) {
            mDrawerList.setAdapter(cat);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        list.onDialogPositiveClick(dialog);
        ((NoteViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.note_container))
                .setText("");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        list.onDialogNegativeClick(dialog);
    }

    public NotesListFragment getList() {
        return list;
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override

        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
