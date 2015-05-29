package net.rymate.notes.data;

import android.text.Spanned;

/**
 * Generic note object for storing notes to serialize
 *
 * Created by Ryan on 23/12/2014.
 */
public class Note {
    private int id;
    private int catId;
    private String title;
    private String text;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

}
