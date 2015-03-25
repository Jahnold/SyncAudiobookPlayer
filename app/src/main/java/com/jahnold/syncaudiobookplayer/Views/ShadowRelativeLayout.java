package com.jahnold.syncaudiobookplayer.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 *  Has a cheeky shadow
 */
public class ShadowRelativeLayout extends RelativeLayout {

    public ShadowRelativeLayout(Context context) {
        super(context);
    }

    public ShadowRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShadowRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Shadow.onDraw(this, canvas);
    }

}
