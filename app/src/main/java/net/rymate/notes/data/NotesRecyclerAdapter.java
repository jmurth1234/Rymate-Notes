package net.rymate.notes.data;

import android.app.Activity;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.rymate.notes.R;

/**
 * Created by Ryan on 20/11/2014.
 */
public class NotesRecyclerAdapter extends CursorRecyclerAdapter<NotesRecyclerAdapter.ViewHolder> {
    private final Activity activity;
    private OnItemClickListener mItemClickListener;

    public NotesRecyclerAdapter(Cursor cursor, Activity activity) {
        super(cursor);
        this.activity = activity;
    }

    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
        holder.text.setText(
                cursor.getString(cursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)).replaceAll("\\<[^>]*>", ""));

        holder.title.setText(
                cursor.getString(cursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));

        holder.id = cursor.getInt(cursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID));
    }

    @Override
    public NotesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        final View sView = mInflater.inflate(R.layout.notes_row, viewGroup, false);
        return new ViewHolder(sView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, text;
        int id;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_text);
            text = (TextView) itemView.findViewById(R.id.content_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, id);
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
