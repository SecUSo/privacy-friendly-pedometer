/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;

import java.util.ArrayList;
import java.util.List;

import static org.secuso.privacyfriendlyactivitytracker.R.id.menu_pause_step_detection;

/**
 * Displays the main app view.
 * In general it's an overview over the users daily, weekly and monthly reports.
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        StepDetectionServiceHelper.startAllIfEnabled(true, getActivity().getApplicationContext());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setSubtitle(R.string.action_main);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        container.removeAllViews();

        ViewPager viewPager = view.findViewById(R.id.pager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onDetach(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        StepDetectionServiceHelper.stopAllIfNotRequired(getActivity().getApplicationContext());
        super.onDetach();
    }

    @Override
    public void onPause(){
        StepDetectionServiceHelper.stopAllIfNotRequired(getActivity().getApplicationContext());
        super.onPause();
    }

    private void setupViewPager(ViewPager viewPager) {
        new ViewPagerAdapter(null);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(DailyReportFragment.newInstance(), getString(R.string.day));
        adapter.addFragment(WeeklyReportFragment.newInstance(), getString(R.string.week));
        adapter.addFragment(MonthlyReportFragment.newInstance(), getString(R.string.month));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_options_overview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        setPauseContinueMenuItemVisibility(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        switch(item.getItemId()){
            case R.id.menu_pause_step_detection:
                editor.putBoolean(getString(R.string.pref_step_counter_enabled), false);
                editor.apply();
                StepDetectionServiceHelper.stopAllIfNotRequired(getActivity().getApplicationContext());
                return true;
            case R.id.menu_continue_step_detection:
                editor.putBoolean(getString(R.string.pref_step_counter_enabled), true);
                editor.apply();
                StepDetectionServiceHelper.startAllIfEnabled(true, getActivity().getApplicationContext());
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets the visibility of pause and continue buttons in given menu
     * @param menu
     */
    private void setPauseContinueMenuItemVisibility(Menu menu){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean isStepCounterEnabled = sharedPref.getBoolean(getString(R.string.pref_step_counter_enabled), true);
        MenuItem continueStepDetectionMenuItem = menu.findItem(R.id.menu_continue_step_detection);
        MenuItem pauseStepDetectionMenuItem = menu.findItem(R.id.menu_pause_step_detection);
        if(isStepCounterEnabled){
            continueStepDetectionMenuItem.setVisible(false);
            pauseStepDetectionMenuItem.setVisible(true);
        }else {
            continueStepDetectionMenuItem.setVisible(true);
            pauseStepDetectionMenuItem.setVisible(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_step_counter_enabled)) || key.equals(getString(R.string.pref_use_step_hardware))){
            this.getActivity().invalidateOptionsMenu();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public androidx.fragment.app.Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        StepDetectionServiceHelper.startAllIfEnabled(true, getActivity().getApplicationContext());
        super.onAttach(activity);
    }
}
