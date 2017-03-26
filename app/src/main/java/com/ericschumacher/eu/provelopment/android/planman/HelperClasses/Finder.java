package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.content.Context;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;

/**
 * Created by eric on 01.10.2015.
 */
public class Finder {

    public Finder() {
    }

    public String getRubrikUUID(String aufgabe_uuid, Context context) {


        ArrayList<Rubrik> rubriken= RubrikLab.get(context).getRubriken();
        String rubrik_uuid = "";

        for (Rubrik r : rubriken) {
            ArrayList<Aufgabe> aufgaben = r.getAufgabenArrayList(context);
            for (Aufgabe a : aufgaben) {
                if (a.getId().toString().equals(aufgabe_uuid)) {
                    rubrik_uuid = r.getId().toString();
                    break;
                }
            }
        }
        return rubrik_uuid;
    }
}
