package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.UUID;

/**
 * Created by eric on 22.07.2015.
 */
public class Dialog_Teilaufgabe_Edit extends DialogFragment{

    private UUID mId;
    private EditText mEingabe;
    private String mTitle;
    DialogTeilaufgabeEditListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        mId = UUID.fromString(bundle.getString(AufgabeErstellen.UUID_TEILAUFGABE));
        mTitle = bundle.getString(AufgabeErstellen.TITLE_TEILAUFGABE);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Initialize Listener and View
        final View myView = inflater.inflate(R.layout.fragment_dialog_edit_teilaufgabe, null);

        // Initialize Widgets
        final EditText etEingabe = (EditText)myView.findViewById(R.id.etDialog_Edit_Teilaufgabe);
        ImageButton ibSave= (ImageButton) myView.findViewById(R.id.ibDialog_Edit_Teilaufgabe_Save);
        ImageButton ibClear= (ImageButton) myView.findViewById(R.id.ibDialog_Edit_Teilaufgabe_Clear);

        //Show Keyboard and set Title
        etEingabe.setText(mTitle);
        etEingabe.requestFocus();

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Eingabe;
                if (etEingabe != null) {
                    Eingabe = etEingabe.getText().toString();
                    mListener.onHandleTeilaufgabeEditName(mId, Eingabe);
                    Dialog_Teilaufgabe_Edit.this.getDialog().cancel();
                }
            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Teilaufgabe_Edit.this.getDialog().cancel();
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
            mListener = (DialogTeilaufgabeEditListener) activity;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public interface DialogTeilaufgabeEditListener {
        public void onHandleTeilaufgabeEditName(UUID uuid, String eingabe);
    }
}
