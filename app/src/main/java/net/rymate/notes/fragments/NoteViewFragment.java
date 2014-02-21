package net.rymate.notes.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.manuelpeinado.glassactionbar.GlassActionBarHelper;

import net.rymate.notes.R;
import net.rymate.notes.activities.NoteViewActivity;
import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.database.NotesDbAdapter;

/**
 * Created by Ryan on 05/07/13.
 */
public class NoteViewFragment extends Fragment {

    public static Long mRowId;
    NoteViewListener mListener;
    boolean nope = false;
    private EditText mBodyText;
    private NotesDbAdapter mDbHelper;
    private boolean editing;
    private InputMethodManager imeManager;
    private String noteText;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.edit_activity, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.save_note:
                    setEditing(false);
                    mBodyText.setFocusable(false);
                    mBodyText.setFocusableInTouchMode(false);
                    imeManager.hideSoftInputFromWindow(mBodyText.getWindowToken(), 0);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getActivity().getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }

            if (mRowId == null) {
                NotesListActivity list = (NotesListActivity) getActivity();
                mRowId = list.getID();
            }
        }

        mDbHelper = new NotesDbAdapter(this.getActivity());
        mDbHelper.open();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoteViewListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoteViewListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView;

        if (getActivity().getClass() == NoteViewActivity.class) {
            com.manuelpeinado.glassactionbar.GlassActionBarHelper helper = new GlassActionBarHelper().contentLayout(R.layout.fragment_note_view);
            rootView = helper.createView(this.getActivity());
        } else {
            rootView = inflater.inflate(R.layout.fragment_note_view, container, false);

        }

        mBodyText = (EditText) rootView.findViewById(R.id.noteView);
        final NoteViewFragment nvf = this;
        mBodyText.setCustomSelectionActionModeCallback(mActionModeCallback);
        mBodyText.setFocusable(false);
        mBodyText.setFocusableInTouchMode(false);

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);
            noteText = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            mBodyText.setText(noteText);
            if (getActivity().getClass() == NoteViewActivity.class) {
                NoteViewActivity activity = (NoteViewActivity) getActivity();
                activity.getActionBar().setTitle(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }


        // Create an anonymous implementation of OnClickListener
        EditText.OnLongClickListener longClickListener = new EditText.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (editing) {
                    return nope;
                }

                mBodyText.showContextMenu();

                return true;
            }
        };

        EditText.OnClickListener clickListener = new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBodyText.setFocusable(true);
                mBodyText.setFocusableInTouchMode(true);
                mBodyText.requestFocus();

                imeManager = (InputMethodManager) getActivity().getApplicationContext().getSystemService("input_method");
                imeManager.showSoftInput(mBodyText, 0);
            }
        };

        mBodyText.setOnLongClickListener(longClickListener);
        mBodyText.setOnClickListener(clickListener);
        return rootView;
    }

    public void setText(String text) {
        mBodyText.setText(text);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public interface NoteViewListener {
        public void onStartedEditing(NoteViewFragment n);

        public void onFinishedEditing(NoteViewFragment n);
    }

}
