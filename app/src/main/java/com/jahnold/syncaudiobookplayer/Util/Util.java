package com.jahnold.syncaudiobookplayer.Util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 *  Util Class
 */
public class Util {

    public static final int FILETYPE_AUDIO = 0;
    public static final int FILETYPE_IMAGE = 1;

    public static String millisecondsToHhMmSs(int millis) {

        return  String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        );

    }

    public static boolean isFileType(String filename, int fileType) {

        ArrayList<String> fileTypeExtensions = new ArrayList<>();

        // set up known file types
        switch (fileType) {
            case FILETYPE_AUDIO:
                fileTypeExtensions.add("MP3");
                fileTypeExtensions.add("M4A");
                fileTypeExtensions.add("WAV");
                fileTypeExtensions.add("AMR");
                fileTypeExtensions.add("AWB");
                fileTypeExtensions.add("WMA");
                fileTypeExtensions.add("OGG");
                fileTypeExtensions.add("AAC");
                fileTypeExtensions.add("MKA");
                fileTypeExtensions.add("FLAC");
                break;
            case FILETYPE_IMAGE:
                fileTypeExtensions.add("JPEG");
                fileTypeExtensions.add("JPG");
                fileTypeExtensions.add("GIF");
                fileTypeExtensions.add("PNG");
                fileTypeExtensions.add("BMP");
                fileTypeExtensions.add("WBMP");
                fileTypeExtensions.add("WEBP");
                break;
        }


        // get the file extension from after the last period
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String fileExtension = filename.substring(lastDot + 1).toUpperCase(Locale.ROOT);

        // compare found file type to known audio file types
        for (String fileTypeExtension : fileTypeExtensions) {

            if (fileExtension.equals(fileTypeExtension)) return true;
        }

        // if we got this far it's not a match
        return false;
    }

    public static int colorFromObjectId(String objectId) {

        int hash = objectId.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        return Color.rgb(r,g,b);
    }
}
