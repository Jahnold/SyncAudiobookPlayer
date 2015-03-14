package com.jahnold.syncaudiobookplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jahnold.syncaudiobookplayer.Adapters.BookAdapter;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 *  Fragment which lists all books for user
 */
public class BookListFragment extends android.support.v4.app.Fragment {

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

        // get all the books
        Book.loadAll(new FindCallback<Book>() {
            @Override
            public void done(List<Book> books, ParseException e) {

                if (e == null) {
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
