package com.ericschumacher.eu.provelopment.android.planman.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Adapter_AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Check;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_DatePicker;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Get_Premium;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_TestVersion_Limit;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_TestVersion_RunningOut;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AnalyticsApplication;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Services.CheckForChanges;
import com.ericschumacher.eu.provelopment.android.planman.Services.UpdateRubrik;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 27.08.2015.
 */
public class AufgabenListe extends Fragment implements Adapter_AufgabenListe.AufgabenAdapter_Listener, Dialog_Aufgabe_Check.DialogAufgabeDelete_Listener, Dialog_DatePicker.DatePickerListener,
        Dialog_TestVersion_Limit.TestVersionLimitListener, Dialog_TestVersion_RunningOut.Dialog_TestVersionRunningOut_Listener{

    // Layout Components
    private RecyclerView rvAufgabenListe;
    private FloatingActionButton fabAdd;
    private Adapter_AufgabenListe mAdapter;
    private RelativeLayout rlHeaderGround;

    // Data Components
    private Rubrik mRubrik;
    private ArrayList<Aufgabe> mAufgaben;
    private UUID mRubrikId;


    // Listener
    AufgabenListe_Listener mListener;

    // Analytics
    Tracker mTracker;

    // SharedPreferences
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;

    // Premium
    private boolean mPremium;
    private boolean mTestVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // SharedPreferences
        mSharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mSharedPreferencesEditor = mSharedPreferences.edit();

        // inflate View
        View layout = inflater.inflate(R.layout.fragment_aufgabenliste, container, false);

        // initialize Layout Components
        rvAufgabenListe = (RecyclerView) layout.findViewById(R.id.rvAufgabenliste);
        fabAdd = (FloatingActionButton) layout.findViewById(R.id.fabAddAufgabe);

        // get Arguments
        Bundle bundle = getArguments();
        ParcelUuid parcelUuid = bundle.getParcelable(Constants.ID_RUBRIK);
        mRubrikId = parcelUuid.getUuid();
        mPremium = bundle.getBoolean(Constants.FRAGMENT_PREMIUM);
        mTestVersion = bundle.getBoolean(Constants.FRAGMENT_TEST_VERSION);

        // set LayoutManager for RecyclerView
        rvAufgabenListe.setLayoutManager(new LinearLayoutManager(getActivity()));

        // get Rubrik and Aufgaben
        loadData();

        // set Toolbar Title
        //mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());
        mListener.onSetToolbarTitle(mRubrik.getTitle(), -1);

        // set ClickListener
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("AufgabenListe")
                        .setAction("Taks added")
                        .build());

                if (mPremium) {
                    createNewTask();
                } else {
                    if (mTestVersion) {
                        int numberOfTasks = mSharedPreferences.getInt(Constants.NUMBER_OF_TASKS_CREATED, 0);
                        Log.i("NumberOfTasks: ", Integer.toString(numberOfTasks));
                        numberOfTasks++;
                        mSharedPreferencesEditor.putInt(Constants.NUMBER_OF_TASKS_CREATED, numberOfTasks);
                        mSharedPreferencesEditor.commit();

                        int numberOfFreeTasks = getActivity().getResources().getInteger(R.integer.test_version_tasks);

                        if (numberOfTasks == numberOfFreeTasks) {

                                //tell User that Premium is over within Dialog, let him get Premium
                                DialogFragment dialog = new Dialog_TestVersion_Limit();
                                String title = getActivity().getResources().getString(R.string.dialog_premiumOver_title);
                                String description = getActivity().getResources().getString(R.string.dialog_premiumOver_description);
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.TITLE, title);
                                bundle.putString(Constants.DESCRIPTION, description);
                                dialog.setArguments(bundle);
                                dialog.setTargetFragment(AufgabenListe.this, 0);
                                dialog.show(getActivity().getSupportFragmentManager(), "dialog_getPremium");

                        } else {
                            if (numberOfTasks == 15 || numberOfTasks == 20 || numberOfTasks == 25) {
                                String stringOne = getActivity().getString(R.string.test_version_is_running_one);
                                int leftTasks = numberOfFreeTasks - numberOfTasks - 1;
                                String stringTwo = getActivity().getString(R.string.test_version_is_running_two);
                                String descreption = stringOne+" "+Integer.toString(leftTasks)+" "+stringTwo;
                                Dialog_TestVersion_RunningOut dialog = new Dialog_TestVersion_RunningOut();
                                Bundle b = new Bundle();
                                b.putString(Constants.DESCRIPTION, descreption);
                                dialog.setTargetFragment(AufgabenListe.this, 0);
                                dialog.setArguments(b);
                                dialog.show(getActivity().getSupportFragmentManager(), "d_runningout");

                                //Toast.makeText(getActivity(), getActivity().getString(R.string.test_version_is_running), Toast.LENGTH_LONG).show();
                                //createNewTask();

                        } else {
                                createNewTask();
                            }
                        }
                    } else {
                        createNewTask();
                    }
                }



            }
        });


        // set Colors
        ColorTheme colorTheme = new ColorTheme(getActivity());

        // initialize GroundHeader
        rlHeaderGround = (RelativeLayout) layout.findViewById(R.id.rlAppInfo);
        rlHeaderGround.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimaryLight()));

        fabAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary())));


        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        //RubrikLab.get(this).saveRubriken();
        mRubrik.saveAufgaben();

    }

    @Override
    public void onResume() {
        super.onResume();
        // get Rubrik and Aufgaben
        loadData();
        // set up Adapter and RecyclerView
        loadRecyclerView();
        //mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());
        mListener.onSetToolbarTitle(mRubrik.getTitle(), -1);
        mListener.onUpdate();

        mTracker.setScreenName("Image~" + "AufgabenListe");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (AufgabenListe_Listener) context;

    }

    @Override
    public void onAufgabenItemSelected(UUID aufgabenId) {
        Aufgabe aufgabe = mRubrik.getAufgabe(aufgabenId);

        Intent i = new Intent(getActivity(), AufgabeErstellen.class);
        i.putExtra(Constants.ID_AUFGABE, aufgabe.getId());
        i.putExtra(Constants.ID_RUBRIK, mRubrik.getId());
        startActivity(i);

    }

    @Override
    public void onAufgabenItemDelete(UUID aufgabenId) {
        DialogFragment dialog = new Dialog_Aufgabe_Check();
        dialog.setTargetFragment(this, 0);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ID_AUFGABE, aufgabenId.toString());
        //bundle.putBoolean(OVERVIEW, false);
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(), "dialog_check_aufgabe");
    }

    @Override
    public void onPrioritaetUp(UUID aufgabenId) {
        Aufgabe aufgabe = mRubrik.getAufgabe(aufgabenId);
        int prioritaet = aufgabe.getPrioritaet();
        if (prioritaet == 1) {
            aufgabe.setPrioritaet(3);
        } else {
            aufgabe.setPrioritaet(prioritaet - 1);
        }
        mRubrik.saveAufgaben();
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                loadRecyclerView();
            }
        }, getActivity().getResources().getInteger(R.integer.length_update_priorityUP_dateIconLongClick));
    }

    @Override
    public void onDeadlineLongClick(UUID aufgabenId) {
        Aufgabe aufgabe = mRubrik.getAufgabe(aufgabenId);

        // open Calendar
        if (aufgabe.getDeadline() != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.JAHR_AUFGABE, aufgabe.getDeadline().get(Calendar.YEAR));
            bundle.putInt(Constants.MONAT_AUFGABE, aufgabe.getDeadline().get(Calendar.MONTH));
            bundle.putInt(Constants.TAG_AUFGABE, aufgabe.getDeadline().get(Calendar.DAY_OF_MONTH));
            bundle.putString(Constants.ID_AUFGABE, aufgabenId.toString());

            DialogFragment newFragment = new Dialog_DatePicker();
            newFragment.setArguments(bundle);
            newFragment.setTargetFragment(this, 0);
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        } else {
            Bundle bundle = new Bundle();
            Calendar calendar = Calendar.getInstance();
            bundle.putInt(Constants.JAHR_AUFGABE, calendar.get(Calendar.YEAR));
            bundle.putInt(Constants.MONAT_AUFGABE, calendar.get(Calendar.MONTH));
            bundle.putInt(Constants.TAG_AUFGABE, calendar.get(Calendar.DAY_OF_MONTH));
            bundle.putString(Constants.ID_AUFGABE, aufgabenId.toString());
            DialogFragment newFragment = new Dialog_DatePicker();
            newFragment.setTargetFragment(this, 0);
            newFragment.setArguments(bundle);
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        }

    }

    @Override
    public void onAufgabenItemDelete2(String uuid) {
        Aufgabe aufgabe = mRubrik.getAufgabe(UUID.fromString(uuid));
        mRubrik.deleteAufgabe(aufgabe);
        mRubrik.saveAufgaben();
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
        loadRecyclerView();
        //drawerFragment.onAufgabeDeleted();
        //mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());
        mListener.onSetToolbarTitle(mRubrik.getTitle(), -1);
        mListener.onUpdate();
    }

    @Override
    public void onAufgabenRefresh() {
        loadRecyclerView();
    }

    private void loadRecyclerView() {

        // set up Adapter and RecyclerView
        mAdapter = new Adapter_AufgabenListe(getActivity(), mAufgaben, false, this, mPremium, mTestVersion); // false indicates that we do not have an overview here
        rvAufgabenListe.setAdapter(mAdapter);

    }

    private void loadData() {

        // get Rubrik
        mRubrik = RubrikLab.get(getActivity()).getRubrik(mRubrikId);

        // check if Rubrik is connected
        //Boolean rubrikIsConnected = mRubrik.isConnected();

        // not connected, therefore the data is only saved locally on the device
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());

        // Set up RecyclerView
        loadRecyclerView();

    }


    public void onAufgabenSort() {
        if (mRubrik.isSortByDate()) {
            mRubrik.setSortByDate(false);
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Prioritaet), Toast.LENGTH_SHORT).show();
        } else {
            mRubrik.setSortByDate(true);
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Deadline), Toast.LENGTH_SHORT).show();
        }
        //mRubrik.saveAufgaben();

        //loadData();
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
        loadRecyclerView();

    }


    @Override
    public void onDateSelected(int year, int month, int day, String uuid) {
        Aufgabe aufgabe = mRubrik.getAufgabe(UUID.fromString(uuid));
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        aufgabe.setDeadline(cal);

        //update
        mRubrik.saveAufgaben();
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                loadRecyclerView();
            }
        }, getActivity().getResources().getInteger(R.integer.length_update_priorityUP_dateIconLongClick));
        mListener.onUpdate();
    }

    @Override
    public void onTestVersionLimit_Positive() {
        // start InAppPurchase
        mListener.onStartInAppPurchase();
    }

    @Override
    public void onTestVersionLimit_Negative() {
        createNewTask();

    }

    @Override
    public void onCreateAufgabe() {
        createNewTask();
    }

    public interface AufgabenListe_Listener {
        public void onSetToolbarTitle(String title, int Anzahl_Aufgaben);
        public void onStartInAppPurchase();

        public void onUpdate();
    }


    // AsyncTask to load the whole Data from the Rubrik
    class LoadRubrikFromServer extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {


            HashMap<String, String> map = new HashMap<String, String>();
            map.put("RUBRIK_ID", mRubrik.getId().toString());
            String response = "";

            try {
                URL url = new URL("http://provelopment-server.de/Primelist/loadData_Rubrik.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(map));

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
                Log.i("Response: ", response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Option 2 -- JSONArray
            try {
                JSONObject jObjPost = new JSONObject(response);
                int Response_Code = jObjPost.getInt("result");

                if (Response_Code == 1) {

                    //set Rubrik Information
                    mRubrik.setTitle(jObjPost.getString("rubrikName"));

                    // delete all Aufgaben in Rubrik
                    mRubrik.deleteAufgabenArrayList();

                    // get AufgabenArray
                    JSONArray aufgabenArray = jObjPost.getJSONArray("aufgabenArray");

                    for (int i = 0; i < aufgabenArray.length(); i++) {
                        JSONObject jsonObject = aufgabenArray.getJSONObject(i);
                        Aufgabe aufgabe = new Aufgabe(jsonObject.getString("rubrikName"));
                        aufgabe.setId(UUID.fromString(jsonObject.getString("uuid")));
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
                            Teilaufgabe teilaufgabe = new Teilaufgabe(getActivity());
                            teilaufgabe.setId(UUID.fromString(jsonObject2.getString("uuid")));
                            teilaufgabe.setTitle(jsonObject2.getString("title"));
                            teilaufgabe.setDone(jsonObject2.getBoolean("done"));
                            aufgabe.addTeilaufgabe(teilaufgabe);
                        }

                        mRubrik.addAufgabe(aufgabe);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            // save everything
            mRubrik.saveAufgaben();

            // set up Aufgben
            mAufgaben = mRubrik.getAufgabenArrayList(getActivity());

            // Set up RecyclerView
            loadRecyclerView();

        }
    }

    // Needed Helper Method for the AsyncTask
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Log.i("Code Username: ", result.toString());
        return result.toString();
    }

    private void createNewTask() {
        Aufgabe aufgabe = new Aufgabe(mRubrik.getTitle());
        // get current Version (done throught constant updates) and update it with new Aufgabe
        mRubrik.addAufgabe(aufgabe);
        mRubrik.saveAufgaben();

        // update Rubrik on Server
                /*
                Intent s = new Intent (getActivity(), UpdateRubrik.class);
                getActivity().startService(s);
                */

        Intent i = new Intent(getActivity(), AufgabeErstellen.class);
        i.putExtra(Constants.ID_AUFGABE, aufgabe.getId());
        i.putExtra(Constants.ID_RUBRIK, mRubrik.getId());
        startActivity(i);
    }
}
