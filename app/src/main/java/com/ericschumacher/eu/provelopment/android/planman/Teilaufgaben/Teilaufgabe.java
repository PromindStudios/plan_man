package com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben;

import android.content.Context;
import android.content.SharedPreferences;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constans;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by eric on 02.07.2015.
 */
public class Teilaufgabe {

    UUID mId;
    long mId_long;
    String mTitle;
    boolean mDone;

    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_DONE = "done";
    private static final String JSON_ID_LONG = "id_long";


    public Teilaufgabe(Context context) {
        mId = UUID.randomUUID();
        mDone = false;

        createId_Long(context);
    }

    public Teilaufgabe(JSONObject json, Context context) throws JSONException {
        mId = UUID.fromString((json.getString(JSON_ID)));
        if (json.has(JSON_TITLE)) {
            mTitle = json.getString(JSON_TITLE);
        }
        mDone = json.getBoolean(JSON_DONE);

        if (json.has(JSON_ID_LONG)) {
            mId_long = json.getLong(JSON_ID_LONG);
        } else {
            // load and create id_long
            createId_Long(context);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_ID_LONG, Long.toString(mId_long));
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_DONE, mDone);
        return json;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        mDone = done;
    }

    public long getId_long() {
        return mId_long;
    }

    private void createId_Long(Context context) {

        // load value
        SharedPreferences settings = context.getSharedPreferences(Constans.SHARED_PREFERENCES, 0);
        long lastIdUsed = settings.getLong(Constans.SP_TA_ID_LONG_LAST, 0);
        lastIdUsed++;
        mId_long = lastIdUsed;

        // save value
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constans.SP_TA_ID_LONG_LAST, lastIdUsed);
        editor.commit();

    }
}
