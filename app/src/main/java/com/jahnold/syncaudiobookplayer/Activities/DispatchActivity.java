package com.jahnold.syncaudiobookplayer.Activities;

import com.parse.ui.ParseLoginDispatchActivity;

/**
 *  Dispatch Activity
 */
public class DispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }
}
