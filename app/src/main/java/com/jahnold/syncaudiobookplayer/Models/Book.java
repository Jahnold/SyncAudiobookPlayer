package com.jahnold.syncaudiobookplayer.Models;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.Fragments.ImportBookDialogFragment;
import com.jahnold.syncaudiobookplayer.Util.Installation;
import com.jahnold.syncaudiobookplayer.Util.MediaFile;
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

/**
 *  Book Model
 */
@ParseClassName("Book")
public class Book extends ParseObject {

    private ArrayList<AudioFile> mAudioFiles = new ArrayList<>();

    // getters
    public String getTitle() { return getString("title"); }
    public String getAuthor() { return getString("author"); }
    public String getSeries() { return getString("series"); }
    public int getSeriesNumber() { return getInt("seriesNumber"); }
    public ParseFile getCover() { return getParseFile("cover"); }
    public ParseUser getUser() { return getParseUser("user"); }
    public int getLength() { return getInt("length"); }
    public AudioFile getCurrentFile() { return (AudioFile) getParseObject("currentFile"); }
    public int getCurrentPosition() { return getInt("currentPosition"); }
    public int getCurrentFilePosition() { return getInt("currentFilePosition"); }
    public ArrayList<AudioFile> getAudioFiles() { return mAudioFiles; }

    // setters
    public void setTitle(String title) { put("title", title); }
    public void setAuthor(String author) { put("author", author); }
    public void setSeries(String series ) { put("series", series ); }
    public void setSeriesNumber(int seriesNumber) { put("seriesNumber", seriesNumber ); }
    public void setCover(ParseFile cover) { put("cover", cover); }
    public void setUser(ParseUser user) { put("user", user ); }
    public void setLength(int length) { put("length", length ); }
    public void setCurrentFile(AudioFile file) { put("currentFile", file); }
    public void setCurrentPosition(int position) { put("currentPosition", position); }
    public void setCurrentFilePosition(int position) { put("currentFilePosition", position); }
    private void setAudioFiles(ArrayList<AudioFile> files) { mAudioFiles = files; }

    // useful
    public void incrementLength(int length) {
        increment("length", length);
    }

    public static void createFromLocal(final Context context, final String directory, final ProgressDialog dialog) {

        // create a file ref from our returned directory string
        File bookDir = new File(directory);
        File[] files = bookDir.listFiles();

        // make sure that there are files
        if (files == null || files.length < 1) {
            return;
        }

        new AsyncTask<File,String,Book>() {

            @Override
            protected Book doInBackground(File... files) {

                // create a book and set some defaults
                final Book book = new Book();
                book.setUser(ParseUser.getCurrentUser());
                book.setCurrentPosition(0);

                final ArrayList<AudioFile> audioFiles = new ArrayList<>();
                String title = "";
                String author = "";

                // loop through all the files
                for (File file : files) {

                    String filePath = file.getAbsolutePath();


                    // use the MediaFile helper class to determine whether the file is audio
                    // if not skip and move onto next file
                    if (!MediaFile.isAudioFileType(MediaFile.getFileType(filePath).fileType)) {
                        continue;
                    }

                    // update the progress dialog
                    publishProgress(file.getName());

                    // use meta data retriever to get title & track number
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(filePath);
                    title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    author = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    int trackNumber = Integer.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));

                    // filename = easy
                    String fileName = file.getName();

                    int duration = 0;
                    try {

                        // use a media player to get the duration
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(filePath);
                        mediaPlayer.prepare();
                        duration = mediaPlayer.getDuration();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // create a new audio file with the data we've gathered
                    AudioFile audioFile = new AudioFile(
                            fileName,
                            title,
                            trackNumber,
                            false,
                            duration
                    );

                    // add the file duration to the overall length of the book
                    book.incrementLength(duration);

                    // add file to the array list
                    audioFiles.add(audioFile);
                    book.setAudioFiles(audioFiles);

                }

                book.setAuthor(author);
                book.setTitle(title);

                return book;
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

        }.execute(files);

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
