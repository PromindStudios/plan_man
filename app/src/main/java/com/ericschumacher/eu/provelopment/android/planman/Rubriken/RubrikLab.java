package com.ericschumacher.eu.provelopment.android.planman.Rubriken;

import android.content.Context;
import android.content.SharedPreferences;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

/**
 * Created by eric on 01.07.2015.
 */
public class RubrikLab {

    private ArrayList<Rubrik> mRubriken;
    private static RubrikLab sRubrikLab;
    private static final String FILENAME = "planman_rubriken.json"; // Hier einfach "planman_rubriken.json" mit "planman_rubriken2.json" ersetzen und anschließend wieder rückgängig machen
    private static final String SORTINDICATOR = "sortindicator";
    private static RubrikIntentJSONSerializer mSerializer;
    private Context mAppContext;
    private int sortIndicator;
    private boolean alarmSet;
    public static final String PREFS_NAME = "MyPrefsFile";


    private RubrikLab(Context appContext) {
        mAppContext = appContext;
        mSerializer = new RubrikIntentJSONSerializer(mAppContext, FILENAME);

        try {

            mRubriken = mSerializer.loadRubriken();
            SharedPreferences settings = mAppContext.getSharedPreferences(PREFS_NAME, 0);
            sortIndicator = settings.getInt(SORTINDICATOR, 1);


        } catch (Exception e) {
            mRubriken = new ArrayList<Rubrik>();
            sortIndicator = 1;
        }

    }

    public static RubrikLab get(Context c) {
        if (sRubrikLab == null) {
            sRubrikLab = new RubrikLab(c.getApplicationContext());
        }
        return sRubrikLab;
    }

    public Rubrik getRubrik(UUID id) {
        for (Rubrik a : mRubriken) {
            if (a.getId().equals(id))
                return a;
        }
        return null;
    }


    public void addRubrik(Rubrik a) {
        mRubriken.add(a);
    }

    public void deleteRubrik(Rubrik a) {
        mRubriken.remove(a);
    }

    public void editRubrik(Rubrik rubrik, String title) {
        rubrik.setTitle(title);
    }

    public ArrayList<Rubrik> getRubriken() {
        /*switch (sortIndicator) {
            case 1:
                mRubriken = sortieren_nach_Aufgaben_Anzahl();
                break;
            case 2:
                mRubriken = sortieren_nach_Prioritaet();
                break;
            case 3:
                mRubriken = sortieren_nach_Deadline();
                break;
            default:

            */



        return mRubriken;
    }

    public boolean saveRubriken() {
        try {
            mSerializer.saveRubriken(mRubriken);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<Rubrik> sortieren_nach_Aufgaben_Anzahl() {

        Collections.sort(mRubriken, new CustomComparator_Anzahl_Aufgaben());
        Collections.reverse(mRubriken);
        return mRubriken;

    }

    public ArrayList<Rubrik> sortieren_nach_Prioritaet() {
        ArrayList<Rubrik> sortedRubriken_eins = new ArrayList<Rubrik>();
        ArrayList<Rubrik> sortedRubriken_zwei = new ArrayList<Rubrik>();
        ArrayList<Rubrik> sortedRubriken_drei = new ArrayList<Rubrik>();
        boolean prioritaet_one = false;
        boolean prioritaet_two = false;

        for (Rubrik r : mRubriken) {
            String filename = r.getFilenameAufgaben();
            ArrayList<Aufgabe> aufgaben = r.getAufgabenArrayList(mAppContext);
            for (Aufgabe a: aufgaben) {
                if (a.getPrioritaet() == 1) {
                    prioritaet_one = true;

                }
                if (a.getPrioritaet() == 2) {
                    prioritaet_two = true;
                }
            }
            if (prioritaet_one) {
                sortedRubriken_eins.add(r);
                prioritaet_one = false;
            } else {
                if (prioritaet_two) {
                    sortedRubriken_zwei.add(r);
                } else {
                    sortedRubriken_drei.add(r);
                }

            }
        }
        for (Rubrik r : sortedRubriken_zwei) {
            sortedRubriken_eins.add(r);
        }
        for (Rubrik r : sortedRubriken_drei) {
            sortedRubriken_eins.add(r);
        }
        mRubriken = sortedRubriken_eins;
        return mRubriken;
    }

    public ArrayList<Rubrik> sortieren_nach_Deadline() {
        Collections.sort(mRubriken, new CustomComparator_Rubriken_by_Deadline());
        return mRubriken;
    }

    public class CustomComparator_Anzahl_Aufgaben implements Comparator<Rubrik> {

        @Override
        public int compare(Rubrik rhs, Rubrik lhs) {


            String filename_1 = rhs.getFilenameAufgaben();
            String filename_2 = lhs.getFilenameAufgaben();
            int i = ((Integer)rhs.getAufgabenArrayList(mAppContext).size()).compareTo(lhs.getAufgabenArrayList(mAppContext).size());

            return i;

        }
    }

    public class CustomComparator_Rubriken_by_Deadline implements Comparator<Rubrik> {

        @Override
        public int compare(Rubrik lhs, Rubrik rhs) {

            Calendar Deadline_2;
            Calendar Deadline_1;

            String filename_1 = lhs.getFilenameAufgaben();
            ArrayList<Aufgabe> aufgaben_1 = lhs.getAufgabenArrayList(mAppContext);
            aufgaben_1 = lhs.sortbyDate_for_Rubriken();
            if (aufgaben_1.get(0).getDeadline() != null) {
                Deadline_1 = aufgaben_1.get(0).getDeadline();
            } else {
                Deadline_1 = null;
            }

            String filename_2 = rhs.getFilenameAufgaben();
            ArrayList<Aufgabe> aufgaben_2 = lhs.getAufgabenArrayList(mAppContext);
            aufgaben_2 = rhs.sortbyDate_for_Rubriken();
            if (aufgaben_2.get(0).getDeadline() != null) {
                Deadline_2 = aufgaben_2.get(0).getDeadline();
            } else {
                Deadline_2 = null;
            }

            boolean change_one = false;
            boolean change_two = false;
            if (Deadline_1 == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2090, 1, 1);
                Deadline_1 = calendar;
                change_one = true;
            }
            if (Deadline_2 == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2091, 1, 1);
                Deadline_2 = calendar;
                change_two = true;
            }
            int i = Deadline_1.compareTo(Deadline_2);


            return i;

        }
    }

    public int getSortIndicator() {

        return sortIndicator;
    }

    public void setSortIndicator(int sortIndicator) {
        this.sortIndicator = sortIndicator;
        SharedPreferences settings = mAppContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SORTINDICATOR, sortIndicator);
        editor.commit();
    }
}
