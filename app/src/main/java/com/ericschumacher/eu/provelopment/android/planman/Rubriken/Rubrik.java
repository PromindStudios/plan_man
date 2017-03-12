package com.ericschumacher.eu.provelopment.android.planman.Rubriken;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.AufgabenIntentJSONSerializer;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.Services.DeleteAufgabe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

/**
 * Created by eric on 01.07.2015.
 */
public class Rubrik {

    // Constants
    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_FILENAME_AUFGABE = "filename_aufgabe";
    private static final String JSON_SORTY_BY_DATE = "sort_by_date";
    private static final String JSON_CONNECTED = "connected";

    // Attributes
    private UUID mId;
    private String mTitle;
    private String mFilenameAufgaben;
    private ArrayList<Aufgabe> mAufgaben;
    private AufgabenIntentJSONSerializer mSerializer;
    private static Context sAppContext;
    private boolean mSortByDate;
    private boolean mIsConnected;

    public Rubrik(String title, Context context) {

        mId = UUID.randomUUID();
        mTitle = title;
        mSortByDate = true;
        mAufgaben = new ArrayList<Aufgabe>();
        mFilenameAufgaben = UUID.randomUUID().toString().replaceAll("-", "") + ".json";
        mIsConnected = false;
        sAppContext = context;
    }


    public Rubrik(JSONObject json, Context context) throws JSONException {
        sAppContext = context;
        mId = UUID.fromString(json.getString(JSON_ID));
        mFilenameAufgaben = json.getString(JSON_FILENAME_AUFGABE);

        if (json.has(JSON_SORTY_BY_DATE)) {
            mSortByDate = json.getBoolean(JSON_SORTY_BY_DATE);
        } else {
            mSortByDate = true;
        }


        if (json.has(JSON_TITLE)) {
            mTitle = json.getString(JSON_TITLE);
        }

        if (json.has(JSON_CONNECTED)) {
            mIsConnected = json.getBoolean(JSON_CONNECTED);
        } else {
            mIsConnected = false;
        }
        mSerializer = new AufgabenIntentJSONSerializer(context);

        try {
            mAufgaben = mSerializer.loadAufgaben(mFilenameAufgaben);

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.mIsConnected = isConnected;
    }

    public ArrayList<Aufgabe> getAufgabenArrayList(Context c) {


        sAppContext = c.getApplicationContext();

        mSerializer = new AufgabenIntentJSONSerializer(sAppContext);

        /*
        try {
            mAufgaben = mSerializer.loadAufgaben(mFilenameAufgaben);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */


        if (mSortByDate) {
            Log.i("SortByDate: ", "True");
            Collections.sort(mAufgaben, new CustomComparator());
        } else {
            Log.i("SortByDate: ", "False");
            mAufgaben = sort(mAufgaben);
        }


        return mAufgaben;
    }


    public void addAufgabe(Aufgabe a) {
        mAufgaben.add(a);
    }

    public void deleteAufgabenArrayList() {
        mAufgaben = null;
    }

    public void deleteAufgabe(Aufgabe a) {
        mAufgaben.remove(a);

    }

    public Aufgabe getAufgabe(UUID id) {
        for (Aufgabe a : mAufgaben) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_FILENAME_AUFGABE, mFilenameAufgaben);
        json.put(JSON_SORTY_BY_DATE, mSortByDate);
        json.put(JSON_CONNECTED, mIsConnected);
        //saveAufgaben();
        //mSerializer = new AufgabenIntentJSONSerializer(sAppContext, FILENAME);
        //try {
        //mSerializer.saveAufgaben(mList);
        //} catch (Exception e) {
        //}
        return json;
    }

    public boolean isSortByDate() {
        return mSortByDate;
    }

    public void setSortByDate(boolean sortByDate) {
        mSortByDate = sortByDate;
    }

    public boolean saveAufgaben() {
        try {
            int Size = mAufgaben.size();
            //Log.i("Size_save: ", Integer.toString(Size)); // KEIN FEHLER
            mSerializer = new AufgabenIntentJSONSerializer(sAppContext);
            mSerializer.saveAufgaben(mAufgaben, mFilenameAufgaben);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public int getCountAufgaben() {
        int number;
        if (mAufgaben == null) {
            number = 0;

        } else {
            number = mAufgaben.size();
        }
        return number;
    }

    public String getFilenameAufgaben() {
        return mFilenameAufgaben;
    }

    public class CustomComparator implements Comparator<Aufgabe> {

        @Override
        public int compare(Aufgabe lhs, Aufgabe rhs) {

            boolean change_one = false;
            boolean change_two = false;
            if (lhs.getDeadline() == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2090, 1, 1);
                lhs.setDeadline(calendar);
                change_one = true;
            }
            if (rhs.getDeadline() == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2091, 1, 1);
                rhs.setDeadline(calendar);
                change_two = true;
            }
            int i = lhs.getDeadline().compareTo(rhs.getDeadline());

            if (change_one) {
                lhs.setDeadline(null);
            }
            if (change_two) {
                rhs.setDeadline(null);
            }
            return i;

        }
    }

    public ArrayList<Aufgabe> sort(ArrayList<Aufgabe> aufgaben) {

        ArrayList<Aufgabe> sortedAufgaben_eins = new ArrayList<Aufgabe>();
        ArrayList<Aufgabe> sortedAufgaben_zwei = new ArrayList<Aufgabe>();
        ArrayList<Aufgabe> sortedAufgaben_drei = new ArrayList<Aufgabe>();
        for (Aufgabe a : aufgaben) {
            if (a.getPrioritaet() == 1) {
                sortedAufgaben_eins.add(a);
            }

        }
        for (Aufgabe a : aufgaben) {
            if (a.getPrioritaet() == 2) {
                sortedAufgaben_zwei.add(a);
            }

        }
        for (Aufgabe a : aufgaben) {
            if (a.getPrioritaet() == 3) {
                sortedAufgaben_drei.add(a);
            }

        }
        Collections.sort(sortedAufgaben_eins, new CustomComparator());
        Collections.sort(sortedAufgaben_zwei, new CustomComparator());
        Collections.sort(sortedAufgaben_drei, new CustomComparator());
        for (Aufgabe a : sortedAufgaben_zwei) {
            sortedAufgaben_eins.add(a);
        }
        for (Aufgabe a : sortedAufgaben_drei) {
            sortedAufgaben_eins.add(a);
        }

        return sortedAufgaben_eins;

    }

    public ArrayList<Aufgabe> sortbyDate_for_Rubriken() {
        ArrayList<Aufgabe> aufgaben = mAufgaben;
        Collections.sort(aufgaben, new CustomComparator());
        return aufgaben;

    }


}