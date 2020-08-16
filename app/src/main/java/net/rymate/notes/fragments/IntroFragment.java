package net.rymate.notes.fragments;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.rymate.notes.R;

public class IntroFragment extends Fragment implements View.OnClickListener {
    private OnNewNoteClickedInIntroFragmentListener mListener;
    private TextView t;

    public IntroFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.intro_view, container, false);
        t = (TextView) rootView.findViewById(R.id.no_notes_2);
        t.setOnClickListener(this);
        return rootView;
    }

    public void onClick(View arg0) {
        if (mListener != null) {
            mListener.OnNewNoteClickedInIntroFragment();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnNewNoteClickedInIntroFragmentListener) activity;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnNewNoteClickedInIntroFragmentListener {
        // TODO: Update argument type and name
        public void OnNewNoteClickedInIntroFragment();
    }

}
