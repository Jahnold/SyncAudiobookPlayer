package com.jahnold.syncaudiobookplayer.Models;

import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

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

    public static void createFromLocal(String directory) {

        // create a file ref from our returned directory string
        File bookDir = new File(directory);
        File[] listFiles = bookDir.listFiles();

        // make sure that there are files
        if (listFiles != null && listFiles.length > 0) {

            // create a book and set some defaults
            final Book book = new Book();
            //book.setUser(ParseUser.getCurrentUser());
            book.setCurrentPosition(0);

            final ArrayList<AudioFile> audioFiles = new ArrayList<>();

            // loop through all the files
            for (File file : listFiles) {

                String filePath = file.getAbsolutePath();

                //TODO
                // work out the proper way of checking for audio files
                if (filePath.substring(filePath.lastIndexOf('.') + 1).equals("mp3")) {

                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(filePath);


                    try {

                        final String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        final int trackNumber = Integer.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
                        final String fileName = file.getName();

                        // use a media player to get the duration
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(filePath);
//                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                            @Override
//                            public void onPrepared(MediaPlayer mp) {
//
//                                // create a new audio file with the data we've gathered
//                                AudioFile audioFile = new AudioFile(
//                                        fileName,
//                                        title,
//                                        trackNumber,
//                                        false,
//                                        mp.getDuration()
//                                );
//
//                                book.incrementLength(mp.getDuration());
//
//                                // add it to the array list
//                                audioFiles.add(audioFile);
//                            }
//                        });

                        // call prepare sync because the whole
                        // operation is happening on a separate thread
                        mediaPlayer.prepare();

                        // create a new audio file with the data we've gathered
                        AudioFile audioFile = new AudioFile(
                                fileName,
                                title,
                                trackNumber,
                                false,
                                mediaPlayer.getDuration()
                        );

                        book.incrementLength(mediaPlayer.getDuration());

                        // add it to the array list
                        audioFiles.add(audioFile);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            book.setAudioFiles(audioFiles);
        }

        // escape the directory string (slashes and all that)
//        String escapedDirectory = DatabaseUtils.sqlEscapeString(directory + "/");
//
//        Cursor mediaCursor = getContentResolver().query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                new String[] { "*" },
//                MediaStore.Audio.Media.DATA, //+ " = " + escapedDirectory,
//                null,
//                MediaStore.Audio.Media.TRACK
//        );
//
//        if (mediaCursor == null) {
//            return;
//        }
//
//        if (mediaCursor.moveToFirst()) {
//
//            String path = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//            String title = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//            String album = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//            String artist = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//            int album_id = mediaCursor.getInt(mediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//            int trackid = mediaCursor.getInt(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
//            Long duration = mediaCursor.getLong(mediaCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//
//            Log.d("does it", "work?");
//        }
//
//        mediaCursor.close();

    }
}
