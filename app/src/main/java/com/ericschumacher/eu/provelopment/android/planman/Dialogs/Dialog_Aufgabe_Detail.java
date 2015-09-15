package com.ericschumacher.eu.provelopment.android.planman.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.TeilaufgabenAdapter_Dialog;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 23.07.2015.
 */
public class Dialog_Aufgabe_Detail extends DialogFragment{

    private EditText etTitel;
    private ImageButton ibDelete;
    private EditText etNotiz;
    private RecyclerView mRecyclerView;
    private TeilaufgabenAdapter_Dialog mAdapter;

    private String mTitel;
    private String mNotiz;
    private ArrayList<Teilaufgabe> mTeilaufgaben;

    private UUID id_aufgabe;
    private UUID id_rubrik;

    private Rubrik mRubrik;
    private Aufgabe mAufgabe;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        id_aufgabe = UUID.fromString(bundle.getString(Main.AUFGABE_ID));
        id_rubrik = UUID.fromString(bundle.getString(Main.RUBRIK_ID));
        mRubrik = RubrikLab.get(getActivity()).getRubrik(id_rubrik);
        mAufgabe = mRubrik.getAufgabe(id_aufgabe);
        mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.fragment_dialog_detail_aufgabe, null);
        etTitel = (EditText)myView.findViewById(R.id.etTitel_Dialog);
        ibDelete = (ImageButton)myView.findViewById(R.id.ibDelete_Dialog);
        etNotiz = (EditText)myView.findViewById(R.id.etNotiz_Dialog);
        mRecyclerView = (RecyclerView)myView.findViewById(R.id.rvTeilaufgaben_Dialog);
        mAdapter = new TeilaufgabenAdapter_Dialog(getActivity(), mTeilaufgaben, id_aufgabe, id_rubrik);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        etNotiz.setText(mAufgabe.getNotiz());
        etNotiz.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAufgabe.setNotiz(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etTitel.setText(mAufgabe.getTitle());
        etTitel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAufgabe.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Activity: ", "Geloescht");
                mRubrik.deleteAufgabe(mAufgabe);
            /*Intent i = new Intent (AufgabeErstellen.this, AufgabenListe.class);
            i.putExtra(AufgabenListe.RUBRIK_ID, mRubrik.getId());
            startActivity(i);
            */

                Dialog_Aufgabe_Detail.this.getDialog().cancel();
            }
        });

        builder.setView(myView);
        builder.setPositiveButton(getActivity().getString(R.string.fertig), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        return builder.create();


    }

    @Override
    public void onPause() {
        super.onPause();
        String Filename = mRubrik.getFilenameAufgaben();
        Log.i("Filename!: ", Filename);
        mRubrik.saveAufgaben();
        mAufgabe.saveTeilaufgaben();
    }
}
