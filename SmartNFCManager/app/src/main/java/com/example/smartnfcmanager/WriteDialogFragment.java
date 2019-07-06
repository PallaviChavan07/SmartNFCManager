package com.example.smartnfcmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

public class WriteDialogFragment extends DialogFragment {

    String WRITE_DIALOG_TAG = "write_progress_dialog";
    public static final String TAG = WriteDialogFragment.class.getSimpleName();
    public static WriteDialogFragment newInstance(){
        return new WriteDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.write_dialog_title)
                            .setMessage( R.string.write_dialog_message )
                            .setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.isWrite = false;
                                    dialogInterface.cancel();
                                }
                            } )

                            .create();



    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss( dialog );
    }
}
