package org.secuso.privacyfriendlystepcounter.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Displays the about-page.
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class AboutFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.action_about);
        }
        container.removeAllViews();
        return rootView;
    }
}
