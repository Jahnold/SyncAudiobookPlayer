package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jahnold.syncaudiobookplayer.Activities.FileBrowserActivity;
import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Adapters.BookAdapter;
import com.jahnold.syncaudiobookplayer.Adapters.BookRecyclerAdapter;
import com.jahnold.syncaudiobookplayer.App;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.jahnold.syncaudiobookplayer.Views.BookListViewHolder;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 *  Fragment which lists all books for user
 */
public class BookListFragment extends Fragment {

    //private BookAdapter mAdapter;
    private BookRecyclerAdapter mAdapter;
    private ArrayList<Book> mBooks = new ArrayList<>();
    private AdView mAdView;

    // empty constructor
    public BookListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_book_list,container, false);

        // get refs
        //ListView bookList = (ListView) v.findViewById(R.id.list_books);
        RecyclerView bookList = (RecyclerView) v.findViewById(R.id.book_recycler);
        LinearLayout emptyView = (LinearLayout) v.findViewById(R.id.empty_book_list);
        final SwipeRefreshLayout swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        mAdView = (AdView) v.findViewById(R.id.adView);

        // set up the ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // set up the recycler view
        bookList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        bookList.setLayoutManager(layoutManager);
        mAdapter = new BookRecyclerAdapter(mBooks);
        bookList.setAdapter(mAdapter);

        // set up the click listener for the list items (books)
        mAdapter.setOnItemClickListener(new BookListViewHolder.ViewHolderItemClickListener() {

            @Override
            public void onClick(final int position) {

                // get the clicked book
                final Book book = mAdapter.getItem(position);

                // check whether the book is on the device
                if (!book.onDevice()) {

                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which == DialogInterface.BUTTON_POSITIVE) {

                                // create an intent to start the file browser
                                Intent fileExploreIntent = new Intent(
                                        FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                                        null,
                                        getActivity(),
                                        FileBrowserActivity.class
                                );

                                // don't show the notification
                                ((MainActivity) getActivity()).setSuppressNotification(true);

                                // don't show hidden dirs
                                fileExploreIntent.putExtra(FileBrowserActivity.showCannotReadParameter, false);
                                fileExploreIntent.putExtra("book_id", book.getObjectId());
                                fileExploreIntent.putExtra("position", position);

                                // start the activity
                                startActivityForResult(fileExploreIntent, 987);

                            }
                        }
                    };


                    // show the import on this device dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                            .setTitle(getString(R.string.title_import_book_dialog))
                            .setMessage("This book has not been imported on the current device.  Do you want to do so now?")
                            .setPositiveButton("Yes", onClickListener)
                            .setNegativeButton("Cancel", onClickListener)
                            .show();

                    // don't continue
                    return;
                }

                // if it's a new book, update the player service
                PlayerService playerService = App.getPlayerService();
                if (!book.equals(playerService.getBook())) {
                    playerService.pause();
                    playerService.setBook(book);
                }

                // switch to the playback fragment
                PlaybackFragment playbackFragment = (PlaybackFragment) getFragmentManager().findFragmentByTag("PlaybackFragment");
                if (playbackFragment == null) {
                    playbackFragment = new PlaybackFragment();
                }

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, playbackFragment, "PlaybackFragment")
                        .addToBackStack(null)
                        .commit();

                // tell the navigation draw that a change has happened
                ((MainActivity) getActivity()).getNavigationDrawerFragment().setCurrentSelectedPosition(2);

            }
        });

        // set the click listener for the popup menu items
        mAdapter.setMenuItemClickListener(new BookListViewHolder.PopupMenuItemClickListener() {
            @Override
            public boolean onClick(int position, MenuItem menuItem) {

                // get the book
                final Book book = mAdapter.getItem(position);

                switch (menuItem.getItemId()) {

                    case 0:

                        // show the book details
                        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
                        bookDetailsFragment.setBook(book);
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container,bookDetailsFragment, "BookDetailsFragment")
                                .addToBackStack(null)
                                .commit();

                        break;

                    case 1:

                        // delete the book
                        if (book != null) {

                            // create the click listener for the confirmation dialog
                            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == DialogInterface.BUTTON_POSITIVE) {

                                        // delete from back end
                                        book.deleteFromLibrary();

                                        // remove item from book list
                                        mAdapter.remove(book);
                                    }
                                }
                            };

                            // create and show the confirmation dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder
                                    .setTitle(getActivity().getString(R.string.title_import_book_dialog))
                                    .setMessage("This will remove this book from your library.  All listening progress will be lost.  No files will be deleted from your device.  Do you wish to continue?")
                                    .setPositiveButton("Yes", onClickListener)
                                    .setNegativeButton("Cancel", onClickListener)
                                    .show();

                        }

                        break;
                }

                return false;

            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // get all the books
                Book.loadAll(false, new FindCallback<Book>() {
                    @Override
                    public void done(List<Book> books, ParseException e) {

                        if (e == null) {
                            mAdapter.clear();
                            mAdapter.addAll(books);
                            swipe.setRefreshing(false);
                        }
                        else {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


        // get all the books
        Book.loadAll(true, new FindCallback<Book>() {
            @Override
            public void done(List<Book> books, ParseException e) {

                if (e == null) {
                    mAdapter.clear();
                    mAdapter.addAll(books);
                }
                else {
                    e.printStackTrace();
                }

            }
        });

        // set the empty view - do it after load so that it doesn't flash up first
//        bookList.setEmptyView(emptyView);

        return v;
    }

    @Override
    public void onPause() {

        // pause the ads
        if (mAdView != null) {
            mAdView.pause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // resume the ads
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {

        // destroy the ads
        if (mAdView != null) {
            mAdView.destroy();
        }

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // return from the file browser
        if (requestCode == 987 && resultCode == MainActivity.RESULT_OK && data != null) {



            // create a progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.title_progress_dialog));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            final String directory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);

            // get the book from the intent
            ParseQuery<Book> query = ParseQuery.getQuery("Book");
            query.getInBackground(data.getStringExtra("book_id"), new GetCallback<Book>() {
                @Override
                public void done(Book book, ParseException e) {
                    book.importFromLocal(getActivity(), directory, progressDialog, data.getIntExtra("position", -1));
                }
            });


        }
    }

    public void addBook(Book book) {
        mAdapter.add(book);
    }

    public void removeBook(Book book) { mAdapter.remove(book);}

    public void updateBook(Book book, int position) {

        mBooks.set(position,book);
        mAdapter.notifyDataSetChanged();
    }
}
