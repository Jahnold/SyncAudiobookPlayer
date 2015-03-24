package com.jahnold.syncaudiobookplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Adapters.BookAdapter;
import com.jahnold.syncaudiobookplayer.App;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 *  Fragment which lists all books for user
 */
public class BookListFragment extends Fragment {

    private BookAdapter mAdapter;
    private ArrayList<Book> mBooks = new ArrayList<>();

    // empty constructor
    public BookListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_book_list,container, false);

        // get refs
        ListView bookList = (ListView) v.findViewById(R.id.list_books);

        // set up the adapter and set to to the list view
        mAdapter = new BookAdapter(
                getActivity(),
                0,
                mBooks
        );
        bookList.setAdapter(mAdapter);

        // set up the click listener for the list items (books)
        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // get the clicked book
                Book book = mAdapter.getItem(position);

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
                ((MainActivity) getActivity()).getNavigationDrawerFragment().setCurrentSelectedPosition(3);

            }
        });

        // get all the books
        Book.loadAll(new FindCallback<Book>() {
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

        return v;
    }
}
