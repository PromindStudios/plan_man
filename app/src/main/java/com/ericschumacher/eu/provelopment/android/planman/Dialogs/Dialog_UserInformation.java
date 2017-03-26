package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 26.09.2015.
 */
public class Dialog_UserInformation extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // create AlterDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle bundle = getArguments();
        int userId = bundle.getInt(Constants.USER_ID);
        int userKey = bundle.getInt(Constants.USER_KEY);

        // Initialize View
        final View myView = inflater.inflate(R.layout.fragment_dialog_user_information, null);

        // Initialize Layout Components
        ImageButton ibOk = (ImageButton) myView.findViewById(R.id.ibUserInformation_Ok);

        // Set Text Information
        TextView tvUserId = (TextView)myView.findViewById(R.id.tvUserId);
        tvUserId.setText(Integer.toString(userId));
        TextView tvUserKey = (TextView)myView.findViewById(R.id.tvUserKey);
        tvUserKey.setText(Integer.toString(userKey));

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

        //Set ClickListener
        ibOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_UserInformation.this.getDialog().cancel();

            }
        });

        // Dialog cannot be canceled
        setCancelable(false);

        // connect the layout with the AlertDialog.Builder
        builder.setView(myView);

        // create the Dialog
        Dialog d = builder.create();

        // pass back the Dialog
        return d;

    }

}

