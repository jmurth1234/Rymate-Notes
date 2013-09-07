package net.rymate.notes.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.R;

/**
 * Created by Ryan on 04/07/13.
 */
public class NotesListFragment extends ListFragment
        implements DeleteNoteDialogFragment.DeleteNoteDialogListener {

    /**
     * The serialization key representing the selected note.
     * Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * How else do we access notes? :)
     */
    private NotesDbAdapter mDbHelper;

    private long noteId;

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

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Long id) {
        }
    };

    public NotesListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this.getActivity());
        mDbHelper.open();
        fillData();

    }

    public void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        this.getActivity().startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE, NotesDbAdapter.getSample()};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this.getActivity(), R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(id);
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
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
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
        this.fillData();
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, R.string.note_deleted, duration);
        toast.show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
}