package com.jahnold.syncaudiobookplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.Models.BookPath;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Audio Playback Service
 */
public class PlayerService extends Service
    implements  MediaPlayer.OnPreparedListener,
                MediaPlayer.OnErrorListener,
                MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    private int mCurrentFile;                           // current audio file in the array
    private ArrayList<AudioFile> mAudioFiles;           // array of playlist of audio files
    private BookPath mBookPath;                         // path to the files on this install
    private Book mBook;                                 // the book that's being played
    private final IBinder mBinder = new PlayerBinder();
    private boolean mPrepared = false;                  // tracks whether the mediaplayer is prepared
    private SeekBar mSeekBar;
    private boolean mUpdateSeekBar = true;              // tracks whether user is touching seekbar
    //private MediaObserver mMediaObserver;

    private final String TAG = "Playback Service";

    // setters
    public void setAudioFiles(ArrayList<AudioFile> files) { mAudioFiles = files; }
    public void setBookPath(BookPath bookPath) { mBookPath = bookPath; }
    public void setCurrentFile(int currentFile) { mCurrentFile = currentFile; }
    public void setBook(Book book) { mBook = book; }
    public void setSeekBar(SeekBar seekBar) { mSeekBar = seekBar;}
    public void setUpdateSeekBar(boolean updateSeekBar) { mUpdateSeekBar = updateSeekBar; }

    // getters
    public boolean isPlaying() { return mMediaPlayer.isPlaying(); }
    public Book getBook() { return mBook; }

    @Override
    public void onCreate() {

        super.onCreate();

        mCurrentFile = 0;
        mMediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    /**
     *  Initialise the Media PlayerService
     */
    public void initMediaPlayer() {

        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        //mMediaObserver.stop();
        mMediaPlayer.stop();
        mMediaPlayer.release();

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mMediaPlayer.stop();

        //mMediaObserver.stop();
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

        mPrepared = true;
        mp.seekTo(mBook.getCurrentFilePosition());
        mp.start();
        //mMediaObserver = new MediaObserver();
        //new Thread(mMediaObserver).start();

    }

    public void playPause(Book book) {

        if (book != mBook) {
            // whole new book
            setBookThenBeginPlayback(book);
        }
        else if (mMediaPlayer.isPlaying()) {

            // already playing
            mMediaPlayer.pause();
            // save the progress
            mBook.saveInBackground();
            // pause the seek observer

        }
        else {
            // paused
            mMediaPlayer.start();

        }

    }

    public void seekTo(int position) {

        mMediaPlayer.seekTo(position - mBook.getCumulativePosition());

    }

    private void setBookThenBeginPlayback(final Book book) {

        mBook = book;

        // get the bookpath
        book.getBookPathForCurrentDevice(
                getApplicationContext(),
                new GetCallback<BookPath>() {
                    @Override
                    public void done(BookPath bookPath, ParseException e) {
                        if (e == null) {
                            mBookPath = bookPath;

                            // get the audio files
                            AudioFile.loadForBook(
                                    book,
                                    new FindCallback<AudioFile>() {
                                        @Override
                                        public void done(List<AudioFile> audioFiles, ParseException e) {
                                            if (e == null) {
                                                mAudioFiles = (ArrayList<AudioFile>) audioFiles ;

                                                // prepare for playback
                                                prepare();
                                            }
                                            else {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                            );
                        }
                        else {
                            e.printStackTrace();
                        }
                    }
                }
        );


    }

    private void prepare() {

        mMediaPlayer.reset();

        // get the first audio file
        AudioFile file = mAudioFiles.get(mBook.getCurrentFile());

        try {
            mMediaPlayer.setDataSource(mBookPath.getPath() + File.separator + file.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();

    }

    public int getCurrentPosition() {

        if (mPrepared) {
            return mBook.getCumulativePosition() + mMediaPlayer.getCurrentPosition();
        }
        else {
            return 0;
        }

    }

    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }

//    /**
//     *  MediaObserver updates the seek bar
//     */
//    private class MediaObserver implements Runnable {
//        private AtomicBoolean stop = new AtomicBoolean(false);
//
//        public void stop() {
//            stop.set(true);
//        }
//
//        @Override
//        public void run() {
//            while (!stop.get()) {
//
//                if (mUpdateSeekBar) {
//                    mSeekBar.setProgress(mBook.getCumulativePosition() + mMediaPlayer.getCurrentPosition());
//                }
//
//                // update the book
//                mBook.setCurrentFilePosition(mMediaPlayer.getCurrentPosition());
//
//                // sleep
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
