package com.jahnold.syncaudiobookplayer.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.jahnold.syncaudiobookplayer.Fragments.BookDetailsFragment;
import com.jahnold.syncaudiobookplayer.Fragments.BookListFragment;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

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
        final ImageView imgCover = (ImageView) convertView.findViewById(R.id.img_cover);
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
            else {
                ParseFile cover = item.getCover();
                cover.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {

                        if (e == null) {

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imgCover.setImageBitmap(bitmap);

                        } else { e.printStackTrace(); }
                    }
                });
            }

            // set the progress bar
            progressBar.setMax(item.getLength());
            progressBar.setProgress(item.getCumulativePosition() + item.getCurrentFilePosition());

            // if the book isn't on this device change the text color to indicate
            int colourNormal = convertView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light);
            int colourMissing = convertView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_disabled_material_light);
            txtTitle.setTextColor((item.onDevice()) ? colourNormal : colourMissing);
            txtAuthor.setTextColor((item.onDevice()) ? colourNormal : colourMissing);

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

                        // delete the book
                        if (item != null) {

                            // create the click listener for the confirmation dialog
                            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == DialogInterface.BUTTON_POSITIVE) {

                                        // delete from back end
                                        item.deleteFromLibrary();

                                        // remove item from book list
                                        BookListFragment bookListFragment = (BookListFragment) ((MainActivity) getContext()).getSupportFragmentManager().findFragmentByTag("BookListFragment");
                                        bookListFragment.removeBook(item);
                                    }
                                }
                            };

                            // create and show the confirmation dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder
                                    .setTitle(getContext().getString(R.string.title_import_book_dialog))
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

        return convertView;

    }
}
