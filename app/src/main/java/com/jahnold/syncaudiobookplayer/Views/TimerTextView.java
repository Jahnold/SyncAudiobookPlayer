package com.jahnold.syncaudiobookplayer.Views;

import android.content.Context;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Util.Util;

/**
 *  Timer Text View.  Allows a millisecond input and displays as hh:mm:ss
 */
public class TimerTextView extends TextView {


    private int totalTime;

    public TimerTextView(Context context) {
        super(context);
    }

    public void setTotalTime(int totalTime) { this.totalTime = totalTime;  }
    public void setTime(int milliseconds) { this.setText(Util.millisecondsToHhMmSs(milliseconds)); }


}