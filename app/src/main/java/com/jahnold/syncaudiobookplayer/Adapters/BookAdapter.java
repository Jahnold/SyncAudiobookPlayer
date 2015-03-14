package com.jahnold.syncaudiobookplayer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;

import java.util.ArrayList;

/**
 *  Book Adapter
 */
public class BookAdapter extends ArrayAdapter<Book> {

    // working copy of the feed
    private ArrayList<Book> mFeed;

    // constructor
    public BookAdapter(Context context, int textViewResourceId, ArrayList<Book> items) {

        super(context, textViewResourceId, items);
        this.mFeed = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // check whether the view needs inflating (it may be recycled)
        if (convertView == null) {

            // inflate the view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_book, null);

        }

        // get the Book at [position] from the array list
        final Book item = mFeed.get(position);

        // get refs to the interface controls
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
        TextView txtAuthor = (TextView) convertView.findViewById(R.id.txt_author);
        TextView txtPercent = (TextView) convertView.findViewById(R.id.txt_percent);
        ImageView imgCover = (ImageView) convertView.findViewById(R.id.img_cover);
        ImageButton btnMenu = (ImageButton) convertView.findViewById(R.id.btn_menu);

        if (item != null) {

            // transfer details from the book to the view
            txtTitle.setText(item.getTitle());
            txtAuthor.setText(item.getAuthor());

        }

        return convertView;

    }
}
