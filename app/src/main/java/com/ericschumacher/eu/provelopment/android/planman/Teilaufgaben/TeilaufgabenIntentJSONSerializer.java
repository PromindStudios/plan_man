package com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben;

import android.content.Context;

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
 * Created by eric on 02.07.2015.
 */
public class TeilaufgabenIntentJSONSerializer {

    private Context mContext;

    public TeilaufgabenIntentJSONSerializer(Context context) {
        mContext = context;
    }

    public ArrayList<Teilaufgabe> loadTeilaufgaben(String filenname) throws IOException, JSONException {
        ArrayList<Teilaufgabe> teilaufgaben = new ArrayList<Teilaufgabe>();
        BufferedReader reader = null;
        try {
            InputStream in = mContext.openFileInput(filenname);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            for (int i = 0; i < array.length(); i++) {
                teilaufgaben.add(new Teilaufgabe(array.getJSONObject(i), mContext));
            }
        } catch (FileNotFoundException e) {

        } finally {
            if (reader != null)
                reader.close();
        }
        return teilaufgaben;
    }

    public void saveTeilaufgaben(ArrayList<Teilaufgabe> teilaufgaben, String filename) throws JSONException, IOException {

        JSONArray array = new JSONArray();
        for (Teilaufgabe t : teilaufgaben) {
            array.put(t.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
