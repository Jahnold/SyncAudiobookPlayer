package com.jahnold.syncaudiobookplayer.Util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 *  Util Class
 */
public class Util {

    public static String millisecondsToHhMmSs(int millis) {

        return  String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        );

    }

    public static boolean isAudioFile(String filename) {

        ArrayList<String> audioFileTypes = new ArrayList<>();

        // set up known audio file types
        audioFileTypes.add("MP3");
        audioFileTypes.add("M4A");
        audioFileTypes.add("WAV");
        audioFileTypes.add("AMR");
        audioFileTypes.add("AWB");
        audioFileTypes.add("WMA");
        audioFileTypes.add("OGG");
        audioFileTypes.add("AAC");
        audioFileTypes.add("MKA");
        audioFileTypes.add("FLAC");

        // get the file type from after the last period
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String fileType = filename.substring(lastDot + 1).toUpperCase(Locale.ROOT);

        // compare found file type to known audio file types
        for (String audioFileType : audioFileTypes) {

            if (fileType.equals(audioFileType)) return true;
        }

        // if we got this far it's not a known audio file type
        return false;
    }
}
