package com.ericschumacher.eu.provelopment.android.planman.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 27.09.2015.
 */
public class CheckUserAttachedToRubrik extends IntentService {

    ArrayList<Rubrik> mRubriken;
    int mUserId;

    public CheckUserAttachedToRubrik() {
        super("CheckUserAttachedToRubrik");
    }

    @Override
    protected void onHandleIntent(Intent intent_carefull) {

        Log.i("Service: ", "started");

        // get Shared Preferences
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mUserId = settings.getInt(Constants.USER_ID, 0);

        mRubriken = RubrikLab.get(this).getRubriken();


        String response = "";

        try {
            // Create JSONObject which will be send to Server
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", Integer.toString(mUserId));
            JSONArray rubriken_uuids = new JSONArray();

            for (int i = 0; i < mRubriken.size(); i++) {
                JSONObject jsonObject_rubrik = new JSONObject();
                jsonObject_rubrik.put("uuid", mRubriken.get(i).getId().toString());
                rubriken_uuids.put(jsonObject_rubrik);
            }

            jsonObject.put("rubriken_uuids", rubriken_uuids);

            Log.i("Data prepared: ", jsonObject.toString());

            URL url = new URL("http://provelopment-server.de/Primelist/check_user_attached_to_rubrik.php");
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
            Log.i("Response_Att.Rubrik: ", response);

            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.getInt("result") == 1) {
                String uuid = jsonResponse.getString("rubrik_uuid");

                // send Message to Activity
                Intent i = new Intent(Constants.ADD_RUBRIK_TO_RUBRIKEN);
                // add data
                i.putExtra("uuid", uuid);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
