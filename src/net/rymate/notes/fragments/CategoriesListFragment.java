package net.rymate.notes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.rymate.notes.R;

/**
 * Created by Ryan on 06/09/13.
 */
public class CategoriesListFragment extends ListFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.categories_fragment, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SampleAdapter adapter = new SampleAdapter(getActivity().getApplicationContext());
        for (int i = 0; i < 20; i++) {
            adapter.add(new SampleItem("Dummy Category " + i, android.R.drawable.ic_menu_search));
        }
        setListAdapter(adapter);
    }

    public class SampleItem {
        public String tag;
        public int iconRes;
        public SampleItem(String tag, int iconRes) {
            this.tag = tag;
            this.iconRes = iconRes;
        }
    }

    public class SampleAdapter extends ArrayAdapter<SampleItem> {

        public SampleAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.notes_row, null);
            }

            TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            title.setText(getItem(position).tag);

            return convertView;
        }

    }
}