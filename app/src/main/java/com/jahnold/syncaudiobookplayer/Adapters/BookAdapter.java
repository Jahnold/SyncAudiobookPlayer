package com.jahnold.syncaudiobookplayer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.App;
import com.jahnold.syncaudiobookplayer.Fragments.BookDetailsFragment;
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
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);

        if (item != null) {

            // transfer details from the book to the view
            txtTitle.setText(item.getTitle());
            txtAuthor.setText(item.getAuthor());

            // set the cover picture
            if (item.getCover() == null) {
                imgCover.setImageResource(R.drawable.book);
            }

            // set the progress bar
            progressBar.setMax(item.getLength());
            progressBar.setProgress(item.getCumulativePosition() + item.getCurrentFilePosition());

        }

        // create the popup menu
        final PopupMenu popupMenu = new PopupMenu(getContext(), btnMenu);
        popupMenu.getMenu().add(Menu.NONE, 0, Menu.NONE, getContext().getString(R.string.menu_details));
        popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, getContext().getString(R.string.menu_delete));

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
        btnMenu.setFocusable(false);

        // set up the popup menu item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case 0:

                        // show the book details
                        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
                        bookDetailsFragment.setBook(item);
                        ((MainActivity) getContext()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container,bookDetailsFragment, "BookDetailsFragment")
                                .addToBackStack(null)
                                .commit();

                        break;

                    case 1:

                        break;
                }

                return false;
            }
        });

        return convertView;

    }
}
