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
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 01.08.2015.
 */
public class Dialog_Aufgabe_Check extends DialogFragment {

    DialogAufgabeDelete_Listener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Get UUID
        final Bundle bundle = getArguments();
        final String uuid = bundle.getString(Constants.ID_AUFGABE);
        //final Boolean overview = bundle.getBoolean(Main.OVERVIEW);

        // Set up Listener
        mListener = (DialogAufgabeDelete_Listener)getTargetFragment();

        // Initialize Listener and View
        final View myView = inflater.inflate(R.layout.fragment_dialog_check_aufgabe, null);

        // Initialize Widgets
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Check_Aufgabe_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Check_Aufgabe_Clear);

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));


//Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Aufgabe_Check.this.getDialog().cancel();
                mListener.onAufgabenItemDelete2(uuid);

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAufgabenRefresh();
                Dialog_Aufgabe_Check.this.getDialog().cancel();
            }
        });

        builder.setView(myView);
        setCancelable(false);

        Dialog d = builder.create();



        return d;


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CheckAufgabenListener) activity;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        */
    }


    public interface CheckAufgabenListener {
        public void checkAufgabe(String uuid, boolean overview);
        public void checkAufgabeRefresh();
    }

    public interface DialogAufgabeDelete_Listener {
        public void onAufgabenItemDelete2(String uuid);
        public void onAufgabenRefresh();
    }
}
