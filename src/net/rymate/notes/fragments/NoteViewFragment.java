package net.rymate.notes.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import net.rymate.notes.R;
import net.rymate.notes.activities.NoteViewActivity;
import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.database.NotesDbAdapter;

/**
 * Created by Ryan on 05/07/13.
 */
public class NoteViewFragment extends Fragment {

    private EditText mBodyText;
    public static Long mRowId;
    private NotesDbAdapter mDbHelper;
    private boolean editing;
    private InputMethodManager imeManager;
    private String noteText;

    public interface NoteViewListener {
        public void onStartedEditing(NoteViewFragment n);
        public void onFinishedEditing(NoteViewFragment n);
    }

    NoteViewListener mListener;

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
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_note_view, container, false);
        mBodyText = (EditText) rootView.findViewById(R.id.noteView);
        final NoteViewFragment nvf = this;
        mBodyText.setFocusable(false);
        mBodyText.setFocusableInTouchMode(false);

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);
            noteText = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            mBodyText.setText(noteText);
            if (getActivity().getClass() == NoteViewActivity.class) {
                NoteViewActivity activity = (NoteViewActivity) getActivity();
                activity.getSupportActionBar().setTitle(note.getString(
                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            }
        }

        // Create an anonymous implementation of OnClickListener
        EditText.OnLongClickListener longClickListener = new EditText.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (editing) {
                    return false;
                }

                mBodyText.setFocusable(true);
                mBodyText.setFocusableInTouchMode(true);
                mBodyText.requestFocus();

                imeManager = (InputMethodManager) getActivity().getApplicationContext().getSystemService("input_method");
                imeManager.showSoftInput(mBodyText, 0);

                // Start the CAB using the ActionMode.Callback defined above
                ActionBarActivity leActivity = (ActionBarActivity) getActivity();
                mListener.onStartedEditing(nvf);

                return true;
            }
        };

        mBodyText.setOnLongClickListener(longClickListener);
        return rootView;
    }

    public void setText(String text) {
        mBodyText.setText(text);
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public boolean isEditing() {
        return editing;
    }

}
