package com.jahnold.syncaudiobookplayer;

import android.app.Application;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.Models.BookPath;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 *  App Class
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Parse
        ParseObject.registerSubclass(Book.class);
        ParseObject.registerSubclass(AudioFile.class);
        ParseObject.registerSubclass(BookPath.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

    }
}
