package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 07.10.2015.
 */

public class CodingWithJava  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView Text = (TextView)findViewById(R.id.tvDeadline);

        Text.setText("Das ist meine erste App!!");
    }
}
