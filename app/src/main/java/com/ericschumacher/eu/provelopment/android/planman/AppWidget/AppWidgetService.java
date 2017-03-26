package com.ericschumacher.eu.provelopment.android.planman.AppWidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by eric on 28.02.2016.
 */
public class AppWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class AppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private ArrayList<Aufgabe> mDueTasks;
    private ArrayList<Rubrik> mRubriken;


    public AppWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mDueTasks = new ArrayList<Aufgabe>();
        int daysLeft;

        // Get all Rubriken
        mRubriken = RubrikLab.get(mContext).getRubriken();
        for (Rubrik r : mRubriken) {
            ArrayList<Aufgabe> mAufgaben = new ArrayList<Aufgabe>();
            mAufgaben = r.getAufgabenArrayList(mContext);
            for (Aufgabe a : mAufgaben) {
                if (a.getDeadline() != null) {
                    daysLeft = (int) getNumberOfLeftDays(a.getDeadline());
                    if (daysLeft <= 1) {
                        mDueTasks.add(a);
                    }

                }
            }
        }


    }

    @Override
    public void onDataSetChanged() {
        /*
        String uuid = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES, 0).getString(Constants.APP_WIDGET_ID_AUFGABE, "da");
        if (!uuid.equals("da")) {
            Log.i("Factory: ", Integer.toString(mAppWidgetId));
            UUID aufgabenId = UUID.fromString(uuid);
            Aufgabe aufgabe = null;
            Rubrik rubrik = null;
            ArrayList<Rubrik> rubriken = RubrikLab.get(mContext).getRubriken();
            for (Rubrik r : rubriken) {
                aufgabe = r.getAufgabe(aufgabenId);
                if (aufgabe != null) {
                    rubrik = r;
                    break;
                }
            }
            rubrik.deleteAufgabe(aufgabe);
            rubrik.saveAufgaben();
            mDueTasks.remove(aufgabe);
        }
        */
        mDueTasks.clear();
        int daysLeft;

        // Get all Rubriken
        mRubriken = RubrikLab.get(mContext).getRubriken();
        for (Rubrik r : mRubriken) {
            ArrayList<Aufgabe> mAufgaben = new ArrayList<Aufgabe>();
            mAufgaben = r.getAufgabenArrayList(mContext);
            for (Aufgabe a : mAufgaben) {
                if (a.getDeadline() != null) {
                    daysLeft = (int) getNumberOfLeftDays(a.getDeadline());
                    if (daysLeft <= 1) {
                        mDueTasks.add(a);
                    }

                }
            }
        }

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Log.i("Size of DueTasks: ", Integer.toString(mDueTasks.size()));
        return mDueTasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Aufgabe aufgabe = mDueTasks.get(position);
        RemoteViews rv;
        int daysLeft = (int) getNumberOfLeftDays(aufgabe.getDeadline());

        rv = new RemoteViews(mContext.getPackageName(), R.layout.item_appwidget);


        // Fill item with Data
        rv.setTextViewText(R.id.tvTitelAppWidget, aufgabe.getTitle());
        rv.setTextViewText(R.id.tvRubrikAppWdget, aufgabe.getRubrikName());

        // Check DueDate and fill content with Data
        if (daysLeft == 1) {
            rv.setTextViewText(R.id.tvDeadlineAppWidget, mContext.getString(R.string.due_tomorrow));
            rv.setImageViewResource(R.id.ivDeadlineAppWidget, R.drawable.ic_clock_small_orange);
        }
        if (daysLeft == 0) {
            rv.setTextViewText(R.id.tvDeadlineAppWidget, mContext.getString(R.string.due_today));
            rv.setImageViewResource(R.id.ivDeadlineAppWidget, R.drawable.ic_clock_small_red);
        }
        if (daysLeft == -1) {
            rv.setTextViewText(R.id.tvDeadlineAppWidget, mContext.getString(R.string.overdue));
            rv.setImageViewResource(R.id.ivDeadlineAppWidget, R.drawable.ic_clock_small_red);
        }
        // check Priority
        if (aufgabe.getPrioritaet() == 1) {
            rv.setImageViewResource(R.id.ivPriorityAppWidget, R.drawable.ic_prioritaet_eins);
        }
        if (aufgabe.getPrioritaet() == 2) {
            rv.setImageViewResource(R.id.ivPriorityAppWidget, R.drawable.ic_prioritaet_zwei);
        }
        if (aufgabe.getPrioritaet() == 3) {
            rv.setViewVisibility(R.id.ivPriorityAppWidget, View.INVISIBLE);
        }

        // Check for Teilaufgaben
        if (aufgabe.getTeilaufgabenArrayList(mContext) != null && aufgabe.getTeilaufgabenArrayList(mContext).size() > 0) {
            String sizeTeilaufgaben = Integer.toString(aufgabe.getTeilaufgabenArrayList(mContext).size());
            String sizeTeilaufgaben_done = Integer.toString(aufgabe.getSize_DoneTeilaufgaben());
            if (Integer.parseInt(sizeTeilaufgaben) == Integer.parseInt(sizeTeilaufgaben_done)) {
                rv.setViewVisibility(R.id.tvTeilaufgabenNumberAppWidget, View.INVISIBLE);
                rv.setViewVisibility(R.id.ivDoneAppWidget, View.VISIBLE);
            } else {
                rv.setViewVisibility(R.id.ivDoneAppWidget, View.INVISIBLE);
                rv.setViewVisibility(R.id.tvTeilaufgabenNumberAppWidget, View.VISIBLE);
                rv.setTextViewText(R.id.tvTeilaufgabenNumberAppWidget, sizeTeilaufgaben_done + "/" + sizeTeilaufgaben);
            }
        } else {
            rv.setViewVisibility(R.id.tvTeilaufgabenNumberAppWidget, View.INVISIBLE);
            rv.setViewVisibility(R.id.ivDoneAppWidget, View.VISIBLE);


        }


        // Get the right Rubrik
        ArrayList<Rubrik> mRubriken = new ArrayList<>();
        Aufgabe checkAufgabe;
        mRubriken = RubrikLab.get(mContext).getRubriken();
        Rubrik rubrik;
        UUID rubrikUUID = null;
        UUID uuid = aufgabe.getId();
        for (Rubrik r : mRubriken) {
            checkAufgabe = r.getAufgabe(uuid);
            if (checkAufgabe != null) {
                rubrikUUID = r.getId();
            }
        }

        // Fill Intent for Link to Task
        if (rubrikUUID != null) {
            Log.i("WidgetService: ", "Link prepared");
            Bundle extras2 = new Bundle();
            extras2.putString(Constants.LINK_OR_CHECK, Constants.LINK);
            extras2.putString(Constants.ID_AUFGABE, aufgabe.getId().toString());
            extras2.putString(Constants.ID_RUBRIK, rubrikUUID.toString());
            Intent linkIntent = new Intent();
            linkIntent.putExtras(extras2);
            rv.setOnClickFillInIntent(R.id.llTextAppWidget, linkIntent);

            // Fill Intent for Check
            Bundle extras = new Bundle();
            extras.putString(Constants.LINK_OR_CHECK, Constants.CHECK);
            extras.putString(Constants.ID_AUFGABE, aufgabe.getId().toString());
            extras.putString(Constants.ID_RUBRIK, rubrikUUID.toString());
            Intent fillIntent = new Intent();
            fillIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.ivDoneAppWidget, fillIntent);
        }


        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public long getNumberOfLeftDays(Calendar deadline) {
        Calendar today = Calendar.getInstance();
        long daysBetween = 0;

        while (today.before(deadline)) {
            today.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }

        if (daysBetween == 0) {
            today.add(Calendar.DAY_OF_MONTH, -1);
            if (deadline.compareTo(today) == -1) {
                daysBetween = -1;
            }
        }
        return daysBetween;
    }
}

