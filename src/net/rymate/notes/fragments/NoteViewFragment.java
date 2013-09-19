package net.rymate.notes.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_note_view, container, false);

        mBodyText = (EditText) rootView.findViewById(R.id.noteView);

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            if (getActivity().getClass() == NoteViewActivity.class) {
                NoteViewActivity activity = (NoteViewActivity) getActivity();
                activity.getSupportActionBar().setTitle(note.getString(
                        note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            }
        }

        // Create an anonymous implementation of OnClickListener
        /*EditText.OnLongClickListener longClickListener = new EditText.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mBodyText.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                return true;
            }
        };
*/
        //mBodyText.setOnLongClickListener(longClickListener);
        return rootView;
    }

    public void setText(String text) {
        mBodyText.setText(text);
    }

}
