package com.jahnold.syncaudiobookplayer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.Models.BookPath;
import com.jahnold.syncaudiobookplayer.Services.PlayerService;
import com.jahnold.syncaudiobookplayer.Util.Installation;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseObject;

/**
 *  App Class
 */
public class App extends Application {

    private static PlayerService sPlayerService;
    private boolean mPlayerBound = false;
    private Intent mPlayerIntent;
    private static String sInstallationId;

    public static PlayerService getPlayerService() {

        return sPlayerService;
    }

    public static String getInstallId() {
        return sInstallationId;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Parse

        // Enable Crash Reporting
        ParseCrashReporting.enable(this);

        ParseObject.registerSubclass(Book.class);
        ParseObject.registerSubclass(AudioFile.class);
        ParseObject.registerSubclass(BookPath.class);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        // set up ImageLoader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        // start/bind the service
        if (mPlayerIntent == null) {
            mPlayerIntent = new Intent(this, PlayerService.class);
            startService(mPlayerIntent);
            bindService(mPlayerIntent, playerConnection, Context.BIND_AUTO_CREATE);

        }

        sInstallationId = Installation.id(this);
    }

    // connect to the player service
    private ServiceConnection playerConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            sPlayerService = binder.getService();
            mPlayerBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mPlayerBound = false;

        }

    };


}
