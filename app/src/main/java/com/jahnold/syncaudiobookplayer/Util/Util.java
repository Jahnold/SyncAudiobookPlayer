package com.jahnold.syncaudiobookplayer.Util;

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
}
