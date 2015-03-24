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

import com.jahnold.syncaudiobookplayer.App;
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
    private TimerTextView mTxtPause;
    private TimerTextView mTxtTotal;
    private TextView mTxtPauseLabel;
    private TextView mTxtPauseColon;
    private TextView mTxtTitle;
    private TextView mTxtAuthor;
    private ImageView mCover;
    private PlayerService mPlayerService;
    private Handler mHandler;


    /**
     *  The progress checker polls the player service every 500 milliseconds
     *  It updates the playback fragment interface to reflect current playback state
     */
    private Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {

            // update timer and seekbar
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // check to make sure we've got a ref to the service
                    if (mPlayerService == null) {
                        return;
                    }

                    // update the progress
                    if (mPlayerService.getCurrentPosition() != -1) {
                        mSeekBar.setProgress(mPlayerService.getCurrentPosition());
                        mTxtProgress.setTime(mPlayerService.getCurrentPosition());
                    }

                    // update pause countdown if there is one
                    if (mPlayerService.getCountdownRemaining() != -1) {
                        mTxtPause.setTime(mPlayerService.getCountdownRemaining());
                        setPauseTimerVisibility(View.VISIBLE);
                    } else {
                        setPauseTimerVisibility(View.INVISIBLE);
                    }

                    // update the play/pause button to reflect the current playing state
                    mBtnPlayPause.setBackgroundResource((mPlayerService.isPlaying()) ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow);

                }
            });

            // repeat in 500 milliseconds
            mHandler.postDelayed(mProgressChecker, 500);
        }
    };

    // empty constructor
    public PlaybackFragment() {}

    // setters
    //public void setBook(Book book) { mBook = book; }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // get a ref to the player service and init the handler
        mPlayerService = App.getPlayerService();
        mBook = mPlayerService.getBook();
        mHandler = new Handler();

    }

    @Override
    public void onStart() {
        super.onStart();

        // view should have initialized so start polling the service for details
        //startProgressChecker();
    }

    @Override
    public void onPause() {
        super.onPause();

        // fragment is no longer visible so stop polling the service
        stopProgressChecker();
    }

    @Override
    public void onResume() {
        super.onResume();


        // if the fragment is being re-used then we need to make sure we've got the correct book
        // and are showing the correct details for title, author, cover, etc
        mBook = mPlayerService.getBook();

        if (mBook != null) {
            mTxtTitle.setText(mBook.getTitle());
            mTxtAuthor.setText(mBook.getAuthor());
            mTxtTotal.setTime(mBook.getLength());

            if (mBook.getCover() == null) {
                mCover.setImageResource(R.drawable.book);
            }

            mSeekBar.setMax(mBook.getLength());
        }

    }

    private void startProgressChecker() {
        new Thread(mProgressChecker).start();
    }

    private void stopProgressChecker() {
        mHandler.removeCallbacks(mProgressChecker);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_playback,container,false);

        // get refs to controls
        mCover = (ImageView) v.findViewById(R.id.img_cover);
        ImageButton btnBack = (ImageButton) v.findViewById(R.id.btn_back);
        mBtnPlayPause = (ImageButton) v.findViewById(R.id.btn_play_pause);
        ImageButton btnSpecialPause = (ImageButton) v.findViewById(R.id.btn_special_pause);
        ImageButton btnForward = (ImageButton) v.findViewById(R.id.btn_forward);
        mTxtTitle = (TextView) v.findViewById(R.id.txt_title);
        mTxtAuthor = (TextView) v.findViewById(R.id.txt_author);
        mTxtProgress = (TimerTextView) v.findViewById(R.id.txt_progress);
        mTxtPause = (TimerTextView) v.findViewById(R.id.txt_pause_timer);
        mTxtPauseLabel = (TextView) v.findViewById(R.id.txt_pause);
        mTxtPauseColon = (TextView) v.findViewById(R.id.centre);
        mTxtTotal = (TimerTextView) v.findViewById(R.id.txt_total);
        mSeekBar = (SeekBar) v.findViewById(R.id.seek_bar);

        // set the icon for play/pause depending on whether the service is current playing
        mBtnPlayPause.setBackgroundResource((mPlayerService.isPlaying()) ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow);
        //mBtnPlayPause.setBackgroundResource(R.drawable.ic_action_pause);

        startProgressChecker();

        // set things from the book
        if (mBook != null) {

            mTxtTitle.setText(mBook.getTitle());
            mTxtAuthor.setText(mBook.getAuthor());
            mTxtTotal.setTime(mBook.getLength());
            //mTxtProgress.setTime(mBook.getCumulativePosition() + mBook.getCurrentFilePosition());

            if (mBook.getCover() == null) {
                mCover.setImageResource(R.drawable.book);
            }

            mSeekBar.setMax(mBook.getLength());
            //mSeekBar.setProgress(mBook.getCumulativePosition() + mBook.getCurrentFilePosition());

        }



        // set the click listeners
        btnBack.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        btnSpecialPause.setOnClickListener(this);


        // set the seek listener
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
    *
    */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back:
                mPlayerService.seekTo(mPlayerService.getCurrentPosition() - 60000);
                break;

            case R.id.btn_forward:
                mPlayerService.seekTo(mPlayerService.getCurrentPosition() + 60000);
                break;

            case R.id.btn_play_pause:
                mPlayerService.togglePlayback();
                break;

            case R.id.btn_special_pause:
                onSpecialPauseClick();
                break;
        }
    }

    /**
     *  Hides or shows the controls which make up the pause countdown timer
     *
     */
    private void setPauseTimerVisibility(int visibility) {
        mTxtPause.setVisibility(visibility);
        mTxtPauseLabel.setVisibility(visibility);
        mTxtPauseColon.setVisibility(visibility);
    }

    /**
     *  Called when the user clicks on special pause button
     *  Shows the pause dialog fragment with pause options
     */
    private void onSpecialPauseClick() {

        PauseDialogFragment pauseDialogFragment = new PauseDialogFragment();
        pauseDialogFragment.setListener(new PauseDialogFragment.PauseDialogListener() {
            @Override
            public void onPauseConfirm(int pauseType, int timerLength, boolean continueOnNudge) {

                switch (pauseType) {

                    case PauseDialogFragment.PAUSE_END_OF_FILE:

                        // tell the service
                        mPlayerService.setPauseAtEndOfFile(true);
                        // make the countdown visible
                        setPauseTimerVisibility(View.VISIBLE);
                        break;

                    case PauseDialogFragment.PAUSE_TIMER:

                        // tell the service
                        mPlayerService.setCountdownTimer(timerLength * 60 * 1000);
                        // make the countdown visible
                        setPauseTimerVisibility(View.VISIBLE);
                        break;

                    case PauseDialogFragment.PAUSE_NONE:
                        mPlayerService.setPauseAtEndOfFile(false);
                        mPlayerService.cancelCountdownTimer();
                        // make the countdown invisible
                        setPauseTimerVisibility(View.INVISIBLE);
                        break;
                }


            }
        });
        pauseDialogFragment.show(getFragmentManager(), "PauseDialogFragment");
    }


}
