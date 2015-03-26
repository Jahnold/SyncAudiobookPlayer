package com.jahnold.syncaudiobookplayer.Models;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.widget.ProgressBar;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.App;
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
import java.util.List;

/**
 *  Book Model
 */
@ParseClassName("Book")
public class Book extends ParseObject {

    private static File sDirectory;
    private static final int ON_DEVICE_TRUE = 1;
    private static final int ON_DEVICE_FALSE = 0;
    private static final int ON_DEVICE_UNKNOWN = -1;

    private ArrayList<AudioFile> mAudioFiles = new ArrayList<>();
    private int mOnDevice = ON_DEVICE_UNKNOWN;
    private String mDevicePath;


    // getters
    public String getTitle() { return getString("title"); }
    public String getAuthor() { return getString("author"); }
    public ParseFile getCover() { return getParseFile("cover"); }
    public ParseUser getUser() { return getParseUser("user"); }
    public int getLength() { return getInt("length"); }
    public int getCurrentFile() { return getInt("currentFile"); }
    public int getCurrentFilePosition() { return getInt("currentFilePosition"); }
    public ArrayList<AudioFile> getAudioFiles() { return mAudioFiles; }
    public int getCumulativePosition() { return  getInt("cumulativePosition"); }
    public ParseObject getBookPaths() { return getParseObject("installations"); }
    //public String getSeries() { return getString("series"); }
    //public int getSeriesNumber() { return getInt("seriesNumber"); }

    // setters
    public void setTitle(String title) { put("title", title); }
    public void setAuthor(String author) { put("author", author); }
    public void setCover(ParseFile cover) { put("cover", cover); }
    public void setUser(ParseUser user) { put("user", user ); }
    public void setCurrentFile(int file) { put("currentFile", file); }
    public void setCurrentFilePosition(int position) { put("currentFilePosition", position); }
    private void setAudioFiles(ArrayList<AudioFile> files) { mAudioFiles = files; }
    public void setCumulativePosition(int position) { put("cumulativePosition", position); }
    public void setBookPaths(ArrayList<BookPath> bookPaths) { put("installations", bookPaths); }
    //public void setSeries(String series ) { put("series", series ); }
    //public void setSeriesNumber(int seriesNumber) { put("seriesNumber", seriesNumber ); }

    // useful
    public void incrementLength(int length) {
        increment("length", length);
    }
    public void incrementCurrentFile() { increment("currentFile"); }



    public static void createFromLocal(final Context context, final String directory, final ProgressDialog dialog) {

        // create a file ref from our returned directory string
        sDirectory = new File(directory);

        new AsyncTask<Void,String,Book>() {

            private MediaPlayer mMediaPlayer;
            private Book mBook;
            private String mAlbum;
            private String mAuthor;
            private ArrayList<AudioFile> mAudioFiles;
            private int mTrackNumber = 0;
            private boolean mMetaDataMediaExtracted = false;

            @Override
            protected Book doInBackground(Void... nothing) {

                // set up book, array list and media player
                mBook = new Book();
                mAudioFiles = new ArrayList<>();
                mMediaPlayer = new MediaPlayer();

                scanDirectory(sDirectory);

                // release the media player
                mMediaPlayer.release();

                mBook.setUser(ParseUser.getCurrentUser());
                mBook.setAudioFiles(mAudioFiles);
                if (mAuthor != null) mBook.setAuthor(mAuthor);
                if (mAlbum != null) mBook.setTitle(mAlbum);
                mBook.setCurrentFilePosition(0);
                mBook.setCurrentFile(0);
                mBook.setCumulativePosition(0);

                return mBook;
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
                    public void onImportBookConfirm(DialogFragment dialog, final String title, final String author) {

                        // create a book path object for this book/installation
                        final BookPath bookPath = new BookPath();
                        bookPath.setBook(book);
                        bookPath.setInstallId(Installation.id(context));
                        bookPath.setPath(directory);
                        bookPath.saveInBackground(new SaveCallback() {

                            @Override
                            public void done(ParseException e) {

                                // put the book path in an array to save to the book
                                ArrayList<BookPath> bookPaths = new ArrayList<>();
                                bookPaths.add(bookPath);

                                book.setBookPaths(bookPaths);
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


                                    }
                                });

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
                    if (Util.isFileType(file.getName(), Util.FILETYPE_AUDIO)) {

                        // create audio file, increment book length and add to the collection
                        AudioFile audioFile = createAudioFile(file);
                        mBook.incrementLength(audioFile.getLength());
                        mAudioFiles.add(audioFile);

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
                mAuthor = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                mAlbum = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

                // these details are for the audio file itself
                String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                // use a media player to get the duration
                int duration = 0;
                try {
                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                    mMediaPlayer.prepare();
                    duration = mMediaPlayer.getDuration();
                    mMediaPlayer.reset();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // increment the track number
                mTrackNumber++;

                // we need to work out the relative path of this file to the base directory sDirectory
                String absolutePath = file.getAbsolutePath();
                String basePath = sDirectory.getAbsolutePath();
                String filePath = new File(basePath).toURI().relativize(new File(absolutePath).toURI()).getPath();

                // update the progress dialog
                publishProgress(filePath);

                // create a new audio file with the data we've gathered
                return new AudioFile(
                        filePath,
                        title,
                        mTrackNumber,
                        false,
                        duration
                );

            }

        }.execute();

    }





    public static void loadAll(FindCallback<Book> callback) {

        ParseQuery<Book> query = ParseQuery.getQuery(Book.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending("updatedAt");
        query.include("installations");
        query.findInBackground(callback);

    }

    /**
     *  Is the book on the current device.
     *  The first time this is called it checks through the BookPath objects
     *  The result is cached for later use
     */
    public boolean onDevice() {

        if (mOnDevice == ON_DEVICE_UNKNOWN) {

            // get all the BookPaths for this book
            List<BookPath> bookPaths = getList("installations");

            // loop through checking if the install id matches this installation
            for (BookPath bookPath : bookPaths) {
                if (bookPath.getInstallId().equals(App.getInstallId())) {

                    // we've got a match, grab the path
                    mDevicePath = bookPath.getPath();

                    mOnDevice = ON_DEVICE_TRUE;
                    return true;
                }
            }

            // if we've got this far the book isn't on the device
            mOnDevice = ON_DEVICE_FALSE;
            return false;
        }
        else {
            return (mOnDevice == ON_DEVICE_TRUE);
        }
    }

    public String getPathForCurrentDevice() {

        if (onDevice()) {
            return mDevicePath;
        }
        else {
            return null;
        }
    }

    public void getBookPathForCurrentDevice(Context context, GetCallback<BookPath> callback) {

        ParseQuery<BookPath> query = ParseQuery.getQuery(BookPath.class);
        query.whereEqualTo("book", Book.this);
        query.whereEqualTo("installId", Installation.id(context));
        query.getFirstInBackground(callback);

    }

    /**
     *   Set up a book already in the users library on a new device
     *   Checks to make sure all files are there and then makes a new BookPath object
     */
    public void importFromLocal(final String directory, final ProgressDialog dialog) {

        new AsyncTask<Void,String,Void>() {

            private boolean mAllFilesFound = true;
            private ArrayList<String> mMissingFiles = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... params) {

                // get audio files
                AudioFile.loadForBook(Book.this, new FindCallback<AudioFile>() {
                    @Override
                    public void done(List<AudioFile> audioFiles, ParseException e) {

                        for (AudioFile audioFile : audioFiles) {

                            publishProgress(audioFile.getFilename());

                            // check that file exists on device
                            File file = new File(directory + File.separator + audioFile.getFilename());
                            if (!file.exists()) {
                                // keep track of the missing files
                                mMissingFiles.add(audioFile.getFilename());
                                mAllFilesFound = false;
                            }
                        }
                    }
                });

                return null;
            }

            @Override
            protected void onProgressUpdate(String... filename) {

                dialog.setMessage(filename[0]);

            }

            @Override
            protected void onPostExecute(Void aVoid) {

                dialog.hide();

                if (mAllFilesFound) {

                    // everything worked, make a BookPath
                    // create a book path object for this book/installation
                    final BookPath bookPath = new BookPath();
                    bookPath.setBook(Book.this);
                    bookPath.setInstallId(App.getInstallId());
                    bookPath.setPath(directory);
                    bookPath.saveInBackground(new SaveCallback() {

                        @Override
                        public void done(ParseException e) {

                            // put the book path in an array to save to the book
                            List<BookPath> currentBookPaths = getList("installations");
                            ArrayList<BookPath> newBookPaths = new ArrayList<>();
                            newBookPaths.add(bookPath);
                            newBookPaths.addAll(currentBookPaths);

                            // update and save book
                            mOnDevice = ON_DEVICE_TRUE;
                            mDevicePath = bookPath.getPath();
                            setBookPaths(newBookPaths);
                            saveInBackground();

                        }
                    });
                }

            }
        }.execute();
    }

}
