package com.jahnold.syncaudiobookplayer.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Views.BookListViewHolder;

import java.util.ArrayList;

/**
 *  Recycler Adapter
 */
public class BookRecyclerAdapter extends RecyclerAdapter<Book, BookListViewHolder> {

    private BookListViewHolder.ViewHolderItemClickListener mListItemClickListener;
    private BookListViewHolder.PopupMenuItemClickListener mMenuItemClickListener;

    // setters
    public void setOnItemClickListener(BookListViewHolder.ViewHolderItemClickListener listener) { mListItemClickListener = listener; }
    public void setMenuItemClickListener(BookListViewHolder.PopupMenuItemClickListener menuItemClickListener) { mMenuItemClickListener = menuItemClickListener; }

    // constructor
    public BookRecyclerAdapter(ArrayList<Book> books) {
        mItems = books;
    }

    // creates a new view
    @Override
    public BookListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // create a new view
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_book, viewGroup, false);

        // return a view holder for our view
        return new BookListViewHolder(
                view,
                mListItemClickListener,
                mMenuItemClickListener
        );

    }

    // replaces the contents of a view (called by the layout manager)
    @Override
    public void onBindViewHolder(final BookListViewHolder viewHolder, int position) {

        // get the book at the given position and pass it to the view holder
        Book book = getItem(position);
        viewHolder.setModel(book,position);

    }


}
