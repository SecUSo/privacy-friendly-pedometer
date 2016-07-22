package org.secuso.privacyfriendlystepcounter.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.secuso.privacyfriendlystepcounter.fragments.AboutFragment;
import org.secuso.privacyfriendlystepcounter.fragments.DailyReportFragment;
import org.secuso.privacyfriendlystepcounter.fragments.HelpFragment;
import org.secuso.privacyfriendlystepcounter.fragments.MainFragment;
import org.secuso.privacyfriendlystepcounter.fragments.MonthlyReportFragment;
import org.secuso.privacyfriendlystepcounter.fragments.WeeklyReportFragment;
import org.secuso.privacyfriendlystepcounter.utils.StepDetectionServiceHelper;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Main view incl. navigation drawer and fragments
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DailyReportFragment.OnFragmentInteractionListener, WeeklyReportFragment.OnFragmentInteractionListener, MonthlyReportFragment.OnFragmentInteractionListener  {

    public static final String LOG_TAG = MainActivity.class.toString();

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set toolbar as actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // init actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        }

        // init navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_main);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // Load first view
        final android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainFragment(), "MainFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        // Start step detection if enabled and not yet started
        StepDetectionServiceHelper.startAllIfEnabled(this);
        Log.i(LOG_TAG, "MainActivity initialized");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Remove comment in case menu on the right is needed
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // handle the clicks on navigation drawer items
        Fragment fragment = null;
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_home:
                fragment = new MainFragment();
                break;
            case R.id.menu_training:
                break;
            case R.id.menu_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_help:
                fragment = new HelpFragment();
                break;
            case R.id.menu_about:
                fragment = new AboutFragment();
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return fragment != null;
    }
}
