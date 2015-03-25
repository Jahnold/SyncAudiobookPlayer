package com.jahnold.syncaudiobookplayer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Models.AudioFile;
import com.jahnold.syncaudiobookplayer.Models.Book;
import com.jahnold.syncaudiobookplayer.R;
import com.jahnold.syncaudiobookplayer.Views.TimerTextView;

import java.util.ArrayList;

/**
 *  File Adapter
 */
public class FileAdapter extends ArrayAdapter<AudioFile> {

    // working copy of the feed
    private ArrayList<AudioFile> mFeed;

    // constructor
    public FileAdapter(Context context, int textViewResourceId, ArrayList<AudioFile> items) {

        super(context, textViewResourceId, items);
        this.mFeed = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // check whether the view needs inflating (it may be recycled)
        if (convertView == null) {

            // inflate the view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_file, null);

        }

        // get the AudioFile at [position] from the array list
        final AudioFile item = mFeed.get(position);

        // get refs to the interface controls
        TextView txtFilename = (TextView) convertView.findViewById(R.id.txt_filename);
        TextView txtTrack = (TextView) convertView.findViewById(R.id.txt_track);
        TimerTextView txtLength = (TimerTextView) convertView.findViewById(R.id.txt_length);
        ImageButton btnMenu = (ImageButton) convertView.findViewById(R.id.btn_menu);

        if (item != null) {

            // transfer details from the audio file to the view
            txtFilename.setText(item.getFilename());
            txtTrack.setText(String.valueOf(item.getTrackNumber()));
            txtLength.setTime(item.getLength());

        }

        // create the popup menu
        final PopupMenu popupMenu = new PopupMenu(getContext(), btnMenu);
        popupMenu.getMenu().add(Menu.NONE, 0, Menu.NONE, getContext().getString(R.string.menu_delete));

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
        btnMenu.setFocusable(false);

        return convertView;

    }
}
