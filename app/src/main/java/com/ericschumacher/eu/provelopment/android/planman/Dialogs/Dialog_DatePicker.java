package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 16.07.2015.
 */
public class Dialog_DatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    DatePickerListener mListener;

    int year;
    int month;
    int day;
    String uuid;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


            Bundle bundle = getArguments();
            year = bundle.getInt(Constants.JAHR_AUFGABE);
            month = bundle.getInt(Constants.MONAT_AUFGABE);
            day = bundle.getInt(Constants.TAG_AUFGABE);
            uuid = bundle.getString(Constants.ID_AUFGABE);


        if (getTargetFragment() != null) {
            mListener = (DatePickerListener)getTargetFragment();
        } else {
            mListener = (DatePickerListener)getActivity();
        }



        DatePickerDialog datePickerDialog;
        if (android.os.Build.VERSION.SDK_INT >= 23)  {
            datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert, this, year, month, day);
        } else {
            datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        }
        // Create a new instance of DatePickerDialog and return it

        //datePickerDialog.setTitle(getActivity().getString(R.string.title_datepicker));

        DialogInterface.OnClickListener listener_positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();

            }
        };
        /*
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Übernehmen", listener_positive);

        DialogInterface.OnClickListener listener_negative = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dismiss();
            }
        };
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Abrechen", listener_negative);

*/
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mListener.onDateSelected(year, month, day, uuid);

        // Do something with the date chosen by the user
    }

    public interface DatePickerListener {
        public void onDateSelected (int year, int month, int day, String uuid);
    }
}




/* !!!!!!!!!!!!1

1. DatePicker so einrichten, dass Datum übernommen wird, in Aufgabe.Datum gespeichert wird und im AufgabeErstellen
angezeigt wird
2. Number Picker einrichten: http://stackoverflow.com/questions/17805040/how-to-create-a-number-picker-dialog
http://developer.android.com/reference/android/widget/NumberPicker.html
3. Teil-Aufgaben mit Recyclerview einrichten

 */

