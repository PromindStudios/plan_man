package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 30.09.2015.
 */
public class Dialog_ColorTheme extends DialogFragment {

    DialogColorTheme_Listener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // create AlterDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set up Listener
        mListener = (DialogColorTheme_Listener)getTargetFragment();

        // Initialize View
        final View myView = inflater.inflate(R.layout.fragment_dialog_colortheme, null);

        // Initialize Layout Components
        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_ColorTheme_Save);
        RadioGroup rgColorTheme = (RadioGroup)myView.findViewById(R.id.rgColorTheme);
        RadioButton rbPrimelist = (RadioButton)myView.findViewById(R.id.rbColorPrimelist);
        RadioButton rbOcean = (RadioButton)myView.findViewById(R.id.rbColorOcean);
        RadioButton rbRoyal = (RadioButton)myView.findViewById(R.id.rbColorRoyal);
        RadioButton rbExecutive = (RadioButton)myView.findViewById(R.id.rbColorExecutive);
        RadioButton rbCandy = (RadioButton)myView.findViewById(R.id.rbColorCandy);
        RadioButton rbGreenAlley = (RadioButton)myView.findViewById(R.id.rbColorGreenAlley);

        // get SharedPreferences
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        final SharedPreferences.Editor editor = settings.edit();
        int currentColorTheme = settings.getInt(Constants.SP_COLOR_THEME, 0);

        // set Checked Button
        switch (currentColorTheme) {
            case 0:
                rgColorTheme.check(R.id.rbColorPrimelist);
                break;
            case 1:
                rgColorTheme.check(R.id.rbColorExecutive);
                break;
            case 2:
                rgColorTheme.check(R.id.rbColorOcean);
                break;
            case 3:
                rgColorTheme.check(R.id.rbColorRoyal);
                break;
            case 4:
                rgColorTheme.check(R.id.rbColorCandy);
                break;
            case 5:
                rgColorTheme.check(R.id.rbColorGreenAlley);
                break;
        }

        // set Color of Header
        Log.i("Aufgabe_Check: ", "checked!");
        ColorTheme colorTheme = new ColorTheme(getActivity());
        TextView tvHeader = (TextView)myView.findViewById(R.id.dialog_title);
        tvHeader.setBackgroundColor(getActivity().getResources().getColor(colorTheme.getColorPrimary()));

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            rbPrimelist.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_Primelist)));
            rbExecutive.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_Executive)));
            rbOcean.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_Ocean)));
            rbRoyal.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_Royal)));
            rbCandy.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_Candy)));
            rbGreenAlley.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary_GreenAlley)));
        } else {
            int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
            rbPrimelist.setButtonDrawable(id);
            rbExecutive.setButtonDrawable(id);
            rbOcean.setButtonDrawable(id);
            rbRoyal.setButtonDrawable(id);
            rbCandy.setButtonDrawable(id);
            rbGreenAlley.setButtonDrawable(id);

            //myViewHolder.cbCheckAufgabe_deaktiviert.setButtonDrawable(id);
        }

        // Set onCheckedChangedListener
        rgColorTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbColorPrimelist:
                        editor.putInt(Constants.SP_COLOR_THEME, 0);
                        editor.commit();
                        break;
                    case R.id.rbColorExecutive:
                        editor.putInt(Constants.SP_COLOR_THEME, 1);
                        editor.commit();
                        break;
                    case R.id.rbColorOcean:
                        editor.putInt(Constants.SP_COLOR_THEME, 2);
                        editor.commit();
                        break;
                    case R.id.rbColorRoyal:
                        editor.putInt(Constants.SP_COLOR_THEME, 3);
                        editor.commit();
                        break;
                    case R.id.rbColorCandy:
                        editor.putInt(Constants.SP_COLOR_THEME, 4);
                        editor.commit();
                        break;
                    case R.id.rbColorGreenAlley:
                        editor.putInt(Constants.SP_COLOR_THEME, 5);
                        editor.commit();
                        break;
                }
            }

        });





        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRestart();
                Dialog_ColorTheme.this.getDialog().cancel();
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
    public interface DialogColorTheme_Listener {
        public void onRestart();
    }
}
