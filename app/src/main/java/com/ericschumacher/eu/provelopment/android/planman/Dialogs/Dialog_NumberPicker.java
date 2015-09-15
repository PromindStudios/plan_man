package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by eric on 22.07.2015.
 */
public class Dialog_NumberPicker extends DialogFragment implements NumberPicker.OnValueChangeListener {

    NumberPickerListener mListener;

    int year;
    int month;
    int day;

    int SelectedDays = 1;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            year = bundle.getInt(AufgabeErstellen.JAHR_TEILAUFGABE);
            month = bundle.getInt(AufgabeErstellen.MONAT_TEILAUFGABE);
            day = bundle.getInt(AufgabeErstellen.TAG_TEILAUFGABE);

            Calendar thatDay = Calendar.getInstance();
            thatDay.set(Calendar.DAY_OF_MONTH, day);
            thatDay.set(Calendar.MONTH, month);
            thatDay.set(Calendar.YEAR, year);

            Calendar today = Calendar.getInstance();

            long start = today.getTimeInMillis();
            long end = thatDay.getTimeInMillis();
            SelectedDays = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end-start));
            Log.i("Differenz: ", Integer.toString(SelectedDays));

        } else {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        mListener = (NumberPickerListener) getActivity();



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.fragment_dialog_numberpicker, null);
        NumberPicker np = (NumberPicker) myView.findViewById(R.id.nbDays);
        np.setMaxValue(100);
        np.setMinValue(0);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        np.setValue(SelectedDays);

        ImageButton ibSave = (ImageButton) myView.findViewById(R.id.ibDialog_Numberpicker_Save);
        ImageButton ibClear = (ImageButton) myView.findViewById(R.id.ibDialog_Numberpicker_Clear);

        //Set ClickListener
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.onDaysSelected(SelectedDays);
                Dialog_NumberPicker.this.getDialog().cancel();

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_NumberPicker.this.getDialog().cancel();
            }
        });

        setCancelable(false);
        builder.setView(myView);

        return builder.create();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        SelectedDays = newVal;
    }

    public interface NumberPickerListener {
        public void onDaysSelected(int days);
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
