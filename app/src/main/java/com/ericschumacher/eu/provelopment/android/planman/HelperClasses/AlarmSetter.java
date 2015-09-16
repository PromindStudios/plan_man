package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by eric on 10.09.2015.
 */
public class AlarmSetter {

    public void setAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.EXTRA_ALARM_NUMBER_ONE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE, 1);
        //calendar.add(Calendar.DAY_OF_MONTH, 1);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        // First
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.EXTRA_ALARM_NUMBER_ONE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
    }
}
