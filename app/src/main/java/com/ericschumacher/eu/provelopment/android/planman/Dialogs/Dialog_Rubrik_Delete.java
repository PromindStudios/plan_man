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

import com.ericschumacher.eu.provelopment.android.planman.Fragments.RubrikListe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 01.08.2015.
 */
public class Dialog_Rubrik_Delete extends DialogFragment {


    DeleteRubrikListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mListener = (DeleteRubrikListener)getTargetFragment();

        // Get UUID
        final String uuid = getArguments().getString(RubrikListe.UUI_RUBRIK);

        // Initialize Listener and View

        final View myView = inflater.inflate(R.layout.fragment_dialog_delete_rubrik, null);

        // Initialize Widgets
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Delete_Rubrik_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Delete_Rubrik_Clear);

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

//Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Rubrik_Delete.this.getDialog().cancel();
                mListener.deleteRubrik_finally(uuid);

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Rubrik_Delete.this.getDialog().cancel();
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
            //mListener = (DeleteRubrikListener) activity;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public interface DeleteRubrikListener {
        public void deleteRubrik_finally(String uuid);
    }
}
