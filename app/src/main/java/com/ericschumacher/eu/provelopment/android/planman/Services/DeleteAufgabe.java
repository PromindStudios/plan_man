package com.ericschumacher.eu.provelopment.android.planman.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * Created by eric on 01.10.2015.
 */
public class DeleteAufgabe extends IntentService {


    public DeleteAufgabe() {
        super("DeleteAufgabe");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do work
        String response = "";

        String aufgabe_uuid = intent.getStringExtra(Constants.ID_AUFGABE);

        try {
            // Create JSONObject which will be send to Server
            JSONObject JsonObject = new JSONObject();

            JsonObject.put("aufgabe_uuid", aufgabe_uuid);

            Log.i("Delete prepared: ", JsonObject.toString());

            URL url = new URL("http://provelopment-server.de/Primelist/DeleteAufgabe.php");
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
            Log.i("Response_DeleteAuf.: ", response);

            int result = Integer.parseInt(response);

            if (result == 1) {
                Log.i("DeleteAufgabe: ", "successful");
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1);
        } catch (Exception e) {

        }
    }
}
