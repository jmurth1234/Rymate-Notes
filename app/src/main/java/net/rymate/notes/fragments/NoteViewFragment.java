package net.rymate.notes.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;


import net.rymate.notes.R;
import net.rymate.notes.activities.NoteViewActivity;
import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.database.NotesDbAdapter;

/**
 * Created by Ryan on 05/07/13.
 */
public class NoteViewFragment extends Fragment {

    public static Long mRowId;
    boolean nope = false;
    private EditText mBodyText;
    private NotesDbAdapter mDbHelper;
    private boolean editing;
    private InputMethodManager imeManager;
    private String noteText;
    private int categoryId;
    private String noteTitle;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView;

            rootView = inflater.inflate(R.layout.fragment_note_view, container, false);


        mBodyText = (EditText) rootView.findViewById(R.id.noteView);
        final NoteViewFragment nvf = this;
        mBodyText.setFocusable(false);
        mBodyText.setFocusableInTouchMode(false);

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);
            noteTitle = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
            noteText = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            categoryId = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATID));
            Spanned formattedBody = Html.fromHtml(noteText);
            mBodyText.setText(formattedBody);
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

                getActivity().invalidateOptionsMenu();

                imeManager = (InputMethodManager) getActivity().getApplicationContext().getSystemService("input_method");
                imeManager.showSoftInput(mBodyText, 0);

                setEditing(true);
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

    public void saveNote() {
        setEditing(false);
        getActivity().invalidateOptionsMenu();
        mBodyText.setFocusable(false);
        mBodyText.setFocusableInTouchMode(false);
        imeManager.hideSoftInputFromWindow(mBodyText.getWindowToken(), 0);
        mDbHelper.updateNote(mRowId, noteTitle, Html.toHtml(mBodyText.getText()), categoryId);
    }

    private WebView mWebView;

    public void printNote() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(getActivity());
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Rymate Notes", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><h1>" + noteTitle + "</h1><p>" + noteText +
                "</p></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";
        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }
}
