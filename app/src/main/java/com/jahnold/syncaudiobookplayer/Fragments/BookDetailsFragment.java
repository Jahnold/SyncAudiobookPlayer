package com.jahnold.syncaudiobookplayer.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Adapters.FileAdapter;
import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  Book Details Fragment
 */
public class BookDetailsFragment extends Fragment implements View.OnClickListener {

    private Book mBook;
    private ArrayList<AudioFile> mAudioFiles = new ArrayList<>();
    private FileAdapter mAdapter;
    private ImageView mCover;
    private TextView mTxtTitle;
    private TextView mTxtAuthor;

    // empty constructor
    public BookDetailsFragment() {}

    // setters
    public void setBook(Book book) { mBook = book; }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_book_details, container, false);

        // get refs
        mCover = (ImageView) v.findViewById(R.id.img_cover);
        mTxtTitle = (TextView) v.findViewById(R.id.txt_title);
        mTxtAuthor = (TextView) v.findViewById(R.id.txt_author);
        ListView listFiles = (ListView) v.findViewById(R.id.list_files);

        // set the onclick listeners
        mTxtAuthor.setOnClickListener(this);
        mTxtTitle.setOnClickListener(this);
        mCover.setOnClickListener(this);

        // fill in some details from the book
        if (mBook != null) {

            mTxtTitle.setText(mBook.getTitle());
            mTxtAuthor.setText(mBook.getAuthor());

            if (mBook.getCover() == null) {
                mCover.setImageResource(R.drawable.adapter_blank);
            }
            else {
                ParseFile cover = mBook.getCover();
                cover.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {

                        if (e == null) {

                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mCover.setImageBitmap(bitmap);

                        } else { e.printStackTrace(); }
                    }
                });
            }
        }

        // set up the adapter and set to to the list view
        mAdapter = new FileAdapter(
                getActivity(),
                0,
                mAudioFiles
        );
        listFiles.setAdapter(mAdapter);

        // load all the audio files
        AudioFile.loadForBook(mBook, new FindCallback<AudioFile>() {
            @Override
            public void done(List<AudioFile> audioFiles, ParseException e) {
                mAdapter.addAll(audioFiles);
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.txt_title:
                editDetails(R.id.txt_title);
                break;
            case R.id.txt_author:
                editDetails(R.id.txt_author);
                break;
            case R.id.img_cover:
                chooseNewCover();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // if this is the result of choosing an image the run the onCoverChosen method
        if (requestCode == 555 && resultCode == MainActivity.RESULT_OK && data != null) {

            ((MainActivity) getActivity()).setSuppressNotification(false);
            onCoverChosen(data);
        }
    }

    private void editDetails(int detail) {

        EditDetailsDialogFragment editDetailsDialogFragment = new EditDetailsDialogFragment();


        switch (detail) {
            case R.id.txt_title:

                editDetailsDialogFragment.setDetailName("Title");
                editDetailsDialogFragment.setCurrentDetail(mBook.getTitle());
                editDetailsDialogFragment.setListener(new EditDetailsDialogFragment.EditDetailsListener() {
                    @Override
                    public void onEditDetailsConfirm(DialogFragment dialog, String detail) {

                        // update the book
                        mBook.setTitle(detail);
                        mBook.saveInBackground();

                        // update the text view
                        mTxtTitle.setText(detail);
                    }
                });
                break;

            case R.id.txt_author:

                editDetailsDialogFragment.setDetailName("Author");
                editDetailsDialogFragment.setCurrentDetail(mBook.getAuthor());
                editDetailsDialogFragment.setListener(new EditDetailsDialogFragment.EditDetailsListener() {
                    @Override
                    public void onEditDetailsConfirm(DialogFragment dialog, String detail) {

                        // update the book
                        mBook.setAuthor(detail);
                        mBook.saveInBackground();

                        // update the text view
                        mTxtAuthor.setText(detail);
                    }
                });

                break;
        }

        // show the dialog
        editDetailsDialogFragment.show(getFragmentManager(),"EditDetailsDialogFragment");
    }

    private void chooseNewCover() {

        // start an intent to get a new cover from the gallery
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        // don't show the notification
        ((MainActivity) getActivity()).setSuppressNotification(true);

        // run intent
        startActivityForResult(intent, 555);
    }

    private void onCoverChosen(Intent data) {

        // set the size we want to scale the image to
        ImageSize imageSize = new ImageSize(400, 400);
        ImageLoader.getInstance().loadImage(
            data.getDataString(),
            imageSize,
            new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                // set the image in the image view
                mCover.setImageBitmap(loadedImage);

                // create a parse file for the bitmap
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                loadedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                final ParseFile cover = new ParseFile(mBook.getObjectId() + ".png", stream.toByteArray());
                cover.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {

                            // associate picture with the book
                            mBook.setCover(cover);
                            mBook.saveInBackground();

                        }
                        else { e.printStackTrace(); }
                    }
                });

            }
        });

    }


}
