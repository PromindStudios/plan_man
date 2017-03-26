package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 21.10.2015.
 */
public class Dialog_Get_Premium_Ad extends DialogFragment {

    GetPremium_AdListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle bundle = getArguments();
        String title = bundle.getString(Constants.TITLE);
        String description = bundle.getString(Constants.DESCRIPTION);

        mListener = (GetPremium_AdListener)getTargetFragment();

        // Initialize Listener and View

        final View myView = inflater.inflate(R.layout.fragment_dialog_get_premium, null);

        // Initialize Widgets
        TextView ibSave = (TextView) myView.findViewById(R.id.ibDialog_Premium_positive);
        TextView ibClear = (TextView) myView.findViewById(R.id.ibDialog_Premium_negative);
        TextView tvTitle = (TextView)myView.findViewById(R.id.tvDialog_title);
        TextView tvDescription = (TextView)myView.findViewById(R.id.tvDialog_description);
        tvTitle.setText(title);
        tvDescription.setText(description);


        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.tvDialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

//Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Get_Premium_Ad.this.getDialog().cancel();
                mListener.onGetPremiumAdSelected();

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Get_Premium_Ad.this.getDialog().cancel();
            }
        });

        builder.setView(myView);
        setCancelable(false);

        Dialog d = builder.create();

        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;

    }



    public interface GetPremium_AdListener {
        public void onGetPremiumAdSelected();
    }
}



