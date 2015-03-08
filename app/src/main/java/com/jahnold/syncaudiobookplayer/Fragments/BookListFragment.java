package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jahnold.syncaudiobookplayer.R;

/**
 *  Fragment which lists all books for user
 */
public class BookListFragment extends android.support.v4.app.Fragment {

    // empty constructor
    public BookListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_book_list,container, false);

        // get refs
        ListView bookList = (ListView) v.findViewById(R.id.list_books);

        return v;
    }
}
