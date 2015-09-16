package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;

/**
 * Created by eric on 05.09.2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    // Field Variables
    private String mTitle;
    private String mSubtitle;
    private String[] mDueTasks_String;
    private ArrayList<Aufgabe> mDueTasks_Array;
    private Context mContext;
    NotificationCompat.InboxStyle mInboxStyle;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        mTitle = context.getString(R.string.Notification_Title);


        String action = intent.getAction();
        if (Constants.EXTRA_ALARM_NUMBER_ONE.equals(action)) {

            mDueTasks_Array = getDueTasks_Array();
            mInboxStyle = new NotificationCompat.InboxStyle();

            if (mDueTasks_Array != null && mDueTasks_Array.size() > 0) {
                int sizeTasks = mDueTasks_Array.size();
                if (sizeTasks == 1) {
                    mSubtitle = Integer.toString(sizeTasks)+" "+context.getString(R.string.dueTask_one);
                    mInboxStyle.addLine(mDueTasks_Array.get(0).getRubrikName()+": "+mDueTasks_Array.get(0).getTitle());
                    mInboxStyle.setSummaryText(mSubtitle);
                } else {
                    mSubtitle = Integer.toString(sizeTasks)+" "+context.getString(R.string.dueTask_more_than_one);
                    for (int i = 0; i < sizeTasks; i++) {
                        mInboxStyle.addLine(mDueTasks_Array.get(i).getRubrikName()+": "+mDueTasks_Array.get(i).getTitle());
                        if (i == 4) {
                            break;
                        }
                    }
                    mInboxStyle.setSummaryText(mSubtitle);
                }
            } else {
                mSubtitle = context.getString(R.string.dueTask_none);
                mInboxStyle.setSummaryText(mSubtitle);
            }


            Intent resultIntent = new Intent(context, Main.class);
            resultIntent.putExtra(Constants.NOTIFACTION_INTENT, true);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Main.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

            Notification notification = mBuilder
                    .setSmallIcon(R.drawable.ic_done)
                    .setAutoCancel(true)
                    .setContentTitle(mTitle)
                    .setContentText(mSubtitle)
                    .setStyle(mInboxStyle)
                    .setContentIntent(resultPendingIntent)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo))
                    .build();

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);

        }

    }

    public ArrayList<Aufgabe> getDueTasks_Array() {
        ArrayList<Aufgabe> dueAufgaben = new ArrayList<Aufgabe>();
        DaysLeft daysLef = new DaysLeft();
        ArrayList<Rubrik> rubriken = RubrikLab.get(mContext).getRubriken();
        for (Rubrik r : rubriken) {
            ArrayList<Aufgabe> aufgaben = r.getAufgabenArrayList(mContext);
            for (Aufgabe a : aufgaben) {
                if (a.getDeadline() != null && daysLef.getDaysLeft(a.getDeadline()) == 0) {
                    dueAufgaben.add(a);
                }
            }
        }
        return dueAufgaben;
    }
}
