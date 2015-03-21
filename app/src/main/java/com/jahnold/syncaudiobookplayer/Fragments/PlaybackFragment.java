package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.jahnold.syncaudiobookplayer.Util.Util;
import com.jahnold.syncaudiobookplayer.Views.TimerTextView;

/**
 *  Playback Fragment
 */
public class PlaybackFragment extends Fragment implements View.OnClickListener {

    public interface PlaybackControls {

        public void setBook(Book book);
        public void setSeekbar(SeekBar seekbar);
        public void onPlayPauseClick();
        public void onBackClick();
        public void onSpecialPauseClick();
        public void onForwardClick();

    }

    private Book mBook;
    private PlaybackControls mCallbacks;
    private ImageButton mBtnPlayPause;
    private SeekBar mSeekBar;
    private boolean isPlaying = false;
    private PlayerService mPlayerService;

    // empty constructor
    public PlaybackFragment() {}

    // setters
    public void setBook(Book book) {

        mBook = book;

    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // get a ref to the interface methods in the activity
        mCallbacks = (PlaybackControls) activity;

        // get a ref to the player service
        mPlayerService = ((MainActivity) activity).getPlayerService();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_playback,container,false);

        // get refs to controls
        ImageView imgCover = (ImageView) v.findViewById(R.id.img_cover);
        ImageButton btnBack = (ImageButton) v.findViewById(R.id.btn_back);
        mBtnPlayPause = (ImageButton) v.findViewById(R.id.btn_play_pause);
        ImageButton btnSpecialPause = (ImageButton) v.findViewById(R.id.btn_special_pause);
        ImageButton btnForward = (ImageButton) v.findViewById(R.id.btn_forward);
        TextView txtTitle = (TextView) v.findViewById(R.id.txt_title);
        TextView txtAuthor = (TextView) v.findViewById(R.id.txt_author);
        final TimerTextView txtProgress = (TimerTextView) v.findViewById(R.id.txt_progress);
        TimerTextView txtTotal = (TimerTextView) v.findViewById(R.id.txt_total);
        mSeekBar = (SeekBar) v.findViewById(R.id.seek_bar);

        // set the icon for play/pause depending on whether the service is current playing
        mBtnPlayPause.setBackgroundResource((mPlayerService.isPlaying()) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

        // set things from the book
        if (mBook != null) {

            txtTitle.setText(mBook.getTitle());
            txtAuthor.setText(mBook.getAuthor());
            txtTotal.setTime(mBook.getLength());
            txtProgress.setTime(mBook.getCurrentPosition());

            mSeekBar.setMax(mBook.getLength());
            mSeekBar.setProgress(mBook.getCurrentPosition());

        }

        // set the click listeners
        btnBack.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        btnSpecialPause.setOnClickListener(this);

        mPlayerService.setSeekBar(mSeekBar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // update the timer
                txtProgress.setTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return v;
    }

    /**
    *   Route the onClick events to the appropriate method
    *   Uses the callback interface to call activity methods
    */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back:

                mCallbacks.onBackClick();
                break;

            case R.id.btn_forward:

                mCallbacks.onForwardClick();
                break;

            case R.id.btn_play_pause:

                // set the icon for play/pause depending on whether the service is current playing
                mBtnPlayPause.setBackgroundResource((mPlayerService.isPlaying()) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                mPlayerService.playPause(mBook);
                break;

            case R.id.btn_special_pause:

                mCallbacks.onSpecialPauseClick();
                break;
        }
    }




}
