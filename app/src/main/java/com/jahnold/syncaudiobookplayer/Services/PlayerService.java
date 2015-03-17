package com.jahnold.syncaudiobookplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.BookPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *  Audio Playback Service
 */
public class PlayerService extends Service
    implements  MediaPlayer.OnPreparedListener,
                MediaPlayer.OnErrorListener,
                MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    private int mCurrentFile;                           // current audio file in the array
    private int mPosition;                              // position in the audio file
    private ArrayList<AudioFile> mAudioFiles;           // array of playlist of audio files
    private BookPath mBookPath;                         // path to the files on this install
    private final IBinder mBinder = new PlayerBinder();

    private final String TAG = "Playback Service";

    // setters
    public void setAudioFiles(ArrayList<AudioFile> files) { mAudioFiles = files; }
    public void setPosition(int position) { mPosition = position; }
    public void setBookPath(BookPath bookPath) { mBookPath = bookPath; }
    public void setCurrentFile(int currentFile) { mCurrentFile = currentFile; }


    @Override
    public void onCreate() {

        super.onCreate();

        mPosition = 0;
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

        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mp.start();

    }



    public void playAudioFile() {

        mMediaPlayer.reset();

        // get the first audiofile
        AudioFile file = mAudioFiles.get(mCurrentFile);

        Log.d(TAG, "file:" + file.getFilename());
        Log.d(TAG, "path:" + mBookPath.getPath());

        File filea = new File(mBookPath.getPath() + File.separator +  file.getFilename());
        boolean exists = filea.exists();

        try {
            mMediaPlayer.setDataSource(mBookPath.getPath() + File.separator + file.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();

    }

    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }


}
