package org.secuso.privacyfriendlyactivitytracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.secuso.privacyfriendlyactivitytracker.BuildConfig;
import org.secuso.privacyfriendlyactivitytracker.R;

/**
 * Displays the about-page.
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.action_about);
        }

        // Make links clickable
        TextView libTextView = (TextView) rootView.findViewById(R.id.textViewLib);
        libTextView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView secusoTextView = (TextView) rootView.findViewById(R.id.textFieldSecuso);
        secusoTextView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView repoTextView = (TextView) rootView.findViewById(R.id.textFieldRepo);
        repoTextView.setMovementMethod(LinkMovementMethod.getInstance());

        container.removeAllViews();
        return rootView;
    }
}