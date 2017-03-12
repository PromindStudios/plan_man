package com.ericschumacher.eu.provelopment.android.planman.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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

    // Shared Preferences Instance
    SharedPreferences settings;

    public ColorTheme (Context context) {
        Log.i("ColorTheme: ", "Executed");
        mContext = context;

        //get Color Theme
        settings = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        mColorTheme = settings.getInt(Constants.SP_COLOR_THEME, 0);

        // set States
        mStates = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };

        Resources r = context.getResources();

        mColorsPrimary = new int[6];
        mColorsPrimary[0] = R.color.colorPrimary_Primelist;
        mColorsPrimary[1] = R.color.colorPrimary_Executive;
        mColorsPrimary[2] = R.color.colorPrimary_Ocean;
        mColorsPrimary[3] = R.color.colorPrimary_Royal;
        mColorsPrimary[4] = R.color.colorPrimary_Candy;
        mColorsPrimary[5] = R.color.colorPrimary_GreenAlley;

        mColorsPrimaryDark = new int[6];
        mColorsPrimaryDark[0] = R.color.colorPrimaryDark_Primelist;
        mColorsPrimaryDark[1] = R.color.colorPrimaryDark_Executive;
        mColorsPrimaryDark[2] = R.color.colorPrimaryDark_Ocean;
        mColorsPrimaryDark[3] = R.color.colorPrimaryDark_Royal;
        mColorsPrimaryDark[4] = R.color.colorPrimaryDark_Candy;
        mColorsPrimaryDark[5] = R.color.colorPrimaryDark_GreenAlley;

        mColorsPrimaryLight = new int[6];
        mColorsPrimaryLight[0] = R.color.colorPrimaryLight_Primelist;
        mColorsPrimaryLight[1] = R.color.colorPrimaryLight_Executive;
        mColorsPrimaryLight[2] = R.color.colorPrimaryLight_Ocean;
        mColorsPrimaryLight[3] = R.color.colorPrimaryLight_Royal;
        mColorsPrimaryLight[4] = R.color.colorPrimaryLight_Candy;
        mColorsPrimaryLight[5] = R.color.colorPrimaryLight_GreenAlley;
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

