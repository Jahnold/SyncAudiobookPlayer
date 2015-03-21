package com.jahnold.syncaudiobookplayer.Activities;

import android.app.ProgressDialog;
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


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        PlaybackFragment.PlaybackControls {


    private NavigationDrawerFragment mNavigationDrawerFragment;     // nav draw fragment
    private CharSequence mTitle;                                    // last screen title
    private PlayerService mPlayerService;
    private Intent mPlayerIntent;
    private boolean mPlayerBound = false;

    // getters
    public PlayerService getPlayerService() { return mPlayerService; }

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

        // show the book list fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new BookListFragment(), "BookListFragment")
                .commit();

    }

    // connect to the player service
    private ServiceConnection playerConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mPlayerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if (mPlayerIntent == null) {
            mPlayerIntent = new Intent(this, PlayerService.class);
            bindService(mPlayerIntent, playerConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayerIntent);
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

            // start the activity
            startActivityForResult(fileExploreIntent, 454);

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

            // create a progress dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.title_progress_dialog));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            String directory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
            Book.createFromLocal(this, directory, progressDialog);
        }
    }

    /*
    *   Playback Controls
    *       Interface
    */

    public void onPlayPauseClick() {

//        if (!mPlayerService.isPrepared()) {
//            // not started playing yet
//            mPlayerService.begin();
//        }
//        else if (mPlayerService.isPlaying()) {
//            // already playing
//            mPlayerService.pause();
//        }
//        else {
//            // paused
//            mPlayerService.play();
//        }

    }

    public void onBackClick() {

    }

    public void onSpecialPauseClick() {

    }

    public void onForwardClick() {

    }

    public void setBook(Book book) {

        book.getBookPathForCurrentDevice(
                getApplicationContext(),
                new GetCallback<BookPath>() {
                    @Override
                    public void done(BookPath bookPath, ParseException e) {
                        if (e == null) {
                            mPlayerService.setBookPath(bookPath);
                        }
                        else {
                            e.printStackTrace();
                        }
                    }
                }
        );
        AudioFile.loadForBook(
                book,
                new FindCallback<AudioFile>() {
                    @Override
                    public void done(List<AudioFile> audioFiles, ParseException e) {
                        if (e == null) {
                            mPlayerService.setAudioFiles((ArrayList<AudioFile>)audioFiles);
                        }
                        else {
                            e.printStackTrace();
                        }
                    }
                }
        );
        mPlayerService.setBook(book);
    }

    public void setSeekbar(SeekBar seekbar) {
        mPlayerService.setSeekBar(seekbar);
    }

}
