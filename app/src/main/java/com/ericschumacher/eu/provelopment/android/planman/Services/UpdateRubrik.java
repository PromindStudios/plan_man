package com.ericschumacher.eu.provelopment.android.planman.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
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
 * Created by eric on 29.09.2015.
 */
public class UpdateRubrik extends IntentService{


    public UpdateRubrik() {
        super("UpdateRubrik");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//Rubrik rubrik = RubrikLab.get(AufgabeErstellen.this).getRubrik(UUID.fromString(params[0]));
        String response = "";

        // get Shared Preferences
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        String rubrik_uuid = intent.getStringExtra(Constants.RUBRIK_UUID);
        Rubrik mRubrik = RubrikLab.get(this).getRubrik(UUID.fromString(rubrik_uuid));

        if (mRubrik.isConnected()) {
            try {
                // Create JSONObject which will be send to Server
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rubrik_uuid", mRubrik.getId().toString());
                jsonObject.put("rubrik_name", mRubrik.getTitle());
                int user_id = settings.getInt(Constants.USER_ID, 0);
                jsonObject.put("user_id", Integer.toString(user_id));

                ArrayList<Aufgabe> aufgaben = mRubrik.getAufgabenArrayList(UpdateRubrik.this);

                JSONArray jsonArray_aufgaben = new JSONArray();

                for (int i = 0; i < aufgaben.size(); i++) {
                    JSONObject jsonObject_aufgabe = new JSONObject();
                    Aufgabe aufgabe = aufgaben.get(i);
                    jsonObject_aufgabe.put("aufgabe_uuid", aufgabe.getId());
                    jsonObject_aufgabe.put("aufgabe_title", aufgabe.getTitle());
                    jsonObject_aufgabe.put("aufgabe_notiz", aufgabe.getNotiz());
                    Calendar deadline = aufgabe.getDeadline();
                    if (deadline != null) {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", deadline.get(Calendar.YEAR));
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", deadline.get(Calendar.MONTH));
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", deadline.get(Calendar.DAY_OF_MONTH));
                    } else {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", -1);
                    }

                    jsonObject_aufgabe.put("aufgabe_prioritaet", aufgabe.getPrioritaet());
                    jsonObject_aufgabe.put("aufgabe_rubrikName", aufgabe.getRubrikName());

                    // get Teilaufgaben and pack them into the jsonObject as an JSONArray
                    ArrayList<Teilaufgabe> teilaufgaben = aufgabe.getTeilaufgabenArrayList(UpdateRubrik.this);
                    JSONArray jsonArray_teilaufgaben = new JSONArray();

                    for (int i2 = 0; i2 < teilaufgaben.size(); i2++) {
                        JSONObject jsonObject_teilaufgabe = new JSONObject();
                        Teilaufgabe teilaufgabe = teilaufgaben.get(i2);
                        jsonObject_teilaufgabe.put("teilaufgabe_uuid", teilaufgabe.getId().toString());
                        jsonObject_teilaufgabe.put("teilaufgabe_title", teilaufgabe.getTitle());
                        int done = (teilaufgabe.isDone()) ? 1 : 0;
                        jsonObject_teilaufgabe.put("teilaufgabe_done", done);

                        jsonArray_teilaufgaben.put(jsonObject_teilaufgabe);
                    }
                    jsonObject_aufgabe.put("teilaufgaben_jsonArray", jsonArray_teilaufgaben);

                    jsonArray_aufgaben.put(jsonObject_aufgabe);
                    // weiter mit Teilaufgaben
                }
                jsonObject.put("aufgaben_jsonArray", jsonArray_aufgaben);

                Log.i("Data prepared: ", jsonObject.toString());

                URL url = new URL("http://provelopment-server.de/Primelist/sendData_Rubrik.php");
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
                Log.i("Response_upd.Rubrik: ", response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
