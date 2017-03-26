package com.ericschumacher.eu.provelopment.android.planman.Activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.AppWidget.MyAppWidgetProvider;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Adapter_AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Check;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Get_Premium;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Add;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.AufgabenUebersicht;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.RubrikListe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmSetter;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabHelper;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabResult;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.Inventory;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.Purchase;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikAdapter;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


public class Main extends AppCompatActivity implements Dialog_Rubrik_Add.DialogListener, Adapter_AufgabenListe.AufgabenAdapterListener, Dialog_Aufgabe_Check.CheckAufgabenListener,
        AufgabeErstellen.BackButtonListener, RubrikAdapter.RubrikAdapter_Listener, RubrikListe.RubrikListe_Listener,
        AufgabenListe.AufgabenListe_Listener, Dialog_Get_Premium.PremiumListener{

    private static final String PREFS_NAME = "AufgabenListe.SP";
    private static final String OVERVIEW_SORT = "OverView_Sort";

    // Layout Components
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    // Contents
    UUID rubrikId;
    Rubrik mRubrik;
    RubrikLab mRubrikLab;
    private ArrayList<Aufgabe> mAufgaben = new ArrayList<Aufgabe>();      // lies mich
    private ArrayList<Aufgabe> mAlleAufgaben = new ArrayList<Aufgabe>();
    private String FILENAME;
    private String mFilename_Aufgaben;
    public static final String UUID_AUFGABE = "UUID_AUFGABE";
    RubrikListe drawerFragment;
    private Boolean mOverViewSortedByDate;
    SharedPreferences settings;
    int mUserId;

    // Listener
    Main_Listener mListener;

    // Intent
    Intent intentCheckForChanges;

    // Fragment
    private boolean mCurrentFragment_isOverview;

    // Constants
    public static final String RUBRIK_ID = "rubrik.RUBRIK_ID";
    public static final String AUFGABE_ID = "aufgabe.AUFGABE_ID";
    public static final String SHOW_OVERVIEW = "aufgabenliste.show_overview";
    public static final String OVERVIEW = "aufgabenListe.overview_check_aufgabe";

    // Colors
    int mColorPrimary;
    int mColorPrimaryDark;
    int mColorPrimaryLight;
    ColorStateList mColorStateListPrimary;
    ColorStateList mColorStateListPrimaryDark;
    ColorStateList mColorStateListPrimaryLight;

    // Alarm
    private boolean mAlarmSet;

    // InAppPurchase
    IabHelper mHelper;
    boolean mPremium;
    boolean mTestVersion;

    // SharedPreferences
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if User has already an Id
        /*
        settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mUserId = settings.getInt(Constants.USER_ID, -1);

        if (mUserId < 0 && InternetChecker.isConnected(this)) {
            // User has no Id yet
            new CreateUser().execute();
        }
        */

        // check if user has Premium!
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mSharedPreferencesEditor = mSharedPreferences.edit();
        mPremium = mSharedPreferences.getBoolean(Constants.USER_HAS_PREMIUM, false);

        if (mPremium) {
            mTestVersion = false;
            Log.i("Premium_Main: ", "true");
        } else {
            Log.i("Premium_Main: ", "false");
            int number_of_tasks = mSharedPreferences.getInt(Constants.NUMBER_OF_TASKS_CREATED, 0);
            if (number_of_tasks < getResources().getInteger(R.integer.test_version_tasks)) {
                mTestVersion = true;
                Log.i("Test-Version_Main: ", "true");
            } else {
                mTestVersion = false;
                Log.i("Test-Version_Main: ", "false");
            }
            // start Query to Google Play, to check if User has Premium

            // InAppPurchase
            String beat = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjCT9zyzV/Vx58WIOUuLPQUVKLeZopUyuC1ngh4m74lcd0IR7TJLls52Q";
            String getup = "Rxab6FM1t4Qh/KuZ3K3+9JSb0bSchJaE9JPaqoSn7qQb1j4IfeReJkC2YkJ6Vjwc9Io4iOhhKyG4uEFYHl/dc8NX89xwYcFAwmJ6";
            String grind = Constants.GRIND;
            String rise = Constants.RISE;

            mHelper = new IabHelper(this, beat + getup + grind + rise);

            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Log.d("Iab: Problem", "Problem setting up In-app Billing: " + result);
                    } else {
                        Log.i("StartSetup", "Finished");
                        //get Information about available Products
                        ArrayList<String> additionalSkuList = new ArrayList<String>();
                        additionalSkuList.add("primelist_update1");
                        mHelper.queryInventoryAsync(mGotInventoryListener);

                    }
                }
            });

        }

        //initialize Layout
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // get instance of RubrikLab;
        mRubrikLab = RubrikLab.get(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences settings_2 = getSharedPreferences(PREFS_NAME, 0);
        mOverViewSortedByDate = settings_2.getBoolean(OVERVIEW_SORT, true);

        drawerFragment = (RubrikListe) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerFragment.setUp(drawerLayout, toolbar);


        // Start Fragment: AufgabenUebersicht
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AufgabenUebersicht aufgabenUebersicht = new AufgabenUebersicht();
        Bundle uebersicht_bundle = new Bundle();
        uebersicht_bundle.putBoolean(Constants.FRAGMENT_PREMIUM, mPremium);
        uebersicht_bundle.putBoolean(Constants.FRAGMENT_TEST_VERSION, mTestVersion);
        aufgabenUebersicht.setArguments(uebersicht_bundle);
        fragmentTransaction.replace(R.id.flActivityMain, aufgabenUebersicht);
        mCurrentFragment_isOverview = true;
        fragmentTransaction.commit();

        // open Navigation Drawer when Activity starts in the beginning
        if (!getIntent().getBooleanExtra(Constants.NOTIFACTION_INTENT, false)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }, 50);
        }

        // set Colors
        ColorTheme colorTheme = new ColorTheme(this);

        // colorComponents
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorTheme.getColorPrimary()));

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, colorTheme.getColorPrimaryDark()));
        }

        // set Alarm
        settings = getSharedPreferences(PREFS_NAME, 0);

        mAlarmSet = settings.getBoolean(Constants.SP_ALARM_SET, false);

        if (!mAlarmSet) {
            AlarmSetter alarmSetter = new AlarmSetter();
            alarmSetter.setAlarm(this);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.SP_ALARM_SET, true);
            editor.commit();
        }

    }

    // create mPurchaseFinishedListener
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d("Error", "Error purchasing: " + result);
                return;
            } else {
                Log.i("Purchase: ", "Finished!!");
                mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, true);
                mSharedPreferencesEditor.commit();
                Intent intent = new Intent(Main.this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                // give user access to premium content and update the UI
            }
        }
    };

    // create mGotInventoryListener
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {
                // handle error here
            } else {
                // does the user have the premium upgrade?
                Boolean mIsPremium = inventory.hasPurchase("primelist_update1");
                if (mIsPremium) {
                    Log.i("Premium: ", "true - QueryInventory");
                    mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, true);
                    mSharedPreferencesEditor.commit();
                    mPremium = true;
                    Log.i("Update?: ", "User hat Update auf dem Handy!!");
                } else {
                    Log.i("Premium: ", "false - QueryInventory");
                    mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, false);
                    mSharedPreferencesEditor.commit();
                    mPremium = false;
                    Log.i("Update?: ", "User hat kein Premium");
                }

                // update UI accordingly
            }
        }
    };

    //create mQueryFinishedListener
    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                Log.i("Error", "Produkt nicht gefunden!");
                return;
            }

            String primelist_update1 = inventory.getSkuDetails("primelist_update1").getPrice();

            Log.i("Price: ", primelist_update1);

        }
    };




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Main: ", "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.i("Main: ", "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    protected void onPostResume() {

        try {
            super.onPostResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName name = new ComponentName(this, MyAppWidgetProvider.class);
        int [] ids = appWidgetManager.getAppWidgetIds(name);
        int length = ids.length;
        for (int i=0; i<length; i++) {
            appWidgetManager.notifyAppWidgetViewDataChanged(ids[i], R.id.lvAppWidget);
            Log.i("Main: ", "Paused: Update App Widget");
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // check if user has update
        queryPurchasedItems();


        /*
        intentCheckForChanges = new Intent(this, CheckForChanges.class);
        startService(intentCheckForChanges);
        */

        // saveChanges
    }

    private void queryPurchasedItems() {
        if (mHelper != null && mHelper.isSetupDone() && !mHelper.isAsyncInProgress()) {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHelper != null) mHelper.dispose();
        mHelper = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aufgabenliste, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.home) {
            Toast.makeText(this, "Hinzufuegen", Toast.LENGTH_LONG).show();

            return true;
        }
        if (id == R.id.ic_sort) {

            if (!mTestVersion && !mPremium) {
                // Function is disabled  - Therefore a Dialog with be shown
                openDialog_FunctionDeactivated();


            } else {
                if (mCurrentFragment_isOverview) {
                    AufgabenUebersicht fragment = (AufgabenUebersicht) getSupportFragmentManager().findFragmentById(R.id.flActivityMain);
                    fragment.onAufgabenSort();
                } else {
                    AufgabenListe fragment = (AufgabenListe) getSupportFragmentManager().findFragmentById(R.id.flActivityMain);
                    fragment.onAufgabenSort();
                }
            }



            /*
            if (mRubrik != null) {
                if (mRubrik.isSortByDate()) {
                    mRubrik.setSortByDate(false);
                    Toast.makeText(this, getString(R.string.Toast_sortieren_Prioritaet), Toast.LENGTH_SHORT).show();
                } else {
                    mRubrik.setSortByDate(true);
                    Toast.makeText(this, getString(R.string.Toast_sortieren_Deadline), Toast.LENGTH_SHORT).show();
                }

                mAufgaben = mRubrik.getAufgabenArrayList(this);
                mAdapter = new Adapter_AufgabenListe(this, rubrikId, mAufgaben);
                mRecyclerView.setAdapter(mAdapter);
                return true;
            } else {
                if (getIntent().getBooleanExtra(SHOW_OVERVIEW, false)) {
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    mOverViewSortedByDate = settings.getBoolean(OVERVIEW_SORT, true);

                    if (mOverViewSortedByDate) {
                        editor.putBoolean(OVERVIEW_SORT, false);
                        editor.commit();
                        mAlleAufgaben = sortbyDate_for_Rubriken(mAlleAufgaben);
                        mAdapterOverview = new AufgabenOverViewAdapter(this, mAlleAufgaben);
                        mRecyclerView.setAdapter(mAdapterOverview);
                        Toast.makeText(this, getString(R.string.Toast_sortieren_Deadline), Toast.LENGTH_SHORT).show();
                    } else {
                        editor.putBoolean(OVERVIEW_SORT, true);
                        editor.commit();
                        mAlleAufgaben = sort(mAlleAufgaben);
                        mAdapterOverview = new AufgabenOverViewAdapter(this, mAlleAufgaben);
                        mRecyclerView.setAdapter(mAdapterOverview);
                        Toast.makeText(this, getString(R.string.Toast_sortieren_Prioritaet), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }

*/

        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialog_FunctionDeactivated() {
        DialogFragment dialog = new Dialog_Get_Premium();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TITLE, getString(R.string.dialog_getPremium_Title_Deaktiviert));
        bundle.putString(Constants.DESCRIPTION, getString(R.string.dialog_getPremium_Description_Deaktiviert));
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialog_check_aufgabe");
    }


    @Override
    public void onBackPressed() {
        // your code.

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
            Log.i("Drawer: ", "open");
        } else {

            drawerLayout.openDrawer(Gravity.LEFT);

            //drawerLayout.openDrawer(Gravity.LEFT);
            Log.i("Drawer: ", "closed");
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String titel) {
        Rubrik rubrik = new Rubrik(titel, this);
        RubrikLab.get(this).addRubrik(rubrik);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


    @Override
    public void onAufgabeClicked(UUID uuid) {
        Aufgabe aufgabe = mRubrik.getAufgabe(uuid);
/*
        Bundle bundle = new Bundle();
        bundle.putString(AUFGABE_ID, aufgabe.getId().toString());
        bundle.putString(RUBRIK_ID, mRubrik.getId().toString());

        DialogFragment newFragment = new Dialog_Aufgabe_Detail();
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "aufgabe_detail");

         */
        Intent i = new Intent(Main.this, AufgabeErstellen.class);
        i.putExtra(AufgabeErstellen.EXTRA_AUFGABE_ID, aufgabe.getId());
        i.putExtra(AufgabeErstellen.MY_RUBRIK_ID, mRubrik.getId());
        startActivityForResult(i, 0);

    }

    @Override
    public void onDeleteAufgabe(UUID uuid) {
        DialogFragment dialog = new Dialog_Aufgabe_Check();
        Bundle bundle = new Bundle();
        bundle.putString(UUID_AUFGABE, uuid.toString());
        bundle.putBoolean(OVERVIEW, false);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialog_check_aufgabe");
    }

    @Override
    public void checkAufgabe(String uuid, boolean overview) {

        /*
        Aufgabe aufgabe_o = null;
        Rubrik rubrik_o = null;
        if (!overview) {
            Aufgabe aufgabe = mRubrik.getAufgabe(UUID.fromString(uuid));

            Log.i("Titel: ", aufgabe.getTitle());
            mRubrik.deleteAufgabe(aufgabe);

            mRubrik.saveAufgaben();
            mAufgaben = mRubrik.getAufgabenArrayList(this);

            Log.i("Size_new: ", Integer.toString(mAufgaben.size()));
            mAdapter = new Adapter_AufgabenListe(this, rubrikId, mAufgaben);
            mRecyclerView.setAdapter(mAdapter);
            drawerFragment.onAufgabeDeleted();
        } else {

            for (Rubrik r : RubrikLab.get(this).getRubriken()) {
                rubrik_o = r;
                for (Aufgabe a : r.getAufgabenArrayList(this)) {
                    if (a.getId().equals(UUID.fromString(uuid)))
                        aufgabe_o = a;
                    break;
                }
            }

            if (aufgabe_o != null) {
                rubrik_o.deleteAufgabe(aufgabe_o);
                rubrik_o.saveAufgaben();
                ArrayList<Rubrik> alleRubriken;
                alleRubriken = RubrikLab.get(this).getRubriken();
                for (Rubrik r : alleRubriken) {
                    ArrayList<Aufgabe> aufgaben = r.getAufgabenArrayList(this);
                    for (Aufgabe a : aufgaben) {
                        mAlleAufgaben.add(a);
                    }
                }
                Log.i("Length_Aufgben", Integer.toString(mAlleAufgaben.size()));

                if (mOverViewSortedByDate) {
                    mAlleAufgaben = sortbyDate_for_Rubriken(mAlleAufgaben);
                } else {
                    mAlleAufgaben = sort(mAlleAufgaben);
                }

                Log.i("Size_new: ", Integer.toString(mAlleAufgaben.size()));
                mAdapterOverview = new AufgabenOverViewAdapter(this, mAlleAufgaben);
                mRecyclerView.setAdapter(mAdapterOverview);
                drawerFragment.onAufgabeDeleted();
            } else {
                Log.i("Aufgabe: ", "nicht gefunden");
            }

        }
        */


    }

    @Override
    public void checkAufgabeRefresh() {
        /*
        mAdapter = new Adapter_AufgabenListe(this, rubrikId, mAufgaben);
        mRecyclerView.setAdapter(mAdapter);
        */

    }

    @Override
    public void onBackButtonPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
            Log.i("Drawer: ", "open");
        } else {

            drawerLayout.openDrawer(Gravity.LEFT);

            Log.i("Drawer: ", "closed");
        }
    }


    @Override
    public void onRubrikItemSelected(UUID rubrikId) {

        // Create Bundle for Arguments
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ID_RUBRIK, new ParcelUuid(rubrikId));
        bundle.putBoolean(Constants.FRAGMENT_PREMIUM, mPremium);
        bundle.putBoolean(Constants.FRAGMENT_TEST_VERSION, mTestVersion);

        // Start Fragment: AufgabenListe
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AufgabenListe aufgabenListe = new AufgabenListe();
        aufgabenListe.setArguments(bundle);
        fragmentTransaction.replace(R.id.flActivityMain, aufgabenListe);
        mCurrentFragment_isOverview = false;
        fragmentTransaction.commit();

        // close Navigation Drawer after a while
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        }, 50);
    }

    @Override
    public void onSelectedOverview() {
        // Start Fragment: AufgabenUebersicht
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AufgabenUebersicht aufgabenUebersicht = new AufgabenUebersicht();
        Bundle uebersicht_bundle = new Bundle();
        uebersicht_bundle.putBoolean(Constants.FRAGMENT_PREMIUM, mPremium);
        uebersicht_bundle.putBoolean(Constants.FRAGMENT_TEST_VERSION, mTestVersion);
        aufgabenUebersicht.setArguments(uebersicht_bundle);
        fragmentTransaction.replace(R.id.flActivityMain, aufgabenUebersicht);
        mCurrentFragment_isOverview = true;
        fragmentTransaction.commit();

        // open Navigation Drawer when Activity starts in the beginning
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        }, 50);
    }

    @Override
    public void onInAppPurchase() {

        startInAppPurchase();

    }

    @Override
    public void onFunctionDeactivated() {
        openDialog_FunctionDeactivated();
    }

    @Override
    public void onSetToolbarTitle(String title, int anzahl_aufgaben) {
        getSupportActionBar().setTitle(title);
        if (anzahl_aufgaben == 1) {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben) + " " + getString(R.string.Task));

        } else {
            if (anzahl_aufgaben == -1) {
                getSupportActionBar().setSubtitle("");
            } else {
                getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben) + " " + getString(R.string.Tasks));
            }

        }
    }

    @Override
    public void onStartInAppPurchase() {
        startInAppPurchase();
    }

    @Override
    public void onUpdate() {
        drawerFragment.onUpdate();
    }

    @Override
    public void onPremiumSelected() {
        startInAppPurchase();
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

    public interface RubrikListeListener {
        public void onAufgabeDeleted();
    }

    public interface Main_Listener {
        public void onAufgabenSort();
    }

    public void setToolbarTitle_Uebersicht(String title, int anzahl_aufgaben) {
        getSupportActionBar().setTitle(title);
        if (anzahl_aufgaben == 1) {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben) + " " + getString(R.string.Task));
        } else {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben) + " " + getString(R.string.Tasks));
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

    public ArrayList<Aufgabe> sortbyDate_for_Rubriken(ArrayList<Aufgabe> Aufgaben) {
        ArrayList<Aufgabe> aufgaben = Aufgaben;
        Collections.sort(aufgaben, new CustomComparator_2());
        return aufgaben;

    }

    public class CustomComparator_2 implements Comparator<Aufgabe> {

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


    class CreateUser extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {


            String response = "";

            try {
                URL url = new URL("http://provelopment-server.de/Primelist/create_user.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                /*
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(map));

                writer.flush();
                writer.close();
                os.close();
                */

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
            try {
                mUserId = Integer.parseInt(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // Eric
        protected void onPostExecute(String file_url) {

            if (mUserId > 0) {
                settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(Constants.USER_ID, mUserId);
                editor.commit();
            }

        }
    }

    // AsyncTask to load the whole Data from the Rubrik
    class LoadRubrikFromServer extends AsyncTask<String, String, String> {


        Rubrik newRubrik;

        protected String doInBackground(String... params) {

            Log.i("Rubrik: ", "is loaded from Server");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("RUBRIK_ID", params[0]);
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
                    newRubrik = new Rubrik(jObjPost.getString("rubrikName"), getApplicationContext());
                    newRubrik.setId(UUID.fromString(jObjPost.getString("rubrikUUID")));
                    newRubrik.setIsConnected(true);
                    mRubrikLab.addRubrik(newRubrik);

                    // delete all Aufgaben in Rubrik
                    //newRubrik.deleteAufgabenArrayList();

                    // get AufgabenArray
                    JSONArray aufgabenArray = jObjPost.getJSONArray("aufgabenArray");

                    for (int i = 0; i < aufgabenArray.length(); i++) {
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

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {


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

    private void startInAppPurchase() {
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        int n = new Random().nextInt(10001);
        String s = UUID.randomUUID().toString();
        int number = settings.getInt(Constants.RESPONSE_CODE_PURCHASE, n);
        String text = settings.getString(Constants.UNIQUE_STRING_PURCHASE, s);

        try {
            mHelper.launchPurchaseFlow(this, "primelist_update1", number, mPurchaseFinishedListener, s);
        } catch (Exception e) {

        }
    }

}
