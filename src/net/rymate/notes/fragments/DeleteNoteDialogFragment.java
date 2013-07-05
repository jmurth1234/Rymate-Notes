package net.rymate.notes.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import net.rymate.notes.database.NotesDbAdapter;
import net.rymate.notes.R;

/**
 * Created by Ryan on 04/07/13.
 */
public class DeleteNoteDialogFragment extends DialogFragment {
    private int noteId;
    private NotesDbAdapter mDbHelper;

    public DeleteNoteDialogFragment(int noteId, NotesDbAdapter mDbHelper) {
        this.noteId = noteId;
        this.mDbHelper = mDbHelper;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_delete)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Well, lets delete the note
                        mDbHelper.deleteNote(noteId);

                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    int mNum;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static DeleteNoteDialogFragment newInstance(int num, int noteId, NotesDbAdapter mDbHelper) {
        DeleteNoteDialogFragment f = new DeleteNoteDialogFragment(noteId, mDbHelper);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }
}