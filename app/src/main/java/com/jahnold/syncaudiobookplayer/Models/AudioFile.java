package com.jahnold.syncaudiobookplayer.Models;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 *  AudioFile Model
 */
@ParseClassName("AudioFile")
public class AudioFile extends ParseObject {

    // empty constructor required for Parse
    public AudioFile() {}

    public AudioFile(String filename, String title, int trackNumber, boolean listened, int length) {

        setFilename(filename);
        setTitle(title);
        setTrackNumber(trackNumber);
        setListened(listened);
        setLength(length);

    }

    // getters
    public String getFilename() { return getString("filename"); }
    public Book getBook() { return (Book) getParseObject("book"); }
    public String getTitle() { return getString("title"); }
    public int getTrackNumber() { return getInt("trackNumber"); }
    public boolean getListened() { return  getBoolean("listened"); }
    public int getLength() { return getInt("length"); }


    // setters
    public void setFilename(String filename) { put("filename", filename); }
    public void setBook(Book book) { put("book", book); }
    public void setTitle(String title) { put("title", title); }
    public void setTrackNumber(int trackNumber) { put("trackNumber", trackNumber); }
    public void setListened(boolean listened) { put("listened", listened); }
    public void setLength(int length) {put("length", length); }

    public static void loadForBook(Book book, FindCallback<AudioFile> callback) {

        ParseQuery<AudioFile> query = ParseQuery.getQuery(AudioFile.class);
        query.whereEqualTo("book", book);
        query.orderByAscending("trackNumber");
        query.findInBackground(callback);

    }
}
