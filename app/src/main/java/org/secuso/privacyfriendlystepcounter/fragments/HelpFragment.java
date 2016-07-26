package org.secuso.privacyfriendlystepcounter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlystepcounter.R;

/**
 * Displays the help content.
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class HelpFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.action_help);
        }
        container.removeAllViews();
        return rootView;
    }
}
