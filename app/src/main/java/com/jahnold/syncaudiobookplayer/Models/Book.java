package com.jahnold.syncaudiobookplayer.Models;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Fragments.ImportBookDialogFragment;
import com.jahnold.syncaudiobookplayer.Util.Installation;
import com.jahnold.syncaudiobookplayer.Util.Util;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Book Model
 */
@ParseClassName("Book")
public class Book extends ParseObject {

    private static MediaPlayer sMediaPlayer;
    private static Book sBook;
    private static String sAlbum;
    private static String sAuthor;
    private static ArrayList<AudioFile> sAudioFiles;
    private static int sTrackNumber = 0;
    private static File sDirectory;

    private ArrayList<AudioFile> mAudioFiles = new ArrayList<>();


    // getters
    public String getTitle() { return getString("title"); }
    public String getAuthor() { return getString("author"); }
    public String getSeries() { return getString("series"); }
    public int getSeriesNumber() { return getInt("seriesNumber"); }
    public ParseFile getCover() { return getParseFile("cover"); }
    public ParseUser getUser() { return getParseUser("user"); }
    public int getLength() { return getInt("length"); }
    public int getCurrentFile() { return getInt("currentFile"); }
    public int getCurrentFilePosition() { return getInt("currentFilePosition"); }
    public ArrayList<AudioFile> getAudioFiles() { return mAudioFiles; }
    public int getCumulativePosition() { return  getInt("cumulativePosition"); }

    // setters
    public void setTitle(String title) { put("title", title); }
    public void setAuthor(String author) { put("author", author); }
    public void setSeries(String series ) { put("series", series ); }
    public void setSeriesNumber(int seriesNumber) { put("seriesNumber", seriesNumber ); }
    public void setCover(ParseFile cover) { put("cover", cover); }
    public void setUser(ParseUser user) { put("user", user ); }
    public void setCurrentFile(int file) { put("currentFile", file); }
    public void setCurrentFilePosition(int position) { put("currentFilePosition", position); }
    private void setAudioFiles(ArrayList<AudioFile> files) { mAudioFiles = files; }
    public void setCumulativePosition(int position) { put("cumulativePosition", position); }

    // useful
    public void incrementLength(int length) {
        increment("length", length);
    }
    public void incrementCurrentFile() { increment("currentFile"); }

    public static void createFromLocal(final Context context, final String directory, final ProgressDialog dialog) {

        // create a file ref from our returned directory string
        sDirectory = new File(directory);

        new AsyncTask<Void,String,Book>() {

            @Override
            protected Book doInBackground(Void... nothing) {

                // set up book, array list and media player
                sBook = new Book();
                sAudioFiles = new ArrayList<>();
                sMediaPlayer = new MediaPlayer();

                scanDirectory(sDirectory);

                // release the media player
                sMediaPlayer.release();

                sBook.setUser(ParseUser.getCurrentUser());
                sBook.setAudioFiles(sAudioFiles);
                sBook.setAuthor(sAuthor);
                sBook.setTitle(sAlbum);
                sBook.setCurrentFilePosition(0);
                sBook.setCurrentFile(0);
                sBook.setCumulativePosition(0);

                return sBook;
            }

            @Override
            protected void onPostExecute(final Book book) {

                // hide the progress dialog
                dialog.hide();

                // create the confirmation dialog
                ImportBookDialogFragment importDialog = new ImportBookDialogFragment();
                importDialog.setBookTitle(book.getTitle());
                importDialog.setAuthor(book.getAuthor());

                // create the dialog listener
                importDialog.setListener(new ImportBookDialogFragment.ImportBookListener() {
                    @Override
                    public void onImportBookConfirm(android.support.v4.app.DialogFragment dialog, String title, String author) {
                        book.setTitle(title);
                        book.setAuthor(author);
                        book.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                // associate all the audio files with the book and save them
                                for (AudioFile audioFile : book.getAudioFiles()) {
                                    audioFile.setBook(book);
                                    audioFile.saveInBackground();
                                }

                                // create a book path object for this book/installation
                                BookPath bookPath = new BookPath();
                                bookPath.setBook(book);
                                bookPath.setInstallId(Installation.id(context));
                                bookPath.setPath(directory);
                                bookPath.saveInBackground();

                            }
                        });
                    }
                });

                // show the dialog
                importDialog.show(((MainActivity) context).getSupportFragmentManager(),"ImportDialogFragment");

            }

            @Override
            protected void onProgressUpdate(String... name) {

                dialog.setMessage(name[0]);

            }

            /**
             *  Scans a directory for audio files.
             *  Will recursively scan any sub directories
             */
            private void scanDirectory(File directory) {

                File[] files = directory.listFiles();

                // make sure that there are files
                if (files == null || files.length < 1) {
                    return;
                }

                // sort alphabetically
                Arrays.sort(files);

                // loop through all the files
                for (File file : files) {

                    // is this an audio file
                    if (Util.isAudioFile(file.getName())) {

                        // update the progress dialog
                        publishProgress(file.getName());

                        // create audio file, increment book length and add to the collection
                        AudioFile audioFile = createAudioFile(file);
                        sBook.incrementLength(audioFile.getLength());
                        sAudioFiles.add(audioFile);

                    }

                    // recursively scan any sub directories
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    }

                }
            }

            /**
             *  Gathers all the details from an actual audio file (mp3, etc) and creates
             *  an AudioFile object
             */
            private AudioFile createAudioFile(File file) {

                // use meta data retriever to get file details
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(file.getAbsolutePath());

                // get these for the book
                sAuthor = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                sAlbum = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

                // these details are for the audio file itself
                String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                // use a media player to get the duration
                int duration = 0;
                try {
                    sMediaPlayer.setDataSource(file.getAbsolutePath());
                    sMediaPlayer.prepare();
                    duration = sMediaPlayer.getDuration();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // increment the track number
                sTrackNumber++;

                // we need to work out the relative path of this file to the base directory sDirectory
                String absolutePath = file.getAbsolutePath();
                String basePath = sDirectory.getAbsolutePath();
                String filePath = new File(basePath).toURI().relativize(new File(absolutePath).toURI()).getPath();

                // create a new audio file with the data we've gathered
                return new AudioFile(
                        filePath,
                        title,
                        sTrackNumber,
                        false,
                        duration
                );

            }

        }.execute();

    }





    public static void loadAll(FindCallback<Book> callback) {

        ParseQuery<Book> query = ParseQuery.getQuery(Book.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(callback);

    }

    /**
     *  Do we know the path to the book on the current device
     */
    public boolean onDevice() {
        return true;
    }

    public void getBookPathForCurrentDevice(Context context, GetCallback<BookPath> callback) {

        ParseQuery<BookPath> query = ParseQuery.getQuery(BookPath.class);
        query.whereEqualTo("book", Book.this);
        query.whereEqualTo("installId", Installation.id(context));
        query.getFirstInBackground(callback);

    }



}
