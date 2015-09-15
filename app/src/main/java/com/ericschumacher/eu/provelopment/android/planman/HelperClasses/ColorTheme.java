package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.ericschumacher.eu.provelopment.android.planman.R;

/**
 * Created by eric on 03.09.2015.
 */
public class ColorTheme {

    Context mContext;
    int mColor;
    int[] mColorsPrimary;
    int[] mColorsPrimaryDark;
    int[] mColorsPrimaryLight;
    int mColorTheme;
    int[][] mStates;

    // StateLists
    ColorStateList mColorStateListPrimary;
    ColorStateList mColorStateListPrimaryDark;
    ColorStateList mColorStateListPrimaryLight;

    public ColorTheme (Context context) {
        Log.i("ColorTheme: ", "Executed");
        mContext = context;

        //get Color Theme
        SharedPreferences settings = mContext.getSharedPreferences(Constans.SHARED_PREFERENCES, 0);
        mColorTheme = settings.getInt(Constans.SP_COLOR_THEME, 0);

        // set States
        mStates = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };

        Resources r = context.getResources();

        mColorsPrimary = new int[6];
        mColorsPrimary[0] = r.getColor(R.color.colorPrimary_Primelist);
        mColorsPrimary[1] = r.getColor(R.color.colorPrimary_Executive);
        mColorsPrimary[2] = r.getColor(R.color.colorPrimary_Ocean);
        mColorsPrimary[3] = r.getColor(R.color.colorPrimary_Royal);
        mColorsPrimary[4] = r.getColor(R.color.colorPrimary_Candy);
        mColorsPrimary[5] = r.getColor(R.color.colorPrimary_Forest);

        mColorsPrimaryDark = new int[6];
        mColorsPrimaryDark[0] = r.getColor(R.color.colorPrimaryDark_Primelist);
        mColorsPrimaryDark[1] = r.getColor(R.color.colorPrimaryDark_Executive);
        mColorsPrimaryDark[2] = r.getColor(R.color.colorPrimaryDark_Ocean);
        mColorsPrimaryDark[3] = r.getColor(R.color.colorPrimaryDark_Royal);
        mColorsPrimaryDark[4] = r.getColor(R.color.colorPrimaryDark_Candy);
        mColorsPrimaryDark[5] = r.getColor(R.color.colorPrimaryDark_Forest);

        mColorsPrimaryLight = new int[6];
        mColorsPrimaryLight[0] = r.getColor(R.color.colorPrimaryLight_Primelist);
        mColorsPrimaryLight[1] = r.getColor(R.color.colorPrimaryLight_Executive);
        mColorsPrimaryLight[2] = r.getColor(R.color.colorPrimaryLight_Ocean);
        mColorsPrimaryLight[3] = r.getColor(R.color.colorPrimaryLight_Royal);
        mColorsPrimaryLight[4] = r.getColor(R.color.colorPrimaryLight_Candy);
        mColorsPrimaryLight[5] = r.getColor(R.color.colorPrimaryLight_Forest);
    }

    public int getColorPrimary() {


        return mColorsPrimary[mColorTheme];
    }

    public ColorStateList getColorPrimary_ColorStateList() {

        int[] colorsPrimary = new int[] {
                mColorsPrimary[mColorTheme]
        };

        mColorStateListPrimary = new ColorStateList(mStates, colorsPrimary);
        return mColorStateListPrimary;
    }

    public int getColorPrimaryDark() {

        return mColorsPrimaryDark[mColorTheme];
    }

    public ColorStateList getColorPrimaryDark_ColorStateList() {

        int[] colorsPrimary = new int[] {
                mColorsPrimaryDark[mColorTheme]
        };

        mColorStateListPrimaryDark = new ColorStateList(mStates, colorsPrimary);
        return mColorStateListPrimaryDark;
    }

    public int getColorPrimaryLight() {

        return mColorsPrimaryLight[mColorTheme];
    }

    public ColorStateList getColorPrimaryLight_ColorStateList() {

        int[] colorsPrimary = new int[] {
                mColorsPrimaryLight[mColorTheme]
        };

        mColorStateListPrimaryLight = new ColorStateList(mStates, colorsPrimary);
        return mColorStateListPrimaryLight;
    }



}

