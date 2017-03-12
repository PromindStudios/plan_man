package com.ericschumacher.eu.provelopment.android.planman.Aufgaben;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.Services.UpdateAufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.TeilaufgabenIntentJSONSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by eric on 01.07.2015.
 */
public class Aufgabe {


    // Aufgaben Values
    private UUID mId;
    private String mTitle;
    private String mNotiz;
    private Calendar mDeadline;
    private int mJahr;
    private int mMonat;
    private int mTag;
    private int mPrioritaet;
    private String mFilenameTeilaufgaben;
    private String mRubrikName;

    private ArrayList<Teilaufgabe> mTeilaufgaben;
    private TeilaufgabenIntentJSONSerializer mSerializer;
    private static Context sAppContext;

    // Names for saving in JSON Object
    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_NOTIZ = "notiz";
    private static final String JSON_TEILAUFGABEN_GESAMT = "teilaufgaben_gesamt";
    private static final String JSON_DEADLINE = "deadline";
    private static final String JSON_TAG = "tag";
    private static final String JSON_MONAT = "monat";
    private static final String JSON_JAHR = "jahr";
    private static final String JSON_TEILAUFGABEN_FILENAME = "filename_teilaufgaben";
    private static final String JSON_TEILAUFGABEN_SIZE = "teilaufgaben_size";
    private static final String JSON_TEILAUFGABEN_DONE = "teilaufgaben_done";
    private static final String JSON_PRIORITAET = "prioritaet";
    private static final String JSON_RUBRIKNAME = "rubrikname";


    public Aufgabe(String rubrikName) {
        mId = UUID.randomUUID();
        mTitle = "";
        mTeilaufgaben = new ArrayList<Teilaufgabe>();
        mPrioritaet = 3;
        mFilenameTeilaufgaben = UUID.randomUUID().toString().replaceAll("-", "") + ".json";
        mRubrikName = rubrikName;
    }

    public Aufgabe(JSONObject json, Context context) throws JSONException {
        String Filename = "Default";
        mId = UUID.fromString(json.getString(JSON_ID));
        if (json.has(JSON_TEILAUFGABEN_FILENAME)) {
            mFilenameTeilaufgaben = json.getString(JSON_TEILAUFGABEN_FILENAME);
        } else {
            mFilenameTeilaufgaben = UUID.randomUUID().toString().replaceAll("-", "") + ".json";
        }

        if (json.has(JSON_TITLE)) {
            mTitle = json.getString(JSON_TITLE);
            Filename = mTitle + ".json";
        }

        if (json.has(JSON_PRIORITAET)) {
            mPrioritaet = json.getInt(JSON_PRIORITAET);
        }

        // get Notiz
        if (json.has(JSON_NOTIZ)) {
            mNotiz = json.getString(JSON_NOTIZ);
        }

        // get Rubriknamen
        if (json.has(JSON_RUBRIKNAME)) {
            mRubrikName = json.getString(JSON_RUBRIKNAME);
        } else {
            mRubrikName = "kein Rubrik-Name";
        }

        // get Information for Deadline
        if (json.has(JSON_TAG)) {

            if (json.getInt(JSON_JAHR) > 2014) {
                mTag = json.getInt(JSON_TAG);
                mMonat = json.getInt(JSON_MONAT);
                mJahr = json.getInt(JSON_JAHR);

                mDeadline = Calendar.getInstance();
                mDeadline.set(Calendar.DAY_OF_MONTH, mTag);
                mDeadline.set(Calendar.MONTH, mMonat);
                mDeadline.set(Calendar.YEAR, mJahr);

            } else {
                mDeadline = null;
                Log.i("Deadline: ", "null_intern");
            }

        } else {
            mDeadline = null;
            Log.i("Deadline: ", "null");
        }

        mSerializer = new TeilaufgabenIntentJSONSerializer(context);

        try {
            mTeilaufgaben = mSerializer.loadTeilaufgaben(mFilenameTeilaufgaben);


        } catch (Exception e) {
            mTeilaufgaben = new ArrayList<Teilaufgabe>();
            e.printStackTrace();
        }


    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        if (mDeadline == null) {
            /*
            mTag = -1;
            Log.i("Tag: ", Integer.toString(mTag));
            mMonat = -1;
            mJahr = -1;
            */
        } else {
            mTag = mDeadline.get(Calendar.DAY_OF_MONTH);
            Log.i("Tag: ", Integer.toString(mTag));
            mMonat = mDeadline.get(Calendar.MONTH);
            mJahr = mDeadline.get(Calendar.YEAR);

            json.put(JSON_TAG, mTag);
            json.put(JSON_MONAT, mMonat);
            json.put(JSON_JAHR, mJahr);
        }

        json.put(JSON_ID, mId.toString());
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_NOTIZ, mNotiz);

        json.put(JSON_TEILAUFGABEN_FILENAME, mFilenameTeilaufgaben);
        json.put(JSON_PRIORITAET, mPrioritaet);
        json.put(JSON_RUBRIKNAME, mRubrikName);
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

    public String getNotiz() {
        return mNotiz;
    }

    public void setNotiz(String notiz, Boolean connnected) {
        mNotiz = notiz;
    }

    public String getRubrikName() {
        return mRubrikName;
    }

    public void setRubrikName(String rubrikName) {
        mRubrikName = rubrikName;
    }

    public ArrayList<Teilaufgabe> getTeilaufgabenArrayList(Context c) {

        sAppContext = c.getApplicationContext();


        mSerializer = new TeilaufgabenIntentJSONSerializer(sAppContext);

        try {
            mTeilaufgaben = mSerializer.loadTeilaufgaben(mFilenameTeilaufgaben);

        } catch (Exception e) {

        }

        //mList = sort(mList);

        return mTeilaufgaben;
    }

    public void addTeilaufgabe(Teilaufgabe a) {
        mTeilaufgaben.add(a);
    }

    public void deleteTeilaufgabe(Teilaufgabe a) {
        mTeilaufgaben.remove(a);
        // delete Teilaufgabe on Server!


    }

    public Teilaufgabe getTeilaufgabe(UUID id) {
        for (Teilaufgabe a : mTeilaufgaben) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }

    public boolean saveTeilaufgaben() {
        try {
            mSerializer.saveTeilaufgaben(mTeilaufgaben, mFilenameTeilaufgaben);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Calendar getDeadline() {
        return mDeadline;
    }

    public void setDeadline(Calendar deadline) {
        mDeadline = deadline;
        if (deadline != null) {
            Log.i("Calendar: ", deadline.toString());
        } else {
            Log.i("Calendar: ", "null");
        }

    }

    public int getPrioritaet() {
        return mPrioritaet;
    }

    public void setPrioritaet(int prioritaet) {
        mPrioritaet = prioritaet;
    }

    public int getSize_DoneTeilaufgaben() {
        int size_done = 0;

        for (Teilaufgabe teilaufgabe : mTeilaufgaben) {
            if (teilaufgabe.isDone()) {
                size_done++;
            }
        }
        return size_done;
    }


}
