package com.jahnold.syncaudiobookplayer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.jahnold.syncaudiobookplayer.Fragments.BookListFragment;
import com.jahnold.syncaudiobookplayer.Fragments.LogInFragment;
import com.jahnold.syncaudiobookplayer.Fragments.NavigationDrawerFragment;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // set up the nav draw
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

        // check if a user is logged in
        if (ParseUser.getCurrentUser() == null) {

            // no user - show log-in fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new LogInFragment(), "BookListFragment")
                    .commit();
        }
        else {

            // show the book list fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new BookListFragment(), "BookListFragment")
                    .commit();
        }


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // catch the number 3 because this is an activity rather than a fragment
        if (position == 2) {

            Intent fileExploreIntent = new Intent(
                    FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                    null,
                    getApplicationContext(),
                    FileBrowserActivity.class
            );

            // don't show hidden dirs
            fileExploreIntent.putExtra(FileBrowserActivity.showCannotReadParameter, false);

            startActivityForResult(
                    fileExploreIntent,
                    454
            );

        }

        Fragment fragment;
        String tag;
        switch (position) {
            case 0:
                fragment = new BookListFragment();
                tag = "BookListFragment";
                break;


            default:
                fragment = new BookListFragment();
                tag = "BookListFragment";
                break;

        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.nav_all_books);
                break;
            case 2:
                mTitle = getString(R.string.nav_authors);
                break;
            case 3:
                mTitle = getString(R.string.nav_import);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 454 && resultCode == MainActivity.RESULT_OK && data != null) {


            String directory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
            Book.createFromLocal(directory);
        }
    }


}
