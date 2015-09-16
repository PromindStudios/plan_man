package com.ericschumacher.eu.provelopment.android.planman.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Adapter_AufgabenListe;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Check;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AnalyticsApplication;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constans;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 27.08.2015.
 */
public class AufgabenListe extends Fragment implements Adapter_AufgabenListe.AufgabenAdapter_Listener, Dialog_Aufgabe_Check.DialogAufgabeDelete_Listener{

    // Layout Components
    private RecyclerView rvAufgabenListe;
    private FloatingActionButton fabAdd;
    private Adapter_AufgabenListe mAdapter;
    private RelativeLayout rlHeaderGround;

    // Data Components
    private Rubrik mRubrik;
    private ArrayList<Aufgabe> mAufgaben;
    private UUID mRubrikId;

    // Colors
    int mColorPrimary;
    int mColorPrimaryDark;
    int mColorPrimaryLight;
    ColorStateList mColorStateListPrimary;
    ColorStateList mColorStateListPrimaryDark;
    ColorStateList mColorStateListPrimaryLight;

    // Listener
    AufgabenListe_Listener mListener;

    // Analytics
    Tracker mTracker;

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
        // inflate View
        View layout = inflater.inflate(R.layout.fragment_aufgabenliste, container, false);

        // initialize Layout Components
        rvAufgabenListe = (RecyclerView)layout.findViewById(R.id.rvAufgabenliste);
        fabAdd = (FloatingActionButton)layout.findViewById(R.id.fabAddAufgabe);

        // get Arguments
        Bundle bundle = getArguments();
        ParcelUuid parcelUuid = bundle.getParcelable(Constans.ID_RUBRIK);
        mRubrikId = parcelUuid.getUuid();

        // get Rubrik and Aufgaben
        loadData();

        // set Toolbar Title
        mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());

        // Set up RecyclerView
        loadRecyclerView();
        rvAufgabenListe.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set ClickListener
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("AufgabenListe")
                        .setAction("Taks added")
                        .build());

                Aufgabe aufgabe = new Aufgabe(mRubrik.getTitle());
                mRubrik.addAufgabe(aufgabe);
                Intent i = new Intent(getActivity(), AufgabeErstellen.class);
                i.putExtra(Constans.ID_AUFGABE, aufgabe.getId());
                i.putExtra(Constans.ID_RUBRIK, mRubrik.getId());
                startActivity(i);
            }
        });

        // set Colors
        //setColors();

        // initialize GroundHeader
        rlHeaderGround = (RelativeLayout)layout.findViewById(R.id.rlAppInfo_);

        /*
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            Log.i("FAB: ", "tinted");
            fabAdd.setBackgroundTintList(mColorStateListPrimary);
            rlHeaderGround.setBackgroundColor(mColorPrimaryLight);
        }
        */

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
        mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());
        mListener.onUpdate();

        mTracker.setScreenName("Image~" + "AufgabenListe");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AufgabenListe_Listener)activity;

    }

    @Override
    public void onAufgabenItemSelected(UUID aufgabenId) {
        Aufgabe aufgabe = mRubrik.getAufgabe(aufgabenId);

        Intent i = new Intent(getActivity(), AufgabeErstellen.class);
        i.putExtra(Constans.ID_AUFGABE, aufgabe.getId());
        i.putExtra(Constans.ID_RUBRIK, mRubrik.getId());
        startActivity(i);

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
            Aufgabe aufgabe = mRubrik.getAufgabe(UUID.fromString(uuid));
            mRubrik.deleteAufgabe(aufgabe);
            mRubrik.saveAufgaben();
            mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
            loadRecyclerView();
            //drawerFragment.onAufgabeDeleted();
            mListener.onSetToolbarTitle(mRubrik.getTitle(), mAufgaben.size());
            mListener.onUpdate();
    }

    @Override
    public void onAufgabenRefresh() {
        loadRecyclerView();
    }

    private void loadRecyclerView () {

        // set up Adapter and RecyclerView
        mAdapter = new Adapter_AufgabenListe(getActivity(), mAufgaben, false, this); // false indicates that we do not have an overview here
        rvAufgabenListe.setAdapter(mAdapter);

    }

    private void loadData() {
        // get Rubrik and Aufgaben
        mRubrik = RubrikLab.get(getActivity()).getRubrik(mRubrikId);
        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
    }



    public void onAufgabenSort() {
        if (mRubrik.isSortByDate()) {
            mRubrik.setSortByDate(false);
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Prioritaet), Toast.LENGTH_SHORT).show();
        } else {
            mRubrik.setSortByDate(true);
            Toast.makeText(getActivity(), getString(R.string.Toast_sortieren_Deadline), Toast.LENGTH_SHORT).show();
        }

        mAufgaben = mRubrik.getAufgabenArrayList(getActivity());
        loadRecyclerView();

    }

    private void setColors() {
        ColorTheme colorTheme = new ColorTheme(getActivity());
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

    public interface AufgabenListe_Listener {
        public void onSetToolbarTitle(String title, int Anzahl_Aufgaben);
        public void onUpdate();
    }
}
