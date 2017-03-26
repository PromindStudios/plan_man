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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 27.09.2015.
 */
public class Dialog_Connect extends DialogFragment {

    DialogConnect_Listener mListener;
    String mRubrik_UUID;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // create AlterDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // get uuid
        mRubrik_UUID = getArguments().getString(Constants.RUBRIK_UUID);

        // Set up Listener
        mListener = (DialogConnect_Listener)getTargetFragment();

        // Initialize View
        final View myView = inflater.inflate(R.layout.fragment_dialog_connect, null);

        // Initialize Layout Components
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Connect_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Connect_Clear);
        final EditText etId = (EditText)myView.findViewById(R.id.etDialog_EnterId);
        final EditText etKey = (EditText)myView.findViewById(R.id.etDialog_EnterKey);

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etId.getText().toString().equals("") && !etKey.getText().toString().equals("")) {
                    try {
                        mListener.onConnect(Integer.parseInt(etId.getText().toString()), Integer.parseInt(etKey.getText().toString()), mRubrik_UUID);
                        Dialog_Connect.this.getDialog().cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getActivity().getString(R.string.invalid), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.enter_Id_and_key), Toast.LENGTH_LONG).show();
                }
            }
        });
        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Connect.this.getDialog().cancel();
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
    public interface DialogConnect_Listener {
        public void onConnect(int id_otherUser, int key_otherUser, String uuid);
    }
}
