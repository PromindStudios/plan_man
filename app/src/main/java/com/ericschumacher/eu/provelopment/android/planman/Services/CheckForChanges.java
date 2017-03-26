package com.ericschumacher.eu.provelopment.android.planman.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

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
import java.util.Calendar;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 29.09.2015.
 */
public class CheckForChanges extends IntentService {


    public CheckForChanges() {
        super("CheckForChanges");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("CheckForChanges: ", "Checks!");

        // Do work
        String response = "";

        // get Shared Preferences
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        try {
            // Create JSONObject which will be send to Server
            JSONObject JsonObject = new JSONObject();

            int user_id = settings.getInt(Constants.USER_ID, 0);
            JsonObject.put("user_id", Integer.toString(user_id));

            Log.i("Data prepared: ", JsonObject.toString());

            URL url = new URL("http://provelopment-server.de/Primelist/checkForChanges.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonObject.toString());

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
            Log.i("Response_CheckChanges: ", response);

            JSONObject jObjPost = new JSONObject(response);

            if (jObjPost.getInt("result") == 1) {
                // get Rubrik

                RubrikLab mRubrikLab = RubrikLab.get(CheckForChanges.this);
                Rubrik newRubrik = new Rubrik(jObjPost.getString("rubrikName"), getApplicationContext());
                newRubrik.setId(UUID.fromString(jObjPost.getString("rubrikUUID")));
                newRubrik.setIsConnected(true);

                // get old Rubrik and delete it
                Rubrik oldRubrik = mRubrikLab.getRubrik(UUID.fromString(jObjPost.getString("rubrikUUID")));
                mRubrikLab.deleteRubrik(oldRubrik);

                mRubrikLab.addRubrik(newRubrik);

                // delete all Aufgaben in Rubrik
                //newRubrik.deleteAufgabenArrayList();

                // get AufgabenArray
                JSONArray aufgabenArray = jObjPost.getJSONArray("aufgabenArray");

                for (int i = 0; i <aufgabenArray.length(); i++) {
                    JSONObject jsonObject = aufgabenArray.getJSONObject(i);
                    // Error
                    Aufgabe aufgabe = new Aufgabe(jsonObject.getString("rubrikName"));
                    try {
                        aufgabe.setId(UUID.fromString(jsonObject.getString("uuid")));
                    } catch (IllegalArgumentException e) {
                        aufgabe.setId(UUID.randomUUID());
                    }
                    aufgabe.setTitle(jsonObject.getString("title"));
                    aufgabe.setNotiz(jsonObject.getString("notiz"), false);

                    if (jsonObject.getInt("jahrDeadline") > 0) {
                        Calendar deadline = Calendar.getInstance();
                        deadline.set(Calendar.DAY_OF_MONTH, jsonObject.getInt("tagDeadline"));
                        deadline.set(Calendar.MONTH, jsonObject.getInt("monatDeadline"));
                        deadline.set(Calendar.YEAR, jsonObject.getInt("jahrDeadline"));
                        aufgabe.setDeadline(deadline);
                    } else {
                        aufgabe.setDeadline(null);
                    }
                    aufgabe.setPrioritaet(jsonObject.getInt("prioritaet"));

                    // get TeilaufgabenArray
                    JSONArray teilaufgabenArray = jsonObject.getJSONArray("teilaufgabenArray");

                    for (int i2 = 0; i < teilaufgabenArray.length(); i++) {
                        JSONObject jsonObject2 = aufgabenArray.getJSONObject(i2);
                        Teilaufgabe teilaufgabe = new Teilaufgabe(getApplicationContext());
                        try {
                            teilaufgabe.setId(UUID.fromString(jsonObject2.getString("uuid")));
                        } catch (IllegalArgumentException ex) {
                            teilaufgabe.setId(UUID.randomUUID());
                        }

                        teilaufgabe.setTitle(jsonObject2.getString("title"));
                        try {
                            teilaufgabe.setDone(jsonObject2.getBoolean("done"));
                        } catch (Exception e) {
                            teilaufgabe.setDone(false);
                        }

                        aufgabe.addTeilaufgabe(teilaufgabe);
                    }

                    newRubrik.addAufgabe(aufgabe);
                    aufgabe.saveTeilaufgaben();
                }
                mRubrikLab.saveRubriken();
                newRubrik.saveAufgaben();

                // restart Service
                Intent i = new Intent(this, CheckForChanges.class);
                startService(i);
            } else {
                Log.i("CheckForChanges: ", "no changes!");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}




/*
public class CheckForChanges extends IntentService{


    public CheckForChanges() {
        super("CheckForChanges");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i("CheckForChanges: ", "called");


        String response = "";

        // get Shared Preferences
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        try {
            // Create JSONObject which will be send to Server
            JSONObject JsonObject = new JSONObject();

            int user_id = settings.getInt(Constants.USER_ID, 0);
            JsonObject.put("user_id", Integer.toString(user_id));

            Log.i("Data prepared: ", JsonObject.toString());

            URL url = new URL("http://provelopment-server.de/Primelist/checkForChanges.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(JsonObject.toString());

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
            Log.i("Response_CheckChanges: ", response);

            JSONObject jObjPost = new JSONObject(response);

            if (jObjPost.getInt("result") == 1) {
                // get Rubrik

                RubrikLab mRubrikLab = RubrikLab.get(this);
                Rubrik newRubrik = new Rubrik(jObjPost.getString("rubrikName"), getApplicationContext());
                newRubrik.setId(UUID.fromString(jObjPost.getString("rubrikUUID")));
                newRubrik.setIsConnected(true);

                // get old Rubrik and delete it
                Rubrik oldRubrik = mRubrikLab.getRubrik(UUID.fromString(jObjPost.getString("rubrikUUID")));
                mRubrikLab.deleteRubrik(oldRubrik);

                mRubrikLab.addRubrik(newRubrik);

                // delete all Aufgaben in Rubrik
                //newRubrik.deleteAufgabenArrayList();

                // get AufgabenArray
                JSONArray aufgabenArray = jObjPost.getJSONArray("aufgabenArray");

                for (int i = 0; i <aufgabenArray.length(); i++) {
                    JSONObject jsonObject = aufgabenArray.getJSONObject(i);
                    // Error
                    Aufgabe aufgabe = new Aufgabe(jsonObject.getString("rubrikName"));
                    try {
                        aufgabe.setId(UUID.fromString(jsonObject.getString("uuid")));
                    } catch (IllegalArgumentException e) {
                        aufgabe.setId(UUID.randomUUID());
                    }
                    aufgabe.setTitle(jsonObject.getString("title"));
                    aufgabe.setNotiz(jsonObject.getString("notiz"));

                    if (jsonObject.getInt("jahrDeadline") > 0) {
                        Calendar deadline = Calendar.getInstance();
                        deadline.set(Calendar.DAY_OF_MONTH, jsonObject.getInt("tagDeadline"));
                        deadline.set(Calendar.MONTH, jsonObject.getInt("monatDeadline"));
                        deadline.set(Calendar.YEAR, jsonObject.getInt("jahrDeadline"));
                        aufgabe.setDeadline(deadline);
                    } else {
                        aufgabe.setDeadline(null);
                    }
                    aufgabe.setPrioritaet(jsonObject.getInt("prioritaet"));

                    // get TeilaufgabenArray
                    JSONArray teilaufgabenArray = jsonObject.getJSONArray("teilaufgabenArray");

                    for (int i2 = 0; i < teilaufgabenArray.length(); i++) {
                        JSONObject jsonObject2 = aufgabenArray.getJSONObject(i2);
                        Teilaufgabe teilaufgabe = new Teilaufgabe(getApplicationContext());
                        try {
                            teilaufgabe.setId(UUID.fromString(jsonObject2.getString("uuid")));
                        } catch (IllegalArgumentException ex) {
                            teilaufgabe.setId(UUID.randomUUID());
                        }

                        teilaufgabe.setTitle(jsonObject2.getString("title"));
                        teilaufgabe.setDone(jsonObject2.getBoolean("done"));
                        aufgabe.addTeilaufgabe(teilaufgabe);
                    }

                    newRubrik.addAufgabe(aufgabe);
                    aufgabe.saveTeilaufgaben();
                }
                mRubrikLab.saveRubriken();
                newRubrik.saveAufgaben();

                // restart Service quicker
            } else {
                Log.i("CheckForChanges: ", "no changes!");

                // restart Service
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
 */
