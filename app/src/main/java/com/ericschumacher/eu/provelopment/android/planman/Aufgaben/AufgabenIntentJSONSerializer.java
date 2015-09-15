package com.ericschumacher.eu.provelopment.android.planman.Aufgaben;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by eric on 01.07.2015.
 */
public class AufgabenIntentJSONSerializer {

    private Context mContext;

    public AufgabenIntentJSONSerializer(Context c) {
        mContext = c;

    }

    public ArrayList<Aufgabe> loadAufgaben(String filename) throws IOException, JSONException {

        Log.i("Load_serializer:", "Started");
        Log.i("FILENAME_ser", filename);
        ArrayList<Aufgabe> aufgaben = new ArrayList<Aufgabe>();
        BufferedReader reader = null;
        try {
            // Open and read the file into a StringBuilder
            InputStream in = mContext.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            Log.i("Serializer: ", "Working");
            while ((line = reader.readLine()) != null) {
                // Line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            // Parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            // Build the array of crimes from JSONObjects
            for (int i = 0; i < array.length(); i++) {
                Log.i("Serializer", "Found something");
                aufgaben.add(new Aufgabe((array.getJSONObject(i)), mContext));
            }
        } catch (FileNotFoundException e) {
            Log.i("ERROR: ", "File not Found");
            // Ignore this one; it happens when starting fresh
        } finally {
            if (reader != null)
                reader.close();
        }
        return aufgaben;
    }

    public void saveAufgaben(ArrayList<Aufgabe> aufgaben, String filename) throws JSONException, IOException {
        // build an array in JSON
        int Size = aufgaben.size();
        Log.i("Serializer_fil: ", filename);
        Log.i("Size_serializer: ", Integer.toString(Size)); // KEIN FEHLER
        JSONArray array = new JSONArray();
        //Log.i("Aufgabe: ", aufgaben.get(0).getTitle());
        for (Aufgabe c : aufgaben) {
            Log.i("Array: ", "Preparing");
            array.put(c.toJSON());

        }
        Log.i("Still: ", "Runnging");
        // write the file to disk
        Writer writer = null;
        try {
            Log.i("Try_Save: ", "Started");
            OutputStream out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            //Log.i("Save_Array", array.toString());
            writer.write(array.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        finally {
            if (writer != null)
                writer.close();
        }
    }
}
