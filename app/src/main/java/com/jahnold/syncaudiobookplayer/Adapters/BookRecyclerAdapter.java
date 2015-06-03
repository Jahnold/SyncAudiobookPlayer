package com.jahnold.syncaudiobookplayer.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Util.Util;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.ArrayList;

/**
 *  Recycler Adapter
 */
public class BookRecyclerAdapter extends RecyclerAdapter<Book, BookRecyclerAdapter.ViewHolder> {

    //private ArrayList<Book> mBooks;
    private RecyclerAdapterItemOnClickListener mClickListener;

    public interface ViewHolderItemClickListener {
        public void onClick(int position);
    }

    // ViewHolder holds refs to all the view items of the adapter layout
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView txtTitle;
        public TextView txtAuthor;
        public ImageView imgCover;
        public ImageButton btnMenu;
        public ProgressBar progressBar;
        public int position;
        private ViewHolderItemClickListener mListener;

        public ViewHolder(View v, ViewHolderItemClickListener listener) {

            super(v);

            // set the listener
            mListener = listener;

            // get refs to the interface controls
            txtTitle = (TextView) v.findViewById(R.id.txt_title);
            txtAuthor = (TextView) v.findViewById(R.id.txt_author);
            imgCover = (ImageView) v.findViewById(R.id.img_cover);
            btnMenu = (ImageButton) v.findViewById(R.id.btn_menu);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

            // set the click listener for the whole view
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mListener.onClick(position);
        }
    }

    public interface RecyclerAdapterItemOnClickListener {
        public void onClick(int position);
    }

    public void setOnItemClickListener(RecyclerAdapterItemOnClickListener listener) {
        mClickListener = listener;
    }

    // constructor
    public BookRecyclerAdapter(ArrayList<Book> books) {
        mItems = books;
    }

    // creates a new view
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_book, viewGroup, false);

        // return a view holder for our view
        return new ViewHolder(v, new ViewHolderItemClickListener() {
            @Override
            public void onClick(int position) {
                mClickListener.onClick(position);
            }
        });

    }

    // replaces the contents of a view (called by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {

        // get the book at the given position
        Book book = getItem(position);

        if (book != null) {

            // tell the view holder the position of this item
            viewHolder.position = position;

            // transfer details from the book to the view
            viewHolder.txtTitle.setText(book.getTitle());
            viewHolder.txtAuthor.setText(book.getAuthor());

            // clear any existing colour filter
            viewHolder.imgCover.clearColorFilter();

            // set the cover picture
            if (book.getCover() == null) {
                viewHolder.imgCover.setImageResource(R.drawable.adapter_blank);

                // set a colour filter based on the item id
                viewHolder.imgCover.setColorFilter(Util.colorFromObjectId(book.getObjectId()));

            }
            else {
                ParseFile cover = book.getCover();
                cover.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {

                        if (e == null) {

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            viewHolder.imgCover.setImageBitmap(bitmap);

                        } else { e.printStackTrace(); }
                    }
                });
            }

            // set the progress bar
            viewHolder.progressBar.setMax(book.getLength());
            viewHolder.progressBar.setProgress(book.getCumulativePosition() + book.getCurrentFilePosition());

            // if the book isn't on this device change the text color to indicate
            int colourNormal = viewHolder.itemView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light);
            int colourMissing = viewHolder.itemView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_disabled_material_light);
            viewHolder.txtTitle.setTextColor((book.onDevice()) ? colourNormal : colourMissing);
            viewHolder.txtAuthor.setTextColor((book.onDevice()) ? colourNormal : colourMissing);

        }

    }


}
