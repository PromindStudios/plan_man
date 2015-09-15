package com.ericschumacher.eu.provelopment.android.planman.Fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Adapter_AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Check;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmReceiver;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constans;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Sorter;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by eric on 27.08.2015.
 */
public class AufgabenUebersicht extends Fragment implements Adapter_AufgabenListe.AufgabenAdapter_Listener, Dialog_Aufgabe_Check.DialogAufgabeDelete_Listener, Main.Main_Listener{

    // Layout Components
    private RecyclerView rvAufgabenUebersicht;
    private FloatingActionButton fabAdd;
    private Adapter_AufgabenListe mAdapter;

    // Data Components
    private Rubrik mRubrik;
    private ArrayList<Aufgabe> mAufgaben;
    private UUID mRubrikId;
    private boolean mOverviewSortedByDate;

    // Listener
    private Main mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate View
        View layout = inflater.inflate(R.layout.fragment_aufgabenuebersicht, container, false);

        // initialize Layout Components
        rvAufgabenUebersicht = (RecyclerView)layout.findViewById(R.id.rvAufgabenliste);
        fabAdd = (FloatingActionButton)layout.findViewById(R.id.fabAddAufgabe);
        fabAdd.setVisibility(View.INVISIBLE);

        // load Shared Preferences
        SharedPreferences settings = getActivity().getSharedPreferences(Constans.SHARED_PREFERENCES, 0);
        mOverviewSortedByDate = settings.getBoolean(Constans.SP_OVERVIEW_SORT, true);

        // get all Aufgaben
        loadData();

        // set Toolbar Title
        mListener.setToolbarTitle_Uebersicht(getActivity().getString(R.string.overview), mAufgaben.size());

        // sort Aufgaben
        Sorter sorter = new Sorter();
        if (mOverviewSortedByDate) {
            mAufgaben = sorter.sortAufgaben_ByDate(mAufgaben);
        } else {
            mAufgaben = sorter.sortAufgaben_ByPriority(mAufgaben);
        }

        // load RecyclerView
        loadRecyclerView();
        rvAufgabenUebersicht.setLayoutManager(new LinearLayoutManager(getActivity()));

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Main)activity;
    }

    @Override
    public void onPause() {
        super.onPause();
        ArrayList<Rubrik> rubriken = RubrikLab.get(getActivity()).getRubriken();
        for (Rubrik r : rubriken) {
            r.saveAufgaben();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadRecyclerView();
        mListener.setToolbarTitle_Uebersicht(getActivity().getString(R.string.overview), mAufgaben.size());
        mListener.onUpdate();
    }



    private void loadData() {
        // load all Aufgaben
        mAufgaben = new ArrayList<Aufgabe>();
        ArrayList<Rubrik> alleRubriken;
        alleRubriken = RubrikLab.get(getActivity()).getRubriken();
        for (Rubrik r : alleRubriken) {
            ArrayList<Aufgabe> aufgaben = r.getAufgabenArrayList(getActivity());
            for (Aufgabe a : aufgaben) {
                a.setRubrikName(r.getTitle());
                mAufgaben.add(a);
            }
        }
        SharedPreferences settings = getActivity().getSharedPreferences(Constans.SHARED_PREFERENCES_UEBERSICHT, 0);
        if (settings.getBoolean(Constans.SORTED_BY_DATE_UEBERSICHT, true)) {
            mAufgaben = new Sorter().sortAufgaben_ByDate(mAufgaben);
        } else {
            mAufgaben = new Sorter().sortAufgaben_ByPriority(mAufgaben);
        }
    }

    private void loadRecyclerView () {
        // set up Adapter and RecyclerView
        mAdapter = new Adapter_AufgabenListe(getActivity(), mAufgaben, true, this); // true indicates that we have an overview here
        rvAufgabenUebersicht.setAdapter(mAdapter);

    }


    public void onAufgabenSort() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constans.SHARED_PREFERENCES_UEBERSICHT, 0);
        if (settings.getBoolean(Constans.SORTED_BY_DATE_UEBERSICHT, true)) {
            mAufgaben = new Sorter().sortAufgaben_ByPriority(mAufgaben);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constans.SORTED_BY_DATE_UEBERSICHT, false);
            editor.commit();
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Prioritaet), Toast.LENGTH_SHORT).show();

        } else {
            mAufgaben = new Sorter().sortAufgaben_ByDate(mAufgaben);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constans.SORTED_BY_DATE_UEBERSICHT, true);
            editor.commit();
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Deadline), Toast.LENGTH_SHORT).show();
        }
        loadRecyclerView();

    }

    @Override
    public void onAufgabenItemSelected(UUID aufgabenId) {
        Aufgabe aufgabe = null;
        Rubrik rubrik = null;
        ArrayList<Rubrik> rubriken = RubrikLab.get(getActivity()).getRubriken();
        for (Rubrik r : rubriken) {
            aufgabe = r.getAufgabe(aufgabenId);
            if (aufgabe != null) {
                rubrik = r;
                break;
            }
        }

        // start Activity AufgabeErstellen
        Intent i = new Intent(getActivity(), AufgabeErstellen.class);
        if (aufgabe != null && rubrik != null) {
            i.putExtra(Constans.ID_AUFGABE, aufgabe.getId());
            i.putExtra(Constans.ID_RUBRIK, rubrik.getId());
            startActivity(i);
        } else {
            Log.i("NULL: ", "Aufgabe oder Rubrik");
        }
    }

    @Override
    public void onAufgabenItemDelete(UUID aufgabenId) {
        DialogFragment dialog = new Dialog_Aufgabe_Check();
        dialog.setTargetFragment(this, 0);
        Bundle bundle = new Bundle();
        bundle.putString(Constans.ID_AUFGABE, aufgabenId.toString());
        //bundle.putBoolean(OVERVIEW, false);
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(), "dialog_check_aufgabe");
    }

    @Override
    public void onAufgabenItemDelete2(String uuid) {

        UUID aufgabenId = UUID.fromString(uuid);
        Aufgabe aufgabe = null;
        Rubrik rubrik = null;
        ArrayList<Rubrik> rubriken = RubrikLab.get(getActivity()).getRubriken();
        for (Rubrik r : rubriken) {
            aufgabe = r.getAufgabe(aufgabenId);
            if (aufgabe != null) {
                rubrik = r;
                break;
            }
        }

        rubrik.deleteAufgabe(aufgabe);
        rubrik.saveAufgaben();
        loadData();
        loadRecyclerView();
        //drawerFragment.onAufgabeDeleted();
        mListener.setToolbarTitle_Uebersicht(getActivity().getString(R.string.overview), mAufgaben.size());
        mListener.onUpdate();
    }

    @Override
    public void onAufgabenRefresh() {
        loadData();
        loadRecyclerView();
    }

    public interface AufgabenUebersicht_Listener {
        public void onSetToolbarTitle(String title, int anzahl_aufgaben);
    }
}
