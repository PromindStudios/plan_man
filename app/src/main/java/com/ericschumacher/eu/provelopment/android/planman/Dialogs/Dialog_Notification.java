package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 10.09.2015.
 */

public class Dialog_Notification extends DialogFragment {

    TextView tvTitle;
    TextView tvContent;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.fragment_dialog_info, null);
        tvTitle = (TextView) myView.findViewById(R.id.tvDialog_Info_Title);
        tvContent = (TextView) myView.findViewById(R.id.tvDialog_Info_Content);
        tvTitle.setText(getActivity().getString(R.string.Dialog_Info_Titlxe));
        tvContent.setText(getActivity().getString(R.string.Dialog_Info_Content));
        ImageButton ibSave= (ImageButton) myView.findViewById(R.id.ibSave_info_dialog);
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Notification.this.getDialog().cancel();
            }
        });


        builder.setView(myView);
        setCancelable(false);

        return builder.create();
    }
}
