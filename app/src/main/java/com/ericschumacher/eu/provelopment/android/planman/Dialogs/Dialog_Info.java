package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 30.07.2015.
 */
public class Dialog_Info extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.fragment_dialog_info, null);
        ImageButton ibSave= (ImageButton) myView.findViewById(R.id.ibSave_info_dialog);
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Info.this.getDialog().cancel();
            }
        });


        builder.setView(myView);
        setCancelable(false);

        return builder.create();
    }
}
