package com.jahnold.syncaudiobookplayer.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Util.NudgeDetector;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Audio Playback Service
 */
public class PlayerService extends Service
    implements  MediaPlayer.OnPreparedListener,
                MediaPlayer.OnErrorListener,
                MediaPlayer.OnCompletionListener {

    private static final int NOTIFICATION_ID = 7;
    private static final int PAUSE_TIMER = 0;
    private static final int PAUSE_END_OF_FILE = 1;

    private final IBinder mBinder = new PlayerBinder();

    private MediaPlayer mMediaPlayer;
    private ArrayList<AudioFile> mAudioFiles;           // array of playlist of audio files
    private Book mBook;                                 // the book that's being played
    private boolean mPrepared = false;                  // tracks whether the media player is prepared
    private boolean mPauseAtEndOfFile = false;          // track whether the user has asked to pause at the end of the current file
    private boolean mSettingNewBook = false;            // changes behaviour for when a new book is being set up
    private boolean mContinueOnNudge = false;           // whether to continue playing if the device is nudged
    private int mCountdownRemaining = -1;               // time in milliseconds until pause, -1 if no countdown
    private int mNudgeTimeRemaining = -1;               // time in milliseconds until the nudge detector is disabled
    private int mCountdownLength;                       // length in milliseconds of the countdown
    private CountDownTimer mCountDownTimer;
    private NudgeDetector mNudgeDetector;


    //private final String TAG = "Playback Service";

    // setters
    public void setPauseAtEndOfFile(boolean pauseAtEndOfFile) {
        mPauseAtEndOfFile = pauseAtEndOfFile;
        // cancel any countdown timers
        cancelCountdownTimer();
    }
    public void setContinueOnNudge(boolean continueOnNudge) { mContinueOnNudge = continueOnNudge; }


    // getters
    public boolean isPlaying() {

        // hopefully this should short circuit so that if the media player
        // is not prepared we won't get an exception (and we also know it's not playing)
        return mPrepared && mMediaPlayer.isPlaying();

    }
    public Book getBook() {

        if (mBook != null) {
            return mBook;
        }
        else {
            return null;
        }

    }

    /**
     *  If there is a special pause set returns the time left until pause in milliseconds
     *  If there is no pause set then it will return -1
     */
    public int getCountdownRemaining() {

        if (mCountdownRemaining != -1) {
            // timer countdown
            return mCountdownRemaining;
        }
        else if (mPauseAtEndOfFile) {
            // end of file countdown
            return mAudioFiles.get(mBook.getCurrentFile()).getLength() - mMediaPlayer.getCurrentPosition();
        }
        else {
            // no countdown
            return -1;
        }

    }

    /**
     *  Returns the current position (of the whole book) in milliseconds
     *  If the media player is current not prepared it gets the last known position from the book
     */
    public int getCurrentPosition() {

        if (mPrepared  && !mSettingNewBook) {
            return mBook.getCumulativePosition() + mMediaPlayer.getCurrentPosition();
        }
        else {
            return mBook.getCumulativePosition() + mBook.getCurrentFilePosition();
        }

    }

    @Override
    public void onCreate() {

        super.onCreate();

        // initialize media player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        // init the nudge detector
        mNudgeDetector = new NudgeDetector(getApplicationContext());
        mNudgeDetector.setEnabled(true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // first check that there is an intent
        if (intent != null) {

            if (MainActivity.INTENT_PLAY_PAUSE.equals(intent.getAction())) {

                togglePlayback();
                createNotification();

            }

            if (MainActivity.INTENT_EXIT.equals(intent.getAction())) {

                pause();
                clearNotification();
                stopSelf();

            }
        }



        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mMediaPlayer.stop();



        // if we've just set a new book then stop now
        if (mSettingNewBook) {
            return;
        }

        if (mAudioFiles.size() > mBook.getCurrentFile()) {

            // set the cumulative total
            int cumulativeTotal = 0;
            for (int i = 0; i <= mBook.getCurrentFile(); i++) {
                cumulativeTotal += mAudioFiles.get(i).getLength();
            }
            mBook.setCumulativePosition(cumulativeTotal);

            // increment the current file and reset the file position
            mBook.incrementCurrentFile();
            mBook.setCurrentFilePosition(0);

            mBook.saveInBackground();


            prepare();

        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {


        mp.seekTo(mBook.getCurrentFilePosition());
        mPrepared = true;

        // if the pause at end of file flag is on stop now
        if (mPauseAtEndOfFile) {
            mPauseAtEndOfFile = false;
            startNudgeDetector(PAUSE_END_OF_FILE);
            return;
        }

        // check whether to being playback
        if (!mSettingNewBook) mp.start();

    }

    public void togglePlayback() {

        // by this point the book is fully set
        mSettingNewBook = false;

        if (mMediaPlayer.isPlaying()) {
            pause();
        }
        else {
            mMediaPlayer.start();
        }

    }

    /**
     *  Pause playback and save position
     */
    public void pause() {

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            // save the progress
            mBook.setCurrentFilePosition(mMediaPlayer.getCurrentPosition());
            mBook.saveInBackground();
        }
    }

    /**
     *  Seek to a position (milliseconds) within the *book*
     *  Calculates which AudioFile this position will be in
     */
    public void seekTo(int position) {

        int cumulative = 0;

        // find the file that the position is part of
        for (int workingFile = 0; workingFile < mAudioFiles.size(); workingFile++) {

            if (position > cumulative && position < cumulative + mAudioFiles.get(workingFile).getLength()) {
                mBook.setCurrentFile(workingFile);
                break;
            }
            cumulative += mAudioFiles.get(workingFile).getLength();

        }

        // set the new details in the book
        mBook.setCumulativePosition(cumulative);
        mBook.setCurrentFilePosition(position - cumulative);
        mBook.saveInBackground();


        prepare();

    }

    /**
     *  Starts the process of loading a new book into the service
     *  Loads all AudioFiles and then calls the prepare method
     */
    public void setBook(final Book book) {

        // boolean stops playback from starting straight away
        // also stops the media player from updating the current position until book is prepared
        mSettingNewBook = true;
        mPauseAtEndOfFile = false;
        mContinueOnNudge = false;

        // grab the book
        mBook = book;

        // load the AudioFiles
        AudioFile.loadForBook(
                book,
                new FindCallback<AudioFile>() {
                    @Override
                    public void done(List<AudioFile> audioFiles, ParseException e) {
                        if (e == null) {
                            mAudioFiles = (ArrayList<AudioFile>) audioFiles;

                            // prepare for playback
                            prepare();
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
        );

        // set this book as the current book on the user
//        ParseUser user = ParseUser.getCurrentUser();
//        user.put("currentBook", book);
//        user.saveEventually();


    }

    /**
     *  For the currently loaded book prepares the current AudioFile for playback
     *
     */
    private void prepare() {

        mPrepared = false;
        mMediaPlayer.reset();

        // get the current audio file
        AudioFile file = mAudioFiles.get(mBook.getCurrentFile());

        try {
            //mMediaPlayer.setDataSource(mBookPath.getPath() + File.separator + file.getFilename());  <-- old book path version
            mMediaPlayer.setDataSource(mBook.getPathForCurrentDevice() + File.separator + file.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();

    }


    /**
     *  Starts a count down timer for a timed pause.
     *  The onTick method updates the countdown variable so that the PlaybackFragment can access it
     */
    public void setCountdownTimer(int milliseconds) {

        // cancel any EOF pauses
        mPauseAtEndOfFile = false;

        // create a new countdown timer
        mCountdownLength = milliseconds;
        mCountDownTimer = new CountDownTimer(milliseconds, 400) {

            @Override
            public void onTick(long millisUntilFinished) {
                mCountdownRemaining = (int) millisUntilFinished ;
            }

            @Override
            public void onFinish() {

                // pause playback
                pause();
                mCountdownRemaining = -1;

                // if continue on nudge is set start listening for nudge now
                if (mContinueOnNudge) startNudgeDetector(PAUSE_TIMER);

            }
        }.start();
    }

    /**
     *  Cancels any active count down to pause
     */
    public void cancelCountdownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

    }

    /**
     *  Uses the NudgeDetector to listen for a nudge of the device for 1 minute
     *  If a nudge is detected it resumes playback and reactivates the same special pause
     */
    public void startNudgeDetector(final int pauseType) {

        // create a countdown timer for 1 minute
        final CountDownTimer countDownTimer =  new CountDownTimer(60000, 400) {
            @Override
            public void onTick(long millisUntilFinished) {
                mNudgeTimeRemaining = (int) millisUntilFinished;
            }

            @Override
            public void onFinish() {
                mNudgeDetector.stopDetection();
            }
        };

        // register the listener for the countdown timer
        mNudgeDetector.registerListener(new NudgeDetector.NudgeDetectorEventListener() {
            @Override
            public void onNudgeDetected() {

                // stop nudge detection and the 1 minute countdown
                mNudgeDetector.stopDetection();
                countDownTimer.cancel();
                mNudgeTimeRemaining = -1;

                // reset the pause
                // if it was an EOF pause reactive the boolean
                // if it was a timer pause start a new countdown
                switch (pauseType) {
                    case PAUSE_END_OF_FILE:

                        mPauseAtEndOfFile = true;
                        break;

                    case PAUSE_TIMER:

                        setCountdownTimer(mCountdownLength);
                        break;
                }

                // restart playback
                mMediaPlayer.start();
            }
        });

        // start nudge detection and the 1 minute countdown
        mNudgeDetector.startDetection();
        countDownTimer.start();

    }

    public void createNotification() {

        // in the case where a book is not loaded
        // stop the service and end the app
        if (mBook == null) {
            stopSelf();
            return;
        }

        // create a pending intent which brings the user to the playback fragment
        Intent playbackIntent = new Intent(this, MainActivity.class);
        playbackIntent.setAction(MainActivity.INTENT_PLAYBACK);
        PendingIntent playbackPendingIntent = PendingIntent.getActivity(
                this,
                151,
                playbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // create a play/pause intent
        Intent playPauseIntent = new Intent(this, PlayerService.class);
        playPauseIntent.setAction(MainActivity.INTENT_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(
                this,
                152,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // create an exit pending intent - this will quit the app
        Intent exitIntent = new Intent(this, PlayerService.class);
        exitIntent.setAction(MainActivity.INTENT_EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getService(
                this,
                153,
                exitIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // create the remote view for the notification
        final RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification);
        notificationView.setTextViewText(R.id.txt_title, mBook.getTitle());
        notificationView.setTextViewText(R.id.txt_author, mBook.getAuthor());
        notificationView.setOnClickPendingIntent(R.id.btn_play_pause, playPausePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.btn_exit, exitPendingIntent);
        // set the play/pause button image depending on whether the media player is playing or not
        notificationView.setImageViewResource(R.id.btn_play_pause, (isPlaying()) ? R.drawable.ic_action_pause_white : R.drawable.ic_action_play_arrow_white);
        notificationView.setImageViewResource(R.id.img_cover, R.drawable.ic_launcher);

        // build the notification
        final Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(playbackPendingIntent)
                .setSmallIcon(R.drawable.notificaiton)
                .setContentIntent(playbackPendingIntent);

        // set the cover picture
        if (mBook.getCover() != null) {

            ParseFile cover = mBook.getCover();
            cover.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {

                    if (e == null) {

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        notificationView.setImageViewBitmap(R.id.img_cover, bitmap);
                        builder.setContent(notificationView);
                        Notification notification = builder.build();
                        startForeground(NOTIFICATION_ID, notification);

                    } else { e.printStackTrace(); }
                }
            });
        }
        else {
            builder.setContent(notificationView);
            Notification notification = builder.build();
            startForeground(NOTIFICATION_ID, notification);
        }




    }

    public void clearNotification() {

        stopForeground(true);
    }

    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }


}
