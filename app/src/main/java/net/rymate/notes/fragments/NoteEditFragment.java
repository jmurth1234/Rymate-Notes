package net.rymate.notes.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import net.rymate.notes.R;

import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.database.NotesDbAdapter;

/**
 * Created by Ryan on 07/08/13.
 */
public class NoteEditFragment extends Fragment {

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    private boolean newNote;
    private Spinner mCategorySpinner;
    private Long mCatId;

    public NoteEditFragment(boolean b) {
        this.newNote = b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this.getActivity());
        mDbHelper.open();

        if (savedInstanceState == null) {
            if (!newNote) {
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_note_edit, container, false);

        mTitleText = (EditText) rootView.findViewById(R.id.title);
        mBodyText = (EditText) rootView.findViewById(R.id.note_body);
        mCategorySpinner = (Spinner) rootView.findViewById(R.id.spinner);

        populateFields();

        return rootView;
    }

    private void populateFields() {
        Cursor note;

        Cursor catCursor = mDbHelper.fetchNearlyAllCategories();
        getActivity().startManagingCursor(catCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{android.R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter cat =
                new SimpleCursorAdapter(this.getActivity(), R.layout.category_row, catCursor, from, to);

        mCategorySpinner.setAdapter(cat);

        if (mRowId != null) {
            note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);

            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));

            int category = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATID));

            if ((category == 0) || (category == 1)) {
                mCategorySpinner.setSelection(0);
            } else {
                mCategorySpinner.setSelection(category - 1);
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateFields();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    public void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        int category = mCategorySpinner.getSelectedItemPosition() + 1;
        boolean saved;

        if (body.length() != 0) {
            if (mRowId == null) {
                long id = mDbHelper.createNote(title, body, category);
                if (id > 0) {
                    mRowId = id;
                    saved = true;
                } else {
                    saved = false;
                }
            } else {
                saved = mDbHelper.updateNote(mRowId, title, body, category);
            }

            Context context = getActivity().getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            int durationFailed = Toast.LENGTH_LONG;

            if (saved) {
                Toast toast = Toast.makeText(context, R.string.note_saved, duration);
                toast.show();
            } else {
                Toast toast = Toast.makeText(context, R.string.note_failed, durationFailed);
                toast.show();

            }
        }
    }
}
