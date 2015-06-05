package com.jahnold.syncaudiobookplayer.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Util.Util;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

/**
 *  View Holder for the Book List Items
 */
public class BookListViewHolder extends RecyclerView.ViewHolder {

    public interface ViewHolderItemClickListener {
        public void onClick(int position);
    }

    public interface PopupMenuItemClickListener {
        public boolean onClick(int position, MenuItem menuItem);
    }

    private Context mContext;
    private TextView mTxtTitle;
    private TextView mTxtAuthor;
    private ImageView mImgCover;
    private ImageButton mBtnMenu;
    private ProgressBar mProgressBar;
    private int mPosition;

    // listeners
    private ViewHolderItemClickListener mItemClickListener;
    private PopupMenuItemClickListener mMenuItemClickListener;

    private Book mBook;

    public BookListViewHolder(View v, ViewHolderItemClickListener listener, PopupMenuItemClickListener popupListener) {

        super(v);

        // grab the context
        mContext = v.getContext();

        // set the listeners
        mItemClickListener = listener;
        mMenuItemClickListener = popupListener;

        // get refs to the interface controls
        mTxtTitle = (TextView) v.findViewById(R.id.txt_title);
        mTxtAuthor = (TextView) v.findViewById(R.id.txt_author);
        mImgCover = (ImageView) v.findViewById(R.id.img_cover);
        mBtnMenu = (ImageButton) v.findViewById(R.id.btn_menu);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        // set the click listener for the whole view
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onClick(mPosition);
            }
        });

        initPopUpMenu();

    }

    public void setModel(Book book, int position) {

        if (book != null) {

            mBook = book;

            // tell the view holder the position of this item
            mPosition = position;

            // transfer details from the book to the view
            mTxtTitle.setText(book.getTitle());
            mTxtAuthor.setText(book.getAuthor());

            // clear any existing colour filter
            mImgCover.clearColorFilter();

            // set the cover picture
            if (book.getCover() == null) {
                mImgCover.setImageResource(R.drawable.adapter_blank);

                // set a colour filter based on the item id
                mImgCover.setColorFilter(Util.colorFromObjectId(book.getObjectId()));

            }
            else {
                ParseFile cover = book.getCover();
                cover.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {

                        if (e == null) {

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mImgCover.setImageBitmap(bitmap);

                        } else { e.printStackTrace(); }
                    }
                });
            }

            // set the progress bar
            mProgressBar.setMax(book.getLength());
            mProgressBar.setProgress(book.getCumulativePosition() + book.getCurrentFilePosition());

            // if the book isn't on this device change the text color to indicate
            int colourNormal = itemView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light);
            int colourMissing = itemView.getResources().getColor(android.support.v7.appcompat.R.color.secondary_text_disabled_material_light);
            mTxtTitle.setTextColor((book.onDevice()) ? colourNormal : colourMissing);
            mTxtAuthor.setTextColor((book.onDevice()) ? colourNormal : colourMissing);

        }

    }

    private void initPopUpMenu() {

        // create the popup menu
        final PopupMenu popupMenu = new PopupMenu(mContext, mBtnMenu);
        popupMenu.getMenu().add(Menu.NONE, 0, Menu.NONE, mContext.getString(R.string.menu_details));
        popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, mContext.getString(R.string.menu_delete));

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
        mBtnMenu.setFocusable(false);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return mMenuItemClickListener.onClick(mPosition, item);

            }

        });

    }

}
