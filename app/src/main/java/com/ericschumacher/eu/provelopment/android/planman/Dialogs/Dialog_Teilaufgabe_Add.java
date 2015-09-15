package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.UUID;

/**
 * Created by eric on 21.07.2015.
 */
public class Dialog_Teilaufgabe_Add extends DialogFragment {

    EditText mEingabe;
    DialogTeilaufgabeListener mListener;
    UUID mId;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        Bundle bundle = getArguments();
        mId = UUID.fromString(bundle.getString(AufgabeErstellen.UUID_TEILAUFGABE));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Initialize Listener and View

        final View myView = inflater.inflate(R.layout.fragment_dialog_add_teilaufgabe, null);

        // Initialize Widgets
        final EditText etEingabe = (EditText) myView.findViewById(R.id.etDialog_add_Teilaufgabe);
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Add_Teilaufgabe_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Add_Teilaufgabe_Clear);
        ImageButton ibAdd = (ImageButton) myView.findViewById(R.id.ibDialog_Add_Teilaufgabe_Add);

        //Show Keyboard and set Title
        etEingabe.requestFocus();

        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Eingabe;
                if (etEingabe != null) {
                    Eingabe = etEingabe.getText().toString();
                    mListener.onAttachTeilaufgabe (Eingabe, mId);
                    Dialog_Teilaufgabe_Add.this.getDialog().cancel();
                }
            }
        });

        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Eingabe;
                if (etEingabe != null) {
                    Eingabe = etEingabe.getText().toString();
                    mListener.onAttachTeilaufgabeAndAdd(Eingabe, mId);
                    Dialog_Teilaufgabe_Add.this.getDialog().cancel();
                }
            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteTeilaufgabe(mId);
                Dialog_Teilaufgabe_Add.this.getDialog().cancel();
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
            mListener = (DialogTeilaufgabeListener) activity;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public interface DialogTeilaufgabeListener {
        public void onAttachTeilaufgabe(String eingabe, UUID uuid);
        public void onAttachTeilaufgabeAndAdd(String eingabe, UUID uuid);
        public void onDeleteTeilaufgabe(UUID uuid);
    }

}
