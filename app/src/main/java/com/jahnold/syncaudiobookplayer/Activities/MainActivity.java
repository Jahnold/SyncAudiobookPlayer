package com.jahnold.syncaudiobookplayer.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.jahnold.syncaudiobookplayer.App;
import com.jahnold.syncaudiobookplayer.Fragments.BookListFragment;
import com.jahnold.syncaudiobookplayer.Fragments.NavigationDrawerFragment;
import com.jahnold.syncaudiobookplayer.Fragments.PlaybackFragment;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    public static String INTENT_PLAYBACK = "com.jahnold.syncaudiobookplayer.playback";
    public static String INTENT_PLAY_PAUSE = "com.jahnold.syncaudiobookplayer.playpause";
    public static String INTENT_EXIT = "com.jahnold.syncaudiobookplayer.exit";

    private NavigationDrawerFragment mNavigationDrawerFragment;     // nav draw fragment
    private CharSequence mTitle;                                    // last screen title
    private boolean mSuppressNotification = false;

    // getters & setters
    public NavigationDrawerFragment getNavigationDrawerFragment() { return mNavigationDrawerFragment; }
    public void setSuppressNotification(boolean suppressNotification) { mSuppressNotification = suppressNotification; }

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
            savedInstanceState.putInt("selected_navigation_drawer_position", 2);
        }

        // set up the nav draw
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

        // load the last book the user used
        Book book = (Book) ParseUser.getCurrentUser().getParseObject("currentBook");
        if (book != null) {
            book.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null) {
                        Book fetchedBook = (Book) parseObject;
                        PlayerService playerService = App.getPlayerService();
                        if (!fetchedBook.equals(playerService.getBook())) {
                            //playerService.setBook(fetchedBook);
                        }
                    }
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mSuppressNotification = false;
        if (!isChangingConfigurations()) {
            App.getPlayerService().clearNotification();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0:

                // load the book list fragment
                BookListFragment bookListFragment = (BookListFragment) getSupportFragmentManager().findFragmentByTag("BookListFragment");
                if (bookListFragment == null) {
                    bookListFragment = new BookListFragment();
                }

                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, bookListFragment, "BookListFragment")
                        .commit();
                break;

            case 1:

                // load the file browser activity
                Intent fileExploreIntent = new Intent(
                        FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                        null,
                        getApplicationContext(),
                        FileBrowserActivity.class
                );

                // don't show the notification
                mSuppressNotification = true;

                // don't show hidden dirs
                fileExploreIntent.putExtra(FileBrowserActivity.showCannotReadParameter, false);

                // start the activity
                startActivityForResult(fileExploreIntent, 454);
                break;

            case 2:

                // first check if there is a book loaded
                PlayerService playerService = App.getPlayerService();
                if (playerService.getBook() == null) {

                    // there is no book loaded, alert the user then stop
                    Toast.makeText(this, getString(R.string.toast_no_book), Toast.LENGTH_SHORT).show();
                }
                else {
                    // load the play back fragment
                    PlaybackFragment playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentByTag("PlaybackFragment");
                    if (playbackFragment == null) {
                        playbackFragment = new PlaybackFragment();
                    }
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.container, playbackFragment, "PlaybackFragment")
                            .commit();
                }
                break;

            case 3:

                // log out, load auth activity
                ParseUser.logOut();
                Intent intent = new Intent(this, DispatchActivity.class);
                mSuppressNotification = true;
                startActivity(intent);
                finish();
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

            mSuppressNotification = false;

            // create a progress dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.title_progress_dialog));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            String directory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
            Book.createFromLocal(this, directory, progressDialog);
        }

    }

    @Override
    protected void onStop() {

        super.onStop();

        // only show the notification if the activity is actually being moved to the back
        if (!isChangingConfigurations() && !mSuppressNotification) {
            // show the notification
            App.getPlayerService().createNotification();
        }
    }
}
