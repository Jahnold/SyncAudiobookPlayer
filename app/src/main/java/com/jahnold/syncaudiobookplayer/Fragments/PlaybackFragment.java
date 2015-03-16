package com.jahnold.syncaudiobookplayer.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;

/**
 *  Playback Fragment
 */
public class PlaybackFragment extends Fragment implements View.OnClickListener {

    private Book mBook;

    // empty constructor
    public PlaybackFragment() {}

    // setters
    public void setBook(Book book) { mBook = book; }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_playback,container,false);

        // get refs to controls
        ImageView imgCover = (ImageView) v.findViewById(R.id.img_cover);
        ImageButton btnBack = (ImageButton) v.findViewById(R.id.btn_back);
        ImageButton btnPlayPause = (ImageButton) v.findViewById(R.id.btn_play_pause);
        ImageButton btnSpecialPause = (ImageButton) v.findViewById(R.id.btn_special_pause);
        ImageButton btnForward = (ImageButton) v.findViewById(R.id.btn_forward);
        TextView txtTitle = (TextView) v.findViewById(R.id.txt_title);
        TextView txtAuthor = (TextView) v.findViewById(R.id.txt_author);
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);


        // set things from the book
        if (mBook != null) {

            txtTitle.setText(mBook.getTitle());
            txtAuthor.setText(mBook.getAuthor());

            progressBar.setMax(mBook.getLength());
            progressBar.setProgress(mBook.getCurrentPosition());

        }

        // set the click listeners
        btnBack.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnSpecialPause.setOnClickListener(this);

        return v;
    }

    /**
    *   Route the onClick events to the appropriate method
    */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back:
                onBackClick();
                break;
            case R.id.btn_forward:
                onForwardClick();
                break;
            case R.id.btn_play_pause:
                onPlayPauseClick();
                break;
            case R.id.btn_special_pause:
                onSpecialPauseClick();
                break;
        }
    }

    public void onPlayPauseClick() {

    }

    public void onBackClick() {

    }

    public void onSpecialPauseClick() {

    }

    public void onForwardClick() {

    }


}
