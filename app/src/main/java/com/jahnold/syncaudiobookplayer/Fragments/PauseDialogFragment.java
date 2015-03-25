package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.jahnold.syncaudiobookplayer.R;

/**
 *  Pause Dialog Fragment
 */
public class PauseDialogFragment extends DialogFragment {

    public interface PauseDialogListener {
        public void onPauseConfirm(int pauseType, int timerLength, boolean continueOnNudge);
    }

    public static final int PAUSE_NONE = -1;
    public static final int PAUSE_END_OF_FILE = 0;
    public static final int PAUSE_TIMER = 1;

    private PauseDialogListener mListener;

    // empty constructor
    public PauseDialogFragment() {}

    // setters
    public void setListener(PauseDialogListener listener) { mListener = listener; }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get builder and inflater
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate and set the view for the dialog then add the title
        View v = inflater.inflate(R.layout.fragment_pause_dialog, null);
        builder.setView(v);
        builder.setTitle(R.string.title_pause_dialog);

        // set up the pickers
        final NumberPicker numHours = (NumberPicker) v.findViewById(R.id.num_hours);
        final NumberPicker numMins = (NumberPicker) v.findViewById(R.id.num_mins);

        // create a formatter so that the number pickers show leading 0s
        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        };

        numHours.setMinValue(0);
        numHours.setMaxValue(9);
        numHours.setFormatter(formatter);
        numHours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);       // no typing

        numMins.setMinValue(0);
        numMins.setMaxValue(59);
        numMins.setFormatter(formatter);
        numMins.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);       // no typing

        // set up the radio buttons
        final RadioButton radEOF = (RadioButton) v.findViewById(R.id.radio_eof);
        final RadioButton radTimer = (RadioButton) v.findViewById(R.id.radio_timer);
        final RadioButton radNone = (RadioButton) v.findViewById(R.id.radio_none);

        // programmatically make the radio buttons act as a group
        radEOF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radTimer.setChecked(false);
                    radNone.setChecked(false);
                }
            }
        });
        radTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radEOF.setChecked(false);
                    radNone.setChecked(false);
                }
            }
        });
        radNone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radTimer.setChecked(false);
                    radEOF.setChecked(false);
                }
            }
        });

        // setup the buttons
        builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // get a ref to the nudge checkbox
                CheckBox nudge = (CheckBox) getDialog().findViewById(R.id.chk_nudge);

                // get the pause type and length
                int pauseType;
                if (radEOF.isChecked()) {
                    pauseType = PAUSE_END_OF_FILE;
                }
                else if (radTimer.isChecked()) {
                    pauseType = PAUSE_TIMER;
                }
                else {
                    pauseType = PAUSE_NONE;
                }

                int time = numHours.getValue() * 60 + numMins.getValue();

                // call the listener confirm method
                mListener.onPauseConfirm(pauseType, time, nudge.isChecked());

            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // user clicked cancel, do nothing
            }
        });

        return builder.create();

    }
}
