package com.jahnold.syncaudiobookplayer.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.SeekBar;

import com.jahnold.syncaudiobookplayer.Fragments.BookListFragment;
import com.jahnold.syncaudiobookplayer.Fragments.NavigationDrawerFragment;
import com.jahnold.syncaudiobookplayer.Fragments.PlaybackFragment;
import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.Models.BookPath;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    public static String INTENT_PLAYBACK = "com.jahnold.syncaudiobookplayer.playback";
    public static String INTENT_BOOKLIST = "com.jahnold.syncaudiobookplayer.booklist";

    private NavigationDrawerFragment mNavigationDrawerFragment;     // nav draw fragment
    private CharSequence mTitle;                                    // last screen title

    // getters & setters
    public NavigationDrawerFragment getNavigationDrawerFragment() { return mNavigationDrawerFragment; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check for an intent directing to the playback fragment
        Intent intent = getIntent();
        if (INTENT_PLAYBACK.equals(intent.getAction())) {

            if (savedInstanceState == null) {
                savedInstanceState = new Bundle();
            }

            // add an item to the bundle to trick the nav draw into loading the playback fragment
            savedInstanceState.putInt("selected_navigation_drawer_position", 3);
        }

        // set up the nav draw
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // catch the number 2 because this is an activity rather than a fragment
        if (position == 2) {

            Intent fileExploreIntent = new Intent(
                    FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                    null,
                    getApplicationContext(),
                    FileBrowserActivity.class
            );

            // don't show hidden dirs
            fileExploreIntent.putExtra(FileBrowserActivity.showCannotReadParameter, false);

            // start the activity
            startActivityForResult(fileExploreIntent, 454);

        }
        else {

            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();

            switch (position) {
                case 0:
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.container, new BookListFragment(), "BookListFragment")
                            .commit();
                    break;

                case 3:
                    PlaybackFragment playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentByTag("PlaybackFragment");
                    if (playbackFragment == null) {
                        playbackFragment = new PlaybackFragment();
                    }
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.container, playbackFragment, "PlaybackFragment")
                            .commit();
                    break;
            }
        }


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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // return from the file browser
        if (requestCode == 454 && resultCode == MainActivity.RESULT_OK && data != null) {

            // create a progress dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.title_progress_dialog));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            String directory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
            Book.createFromLocal(this, directory, progressDialog);
        }

    }



}
