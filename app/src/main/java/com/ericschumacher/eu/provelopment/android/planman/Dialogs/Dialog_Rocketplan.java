package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 19.09.2016.
 */
public class Dialog_Rocketplan extends DialogFragment {


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Initialize Listener and View

            final View myView = inflater.inflate(R.layout.fragment_dialog_rocketplan, null);

            // Initialize Widgets
            TextView tvCheck = (TextView) myView.findViewById(R.id.tvCheck);
            TextView tvLater = (TextView) myView.findViewById(R.id.tvLater);

            // set Color of Header
            Log.i("Aufgabe_Check: ", "checked!");
            ColorTheme colorTheme = new ColorTheme(getActivity());
            TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
            tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

//Set ClickListener
            tvCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String appPackageName = "kalender.notes.calendar.notepad.aufgabenplaner.provelopment.rockitplan";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("market://details?id=" + appPackageName))));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+appPackageName)));
                    }
                    Dialog_Rocketplan.this.getDialog().cancel();
                }
            });

            tvLater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog_Rocketplan.this.getDialog().cancel();
                }
            });

            builder.setView(myView);
            setCancelable(false);

            Dialog d = builder.create();

            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            return d;


        }
}
