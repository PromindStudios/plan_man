package com.ericschumacher.eu.provelopment.android.planman.AppWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 21.02.2016.
 */
public class MyAppWidgetProvider extends AppWidgetProvider {

    private static final String HOME_CLICKED = "home_clicked";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        updateWidget(appWidgetManager, context, appWidgetIds);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Constants.APP_WIDGET_ITEM_CLICK)) {
            if (intent.getExtras().getString(Constants.LINK_OR_CHECK).equals(Constants.CHECK)) {
                int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                String uuid = intent.getStringExtra(Constants.ID_AUFGABE);
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.APP_WIDGET_ID_AUFGABE, uuid);
                editor.commit();



                // Delete Aufgabe with tranfered Id
                UUID aufgabenId = UUID.fromString(uuid);
                Aufgabe aufgabe = null;
                Rubrik rubrik = null;
                ArrayList<Rubrik> rubriken = RubrikLab.get(context).getRubriken();
                for (Rubrik r : rubriken) {
                    aufgabe = r.getAufgabe(aufgabenId);
                    if (aufgabe != null) {
                        rubrik = r;
                        break;
                    }
                }
                rubrik.deleteAufgabe(aufgabe);
                rubrik.saveAufgaben();


                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = {appWidgetId};

                final int N = appWidgetIds.length;
                Log.i("Number Ids: ", Integer.toString(N));

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lvAppWidget);
            } else {
                Log.i("Widget Provider: ", "Link is fired");
                Intent aufgabeIntent = new Intent(context, AufgabeErstellen.class);
                Bundle extras = intent.getExtras();
                aufgabeIntent.putExtra(Constants.ID_AUFGABE, UUID.fromString(extras.getString(Constants.ID_AUFGABE, "")));
                aufgabeIntent.putExtra(Constants.ID_RUBRIK, UUID.fromString(extras.getString(Constants.ID_RUBRIK, "")));
                aufgabeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(aufgabeIntent);
            }

        }
        if (HOME_CLICKED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Intent intentMain = new Intent(context, Main.class);
            intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intentMain);
        }
        if(intent.getAction().equals(Constants.LINK_TO_AUFGABE)) {

        }

        super.onReceive(context, intent);
    }

    private void updateWidget (AppWidgetManager appWidgetManager, Context context, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Go through every App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            Log.i("Widget Provider: ", "Update fired");

            Intent intent = new Intent(context, AppWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Initiate the RemoteViews
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            // Modify RemoteViews
            rv.setRemoteAdapter(R.id.lvAppWidget, intent);
            rv.setEmptyView(R.id.lvAppWidget, R.id.appWidgetEmptyview);
            rv.setOnClickPendingIntent(R.id.ibHomeAppWidget, getPendingSelfIntent(context, HOME_CLICKED));

            // Create unique behaviour for each item link click
            Intent linkIntent = new Intent(context, MyAppWidgetProvider.class);
            linkIntent.setAction(Constants.LINK_TO_AUFGABE);
            linkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            linkIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent linkPendingIntent = PendingIntent.getBroadcast(context, 0, linkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.lvAppWidget, linkPendingIntent);

            // Create unique behaviour for each item check click
            Intent clickIntent = new Intent(context, MyAppWidgetProvider.class);
            clickIntent.setAction(Constants.APP_WIDGET_ITEM_CLICK);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            clickIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.lvAppWidget, clickPendingIntent);

            // Update
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);

        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
