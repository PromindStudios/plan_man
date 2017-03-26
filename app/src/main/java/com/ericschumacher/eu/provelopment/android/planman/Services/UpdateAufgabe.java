package com.ericschumacher.eu.provelopment.android.planman.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Finder;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 01.10.2015.
 */
public class UpdateAufgabe extends IntentService{

    public UpdateAufgabe() {
        super("UpdateAufgabe");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String response = "";

        String aufgabe_uuid = intent.getStringExtra(Constants.ID_AUFGABE);

        String rubrik_uuid = new Finder().getRubrikUUID(aufgabe_uuid, this);
        Rubrik mRubrik = RubrikLab.get(this).getRubrik(UUID.fromString(rubrik_uuid));
        Aufgabe aufgabe = mRubrik.getAufgabe(UUID.fromString(aufgabe_uuid));

        // get Shared Preferences
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);


        try {
            // Create JSONObject which will be send to Server
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", Integer.toString(settings.getInt(Constants.USER_ID, 1)));
            jsonObject.put("rubrik_uuid", mRubrik.getId().toString());
            jsonObject.put("rubrik_name", mRubrik.getTitle());
            jsonObject.put("aufgabe_uuid", aufgabe.getId().toString());
            jsonObject.put("aufgabe_title", aufgabe.getTitle());
            jsonObject.put("aufgabe_notiz", aufgabe.getNotiz());
            Calendar deadline = aufgabe.getDeadline();
            if (deadline != null) {
                jsonObject.put("aufgabe_jahrDeadline", deadline.get(Calendar.YEAR));
                jsonObject.put("aufgabe_monatDeadline", deadline.get(Calendar.MONTH));
                jsonObject.put("aufgabe_tagDeadline", deadline.get(Calendar.DAY_OF_MONTH));
            } else {
                jsonObject.put("aufgabe_jahrDeadline", -1);
                jsonObject.put("aufgabe_monatDeadline", -1);
                jsonObject.put("aufgabe_tagDeadline", -1);
            }

            jsonObject.put("aufgabe_prioritaet", aufgabe.getPrioritaet());

            Log.i("Aufgabe prepared: ", jsonObject.toString());

            URL url = new URL("http://provelopment-server.de/Primelist/UpdateAufgabe.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
            Log.i("Response_upd.Aufgabe: ", response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
