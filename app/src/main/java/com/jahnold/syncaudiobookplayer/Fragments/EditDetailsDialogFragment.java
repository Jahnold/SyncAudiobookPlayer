package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jahnold.syncaudiobookplayer.R;

/**
 * Edit Details Dialog
 */
public class EditDetailsDialogFragment extends DialogFragment {

    private EditDetailsListener mListener;
    private String mCurrentDetail;
    private String mDetailName;

    // listener interface
    public interface EditDetailsListener {
        public void onEditDetailsConfirm(DialogFragment dialog, String detail);
    }

    // empty constructor
    public  EditDetailsDialogFragment() {}

    // setters
    public void setListener(EditDetailsListener listener) { mListener = listener; }
    public void setCurrentDetail(String currentDetail) { mCurrentDetail = currentDetail; }
    public void setDetailName(String detailName) { mDetailName = detailName; }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get builder and inflater
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate and set the view for the dialog then add the title
        View v = inflater.inflate(R.layout.fragment_edit_details_dialog,null);
        builder.setView(v);

        // get ref to the edit text
        final EditText etDetail = (EditText) v.findViewById(R.id.et_detail);

        // if the detail name / current value have been set the add them to the dialog
        if (mCurrentDetail != null) {
            etDetail.setText(mCurrentDetail);
        }
        if (mDetailName != null) {
            builder.setTitle("Edit " + mDetailName);
        }

        // setup the buttons
        builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // call the listener confirm method
                mListener.onEditDetailsConfirm(
                        EditDetailsDialogFragment.this,
                        etDetail.getText().toString()
                );

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
