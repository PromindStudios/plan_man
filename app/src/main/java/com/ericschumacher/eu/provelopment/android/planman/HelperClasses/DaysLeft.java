package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by eric on 07.09.2015.
 */
public class DaysLeft {

    public DaysLeft() {

    }

    public int getDaysLeft(Calendar deadline) {
        Calendar today = Calendar.getInstance();
        long daysBetween = 0;

        while (today.before(deadline)) {
            today.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }

        return (int)daysBetween;
    }
}
