package com.jahnold.syncaudiobookplayer.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Util.Util;

/**
 *  Timer Text View.  Allows a millisecond input and displays as hh:mm:ss
 */
public class TimerTextView extends TextView {


    private int mTotalTime;
    private boolean mNegative = false;

    public TimerTextView(Context context) {
        super(context);
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTotalTime(int totalTime) { mTotalTime = totalTime;  }

    public void setTime(int milliseconds) {

        if (mNegative) {
            setText("- " + Util.millisecondsToHhMmSs(mTotalTime - milliseconds));
        }
        else {
            setText(Util.millisecondsToHhMmSs(milliseconds));
        }
    }

    public void toggleNegative() {
        mNegative = !mNegative;
    }


}