package net.rymate.notes.ui;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import net.rymate.notes.R;

public class StyleCallback implements ActionMode.Callback {

    private final EditText mBodyText;

    public StyleCallback(EditText mBodyText) {
        this.mBodyText = mBodyText;
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.actionmode_style, menu);
        menu.removeItem(android.R.id.selectAll);
        return true;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        CharacterStyle cs;
        int start = mBodyText.getSelectionStart();
        int end = mBodyText.getSelectionEnd();
        SpannableStringBuilder ssb = new SpannableStringBuilder(mBodyText.getText());

        switch (item.getItemId()) {

            case R.id.bold:
                cs = new StyleSpan(Typeface.BOLD);
                ssb.setSpan(cs, start, end, 1);
                mBodyText.setText(ssb);
                return true;

            case R.id.italic:
                cs = new StyleSpan(Typeface.ITALIC);
                ssb.setSpan(cs, start, end, 1);
                mBodyText.setText(ssb);
                return true;

            case R.id.underline:
                cs = new UnderlineSpan();
                ssb.setSpan(cs, start, end, 1);
                mBodyText.setText(ssb);
                return true;
        }
        return false;
    }

    public void onDestroyActionMode(ActionMode mode) {
    }
}