package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 01.08.2015.
 */
public class Dialog_Aufgabe_Delete extends DialogFragment{

    DeleteAufgabenListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Initialize Listener and View

        final View myView = inflater.inflate(R.layout.fragment_dialog_delete_aufgabe, null);

        // Initialize Widgets
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Delete_Aufgabe_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Delete_Aufgabe_Clear);


//Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Dialog_Aufgabe_Delete.this.getDialog().cancel();
                mListener.deleteAufgabe();

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Aufgabe_Delete.this.getDialog().cancel();
            }
        });

        builder.setView(myView);
        setCancelable(false);

        Dialog d = builder.create();

        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;


    }





    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DeleteAufgabenListener) activity;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public interface DeleteAufgabenListener {
        public void deleteAufgabe();
    }
}



