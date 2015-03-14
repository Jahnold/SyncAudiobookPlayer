package com.jahnold.syncaudiobookplayer.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jahnold.syncaudiobookplayer.R;

/**
 *  Import Book Dialog
 */
public class ImportBookDialogFragment extends android.support.v4.app.DialogFragment {

    private ImportBookListener mListener;
    private String mBookTitle;
    private String mAuthor;

    // listener interface
    public interface ImportBookListener {
        public void onImportBookConfirm(android.support.v4.app.DialogFragment dialog, String title, String author);
    }

    // empty constructor
    public ImportBookDialogFragment() {}

    // setters
    public void setListener(ImportBookListener listener) { mListener = listener; }
    public void setBookTitle(String title) { mBookTitle = title; }
    public void setAuthor(String author) { mAuthor = author; }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get builder and inflater
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate and set the view for the dialog then add the title
        View v = inflater.inflate(R.layout.fragment_import_book_dialog,null);
        builder.setView(v);
        builder.setTitle(R.string.title_import_book_dialog);

        // get refs to the the title and author
        final EditText etBookTitle = (EditText) v.findViewById(R.id.et_book_name);
        final EditText etAuthor = (EditText) v.findViewById(R.id.et_author);

        // if any details have been passed in for title & author pre-populate the dialog
        if (mBookTitle != null) {
            etBookTitle.setText(mBookTitle);
        }
        if (mAuthor != null) {
            etAuthor.setText(mAuthor);
        }

        // setup the buttons
        builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // call the listener confirm method
                mListener.onImportBookConfirm(
                        ImportBookDialogFragment.this,
                        etBookTitle.getText().toString(),
                        etAuthor.getText().toString()
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
