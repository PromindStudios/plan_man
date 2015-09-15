package com.ericschumacher.eu.provelopment.android.planman.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Adapter_AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Check;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Add;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.AufgabenUebersicht;
import com.ericschumacher.eu.provelopment.android.planman.Fragments.RubrikListe;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmReceiver;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmSetter;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constans;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikAdapter;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;


public class Main extends AppCompatActivity implements Dialog_Rubrik_Add.DialogListener, Adapter_AufgabenListe.AufgabenAdapterListener, Dialog_Aufgabe_Check.CheckAufgabenListener,
        AufgabeErstellen.BackButtonListener, RubrikAdapter.RubrikAdapter_Listener, RubrikListe.RubrikListe_Listener,
        AufgabenListe.AufgabenListe_Listener {

    private static final String PREFS_NAME = "AufgabenListe.SP";
    private static final String OVERVIEW_SORT = "OverView_Sort";

    // Layout Components
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    // Contents
    UUID rubrikId;
    Rubrik mRubrik;
    RubrikLab mRubrikLab;
    private ArrayList<Aufgabe> mAufgaben = new ArrayList<Aufgabe>();
    private ArrayList<Aufgabe> mAlleAufgaben = new ArrayList<Aufgabe>();
    private String FILENAME;
    private String mFilename_Aufgaben;
    public static final String UUID_AUFGABE = "UUID_AUFGABE";
    RubrikListe drawerFragment;
    private Boolean mOverViewSortedByDate;
    SharedPreferences settings;

    // Listener
    Main_Listener mListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize Layout
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        // get instance of RubrikLab;
        mRubrikLab = RubrikLab.get(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        settings = getSharedPreferences(PREFS_NAME, 0);
        mOverViewSortedByDate = settings.getBoolean(OVERVIEW_SORT, true);

        drawerFragment = (RubrikListe) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerFragment.setUp(drawerLayout, toolbar);


        // Start Fragment: AufgabenUebersicht
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AufgabenUebersicht aufgabenUebersicht = new AufgabenUebersicht();
        fragmentTransaction.replace(R.id.flActivityMain, aufgabenUebersicht);
        mCurrentFragment_isOverview = true;
        fragmentTransaction.commit();

        // open Navigation Drawer when Activity starts in the beginning
        if (!getIntent().getBooleanExtra(Constans.NOTIFACTION_INTENT, false)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }, 50);
        }

        // set Colors
        //setColors();

        // set Alarm
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);;
        mAlarmSet = settings.getBoolean(Constans.SP_ALARM_SET, false);


        if (!mAlarmSet) {
            AlarmSetter alarmSetter = new AlarmSetter();
            alarmSetter.setAlarm(this);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constans.SP_ALARM_SET, true);
            editor.commit();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

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

            if (mCurrentFragment_isOverview) {
                AufgabenUebersicht fragment = (AufgabenUebersicht) getSupportFragmentManager().findFragmentById(R.id.flActivityMain);
                fragment.onAufgabenSort();
            } else {
                AufgabenListe fragment = (AufgabenListe) getSupportFragmentManager().findFragmentById(R.id.flActivityMain);
                fragment.onAufgabenSort();
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
        Rubrik rubrik = new Rubrik(titel);
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
        bundle.putParcelable(Constans.ID_RUBRIK, new ParcelUuid(rubrikId));

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
    public void onSetToolbarTitle(String title, int anzahl_aufgaben) {
        getSupportActionBar().setTitle(title);
        if (anzahl_aufgaben == 1) {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben)+" "+getString(R.string.Task));
        } else {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben)+" "+getString(R.string.Tasks));
        }
    }

    @Override
    public void onUpdate() {
        drawerFragment.onUpdate();
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
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben)+" "+getString(R.string.Task));
        } else {
            getSupportActionBar().setSubtitle(Integer.toString(anzahl_aufgaben)+" "+getString(R.string.Tasks));
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

    private void setColors() {
        ColorTheme colorTheme = new ColorTheme(this);
        mColorPrimary = colorTheme.getColorPrimary();
        mColorPrimaryLight = colorTheme.getColorPrimaryLight();
        mColorPrimaryDark = colorTheme.getColorPrimaryDark();

        mColorStateListPrimary = colorTheme.getColorPrimary_ColorStateList();
        mColorStateListPrimaryDark = colorTheme.getColorPrimaryDark_ColorStateList();
        mColorStateListPrimaryLight = colorTheme.getColorPrimaryLight_ColorStateList();

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };
        int[] colorsPrimary = new int[] {
                mColorPrimary
        };

        int[] colorsPrimaryDark = new int[] {
                mColorPrimary
        };

        int[] colorsPrimaryLight = new int[] {
                mColorPrimary
        };
    }




}
