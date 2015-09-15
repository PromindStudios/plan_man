package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ericschumacher.eu.provelopment.android.planman.Fragments.RubrikListe;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 12.07.2015.
 */
public class Dialog_Rubrik_Edit extends DialogFragment {



    EditDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());



        String rubrik_title = getArguments().getString(RubrikListe.EDIT_RUBRIK_KEY);
        final String id = getArguments().getString(RubrikListe.ID_EDIT_RUBRIK_KEY);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Initialize Listener and View
        mListener = (EditDialogListener)getTargetFragment();
        final View myView = inflater.inflate(R.layout.fragment_dialog_edit_rubrik, null);

        // Initialize Widgets
        final EditText etEingabe = (EditText)myView.findViewById(R.id.etDialog_Edit_Rubrik);
        ImageButton ibSave= (ImageButton) myView.findViewById(R.id.ibDialog_Edit_Rubrik_Save);
        ImageButton ibClear= (ImageButton) myView.findViewById(R.id.ibDialog_Edit_Rubrik_Clear);

        //Show Keyboard and set Title
        etEingabe.setText(rubrik_title);
        etEingabe.requestFocus();



        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Eingabe;
                if (etEingabe != null) {
                    Eingabe = etEingabe.getText().toString();
                    mListener.onDialogPositiveClick(Eingabe, id);
                    Dialog_Rubrik_Edit.this.getDialog().cancel();
                }
            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Rubrik_Edit.this.getDialog().cancel();
            }
        });

        builder.setView(myView);
        setCancelable(false);

        Dialog d = builder.create();

        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;

    }

    public interface EditDialogListener {
        public void onDialogPositiveClick(String text, String id);
        public void onDialogNegativeClick(DialogFragment dialog);
    }


}
