package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by eric on 28.08.2015.
 */
public class Sorter {

    public Sorter() {
    }

    public ArrayList<Aufgabe> sortAufgaben_ByDate(ArrayList<Aufgabe> Aufgaben) {

        ArrayList<Aufgabe> aufgaben = Aufgaben;
        Collections.sort(aufgaben, new CustomComparator_2());
        return aufgaben;

    }

    public ArrayList<Aufgabe> sortAufgaben_ByPriority(ArrayList<Aufgabe> aufgaben) {

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

}




