package com.jahnold.syncaudiobookplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.Models.BookPath;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

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

    private MediaPlayer mMediaPlayer;
    private ArrayList<AudioFile> mAudioFiles;           // array of playlist of audio files
    private BookPath mBookPath;                         // path to the files on this install
    private Book mBook;                                 // the book that's being played
    private final IBinder mBinder = new PlayerBinder();
    private boolean mPrepared = false;                  // tracks whether the media player is prepared


    //private final String TAG = "Playback Service";

    // setters
    public void setBook(Book book) { mBook = book; }


    // getters
    public boolean isPlaying() { return mMediaPlayer.isPlaying(); }
    public Book getBook() { return mBook; }

    @Override
    public void onCreate() {

        super.onCreate();

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

        mMediaPlayer.stop();
        mMediaPlayer.release();

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mMediaPlayer.stop();

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
            mBook.setCurrentFilePosition(mMediaPlayer.getCurrentPosition());
            mBook.saveInBackground();
            // pause the seek observer

        }
        else {
            // paused
            mMediaPlayer.start();

        }

    }

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

    private void setBookThenBeginPlayback(final Book book) {

        mBook = book;

        // get the bookPath
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

        mPrepared = false;
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
            return -1;
        }

    }

    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }


}
