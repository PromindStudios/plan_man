package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 05.11.2015.
 */
public class Dialog_TestVersion_RunningOut extends DialogFragment {

    Dialog_TestVersionRunningOut_Listener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // create AlterDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Get Value from Bundle
        final Bundle bundle = getArguments();
        final String descreption = bundle.getString(Constants.DESCRIPTION);

        // Set up Listener
        mListener = (Dialog_TestVersionRunningOut_Listener)getTargetFragment();

        // Initialize View
        final View myView = inflater.inflate(R.layout.fragment_dialog_info, null);

        // Initialize Layout Components
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibSave_info_dialog);
        TextView  tvTitle = (TextView)myView.findViewById(R.id.dialog_title);
        TextView tvDescription = (TextView)myView.findViewById(R.id.tvDialog_Info_Content);
        tvDescription.setText(descreption);
        tvTitle.setText(getActivity().getString(R.string.test_version_is_running_title));

        // Set Color of Header
        ColorTheme colorTheme = new ColorTheme(getActivity());
        tvTitle.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));




        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_TestVersion_RunningOut.this.getDialog().cancel();
                mListener.onCreateAufgabe();

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

    // create interface
    public interface Dialog_TestVersionRunningOut_Listener {
        public void onCreateAufgabe();
    }
}
