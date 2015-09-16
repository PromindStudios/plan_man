package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by eric on 10.09.2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmSetter alarmSetter = new AlarmSetter();

        SharedPreferences settings = context.getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        Boolean alarmActivated = settings.getBoolean(Constants.SP_ALARM_SET_BY_USER, true);

        if (alarmActivated) {
            alarmSetter.cancelAlarm(context);

        } else {
            alarmSetter.setAlarm(context);

        }
    }
}
