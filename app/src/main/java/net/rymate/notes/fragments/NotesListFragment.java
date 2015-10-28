package net.rymate.notes.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.rymate.notes.R;
import net.rymate.notes.activities.NoteEditActivity;
import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.data.NotesDbAdapter;
import net.rymate.notes.data.NotesRecyclerAdapter;
import net.rymate.notes.data.SimpleCursorAdapter;
import net.rymate.notes.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 04/07/13.
 */
public class NotesListFragment extends Fragment
        implements DeleteNoteDialogFragment.DeleteNoteDialogListener, ListView.OnItemClickListener, NotesRecyclerAdapter.OnItemClickListener {

    /**
     * The serialization key representing the selected note.
     * Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Long id) {
        }
    };
    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    private FloatingActionButton fab = null;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    /**
     * How else do we access notes? :)
     */
    private NotesDbAdapter mDbHelper;
    private AbsListView mNoteslist;

    private long noteId;
    private TextView mProgressText;
    private RelativeLayout mProgressContainer;
    private int category;
    private RecyclerView mNotesRecycler;
    private int mScreenHeight;
    private boolean mHidden;
    private float currentY;
    private boolean portraitTablet;


    public NotesListFragment(FloatingActionButton fab) {
        this.fab = fab;
    }

    public NotesListFragment() {
    }

    public AbsListView getListView() {
        return mNoteslist;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this.getActivity());
        mDbHelper.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //mNoteslist = (AbsListView) rootView.findViewById(R.id.listView);
        //mNoteslist.setOnItemClickListener(this);

        mNotesRecycler = (RecyclerView) rootView.findViewById(R.id.listView);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
        fab.setRippleColor(Color.parseColor("#1e90ff"));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((NotesListActivity) getActivity()).mTwoPane) {
                    Intent detailIntent = new Intent(view.getContext(), NoteEditActivity.class);
                    startActivity(detailIntent);
                    getActivity().overridePendingTransition(R.anim.swap_in_bottom, R.anim.swap_out_bottom);
                } else {
                    NoteEditDialogFragment newFragment = NoteEditDialogFragment.newInstance();
                    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            }
        });

        if (rootView.findViewById(R.id.portraitTablet) != null) {
            portraitTablet = true;
        }

        fillData();

        return rootView;
    }


    public void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();

        NotesRecyclerAdapter notes = new NotesRecyclerAdapter(notesCursor, getActivity());
        notes.SetOnItemClickListener(this);
        mNotesRecycler.setAdapter(notes);
        mNotesRecycler.setHasFixedSize(true);
        setNotesLayoutManager();

    }

    private void setNotesLayoutManager() {
        if (portraitTablet) {
            // on a portrait tablet device
            mNotesRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            mNotesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        }
    }


    public void fillData(int catId) {
        category = catId;
        Cursor notesCursor;
        if (catId == 0) {
            //  get ALL THE NOTES
            notesCursor = mDbHelper.fetchAllNotes();
        } else if (catId == 1) {
            // get ALL the notes that are not in a category
            Cursor notesCursor1 = mDbHelper.fetchNotes(0);
            Cursor notesCursor2 = mDbHelper.fetchNotes(1);
            Cursor[] notesCursor12 = {notesCursor1, notesCursor2};

            notesCursor = new MergeCursor(notesCursor12);
        } else {
            // get ALL the notes in a category
            notesCursor = mDbHelper.fetchNotes(catId);
        }

        NotesRecyclerAdapter notes = new NotesRecyclerAdapter(notesCursor, getActivity());
        notes.SetOnItemClickListener(this);
        mNotesRecycler.setAdapter(notes);
        mNotesRecycler.setHasFixedSize(true);
        setNotesLayoutManager();
    }

    public void search(String s) {
        if (mDbHelper.fetchAllNotes().getCount() == 0) {
            return;
        }

        if (!s.isEmpty()) {
            Cursor notesCursor = mDbHelper.searchNotes(s);

            NotesRecyclerAdapter notes = new NotesRecyclerAdapter(notesCursor, getActivity());
            notes.SetOnItemClickListener(this);
            mNotesRecycler.setAdapter(notes);
            mNotesRecycler.setHasFixedSize(true);
            setNotesLayoutManager();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        AbsListView v = getListView();
        if (v == null) {
            return;
        }
        if (activateOnItemClick) {
            v.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } else {
            v.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        Log.i("test", "clicked an item");
        mCallbacks.onItemSelected(l);
    }

    public void showDeleteDialog(long noteId) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment dialog = new DeleteNoteDialogFragment();
        dialog.show(getFragmentManager(), "dialog");
        this.noteId = noteId;
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        mDbHelper.deleteNote(noteId);
        this.fillData(category);
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, R.string.note_deleted, duration);
        toast.show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("test", "clicked an item");
        mCallbacks.onItemSelected((long) position);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Long id);
    }
}