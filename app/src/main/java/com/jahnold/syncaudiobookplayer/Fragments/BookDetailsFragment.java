package com.jahnold.syncaudiobookplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Adapters.BookAdapter;
import com.jahnold.syncaudiobookplayer.Adapters.FileAdapter;
import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 *  Book Details Fragment
 */
public class BookDetailsFragment extends Fragment {

    private Book mBook;
    private ArrayList<AudioFile> mAudioFiles = new ArrayList<>();
    private FileAdapter mAdapter;

    // empty constructor
    public BookDetailsFragment() {}

    // setters
    public void setBook(Book book) { mBook = book; }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_book_details, container, false);

        // get refs
        ImageView cover = (ImageView) v.findViewById(R.id.img_cover);
        ImageButton btnEditCover = (ImageButton) v.findViewById(R.id.btn_edit_cover);
        ImageButton btnEditTitle = (ImageButton) v.findViewById(R.id.btn_edit_title);
        ImageButton btnEditAuthor = (ImageButton) v.findViewById(R.id.btn_edit_author);
        TextView txtTitle = (TextView) v.findViewById(R.id.txt_title);
        TextView txtAuthor = (TextView) v.findViewById(R.id.txt_author);
        ListView listFiles = (ListView) v.findViewById(R.id.list_files);

        // fill in some details from the book
        if (mBook != null) {

            txtTitle.setText(mBook.getTitle());
            txtAuthor.setText(mBook.getAuthor());

            if (mBook.getCover() == null) {
                cover.setImageResource(R.drawable.book);
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
}
