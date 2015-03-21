package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.jahnold.syncaudiobookplayer.Views.TimerTextView;

/**
 *  Playback Fragment
 */
public class PlaybackFragment extends Fragment implements View.OnClickListener {

    private Book mBook;
    private ImageButton mBtnPlayPause;
    private SeekBar mSeekBar;
    private TimerTextView mTxtProgress;
    private PlayerService mPlayerService;
    private Handler mHandler;

    private Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {

            // update timer and seekbar
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSeekBar.setProgress(mPlayerService.getCurrentPosition());
                    mTxtProgress.setTime(mPlayerService.getCurrentPosition());
                }
            });

            mHandler.postDelayed(mProgressChecker, 500);
        }
    };

    // empty constructor
    public PlaybackFragment() {}

    // setters
    public void setBook(Book book) { mBook = book; }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // get a ref to the player service
        mPlayerService = ((MainActivity) activity).getPlayerService();
        mHandler = new Handler();

    }



    private void startProgressChecker() {

        new Thread(mProgressChecker).start();
//         mProgressChecker.run();
    }

    private void stopProgressChecker() {
        mHandler.removeCallbacks(mProgressChecker);
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
        mTxtProgress = (TimerTextView) v.findViewById(R.id.txt_progress);
        TimerTextView txtTotal = (TimerTextView) v.findViewById(R.id.txt_total);
        mSeekBar = (SeekBar) v.findViewById(R.id.seek_bar);

        // set the icon for play/pause depending on whether the service is current playing
        mBtnPlayPause.setBackgroundResource((mPlayerService.isPlaying()) ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow);

        // set things from the book
        if (mBook != null) {

            txtTitle.setText(mBook.getTitle());
            txtAuthor.setText(mBook.getAuthor());
            txtTotal.setTime(mBook.getLength());
            mTxtProgress.setTime(mBook.getCurrentPosition());

            mSeekBar.setMax(mBook.getLength());
            mSeekBar.setProgress(mBook.getCurrentPosition());

        }

        // set the click listeners
        btnBack.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        btnSpecialPause.setOnClickListener(this);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTxtProgress.setTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopProgressChecker();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayerService.seekTo(seekBar.getProgress());
                startProgressChecker();

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

                break;

            case R.id.btn_forward:

                break;

            case R.id.btn_play_pause:

                // set the icon for play/pause depending on whether the service is current playing
                // also stop/start the progress checker
                if (mPlayerService.isPlaying()) {
                    mBtnPlayPause.setBackgroundResource(R.drawable.ic_action_play_arrow);
                    stopProgressChecker();
                }
                else {
                    mBtnPlayPause.setBackgroundResource(R.drawable.ic_action_pause);
                    startProgressChecker();
                }

                // pass the request to the service
                mPlayerService.playPause(mBook);

                break;

            case R.id.btn_special_pause:

                break;
        }
    }




}
