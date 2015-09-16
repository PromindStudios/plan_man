package com.ericschumacher.eu.provelopment.android.planman.Aufgaben;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.UUID;

/**
 * Created by eric on 16.07.2015.
 */
public class Adapter_AufgabenListe extends RecyclerView.Adapter<Adapter_AufgabenListe.myViewHolder> {

    private LayoutInflater inflator;
    ArrayList<Aufgabe> mAufgaben;
    Context context;

    // Colors
    int mColorPrimary;
    int mColorPrimaryDark;
    int mColorPrimaryLight;
    ColorStateList mColorStateListPrimary;
    ColorStateList mColorStateListPrimaryDark;
    ColorStateList mColorStateListPrimaryLight;

    //AufgabenAdapterListener mListener;
    AufgabenAdapter_Listener mListener;
    private boolean mOverview;


    public Adapter_AufgabenListe(Context context, ArrayList<Aufgabe> aufgaben, boolean overview, Fragment fragment) {

        inflator = LayoutInflater.from(context);
        this.context = context;
        mAufgaben = aufgaben;
        mOverview = overview;

        if (mOverview) {
            mListener = (AufgabenAdapter_Listener)fragment;
        } else {
            mListener = (AufgabenAdapter_Listener)fragment;
        }

     // get ColorTheme
        //setColors();

    }

    @Override
    public Adapter_AufgabenListe.myViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflator.inflate(R.layout.item_aufgabe, viewGroup, false);
        myViewHolder holder = new myViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(Adapter_AufgabenListe.myViewHolder myViewHolder, final int position) {

        // set Variables
        final int Position = position;
        final Aufgabe aufgabe = mAufgaben.get(position);

        // set Title and SubTitle depending on Overview
        if (mOverview) {
            myViewHolder.llAufgabe_Notiz.setVisibility(View.VISIBLE);
            myViewHolder.title.setVisibility(View.INVISIBLE);
            myViewHolder.tvAufgabeTitle_mit_Notiz.setText(aufgabe.getTitle());
            myViewHolder.tvNotiz.setText(aufgabe.getRubrikName());
        } else {
            if (aufgabe.getNotiz() != null && !aufgabe.getNotiz().equals("")) {
                myViewHolder.llAufgabe_Notiz.setVisibility(View.VISIBLE);
                myViewHolder.title.setVisibility(View.INVISIBLE);
                myViewHolder.tvAufgabeTitle_mit_Notiz.setText(aufgabe.getTitle());
                myViewHolder.tvNotiz.setText(aufgabe.getNotiz());
            } else {
                myViewHolder.llAufgabe_Notiz.setVisibility(View.INVISIBLE);
                myViewHolder.title.setVisibility(View.VISIBLE);
                myViewHolder.title.setText(aufgabe.getTitle());
            }

        }

        // setText of Title and onClickListener
        myViewHolder.title.setText(aufgabe.getTitle());

        View.OnClickListener onClickListener_Aufgabe = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Aufgabe aufgabe = mAufgaben.get(Position);
                //mListener.onAufgabeClicked(aufgabe.getId());
                mListener.onAufgabenItemSelected(aufgabe.getId());
            }
        };
        myViewHolder.title.setOnClickListener(onClickListener_Aufgabe);
        myViewHolder.llAufgabe_Notiz.setOnClickListener(onClickListener_Aufgabe);


        View.OnClickListener clickListenerCheckbox = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //mListener.onDeleteAufgabe(aufgabe.getId());
                        mListener.onAufgabenItemDelete(aufgabe.getId());
                    }
                }, 400);
            }
        };
        myViewHolder.cbCheckAufgabe_eins.setOnClickListener(clickListenerCheckbox);
        myViewHolder.cbCheckAufgabe_deaktiviert.setEnabled(false);

        final int Days_Left;
        int size_teilaufgaben;
        int size_done_teilaufgaben = 0;
        if (aufgabe.getTeilaufgabenArrayList(context) != null) {
            size_teilaufgaben = aufgabe.getTeilaufgabenArrayList(context).size();
            size_done_teilaufgaben = aufgabe.getSize_DoneTeilaufgaben();
        } else {
            size_teilaufgaben = 0;
        }
        int pixel_width = getPixelWidth(context);
        if (size_teilaufgaben > 0) {
            if (size_teilaufgaben == size_done_teilaufgaben) {
                myViewHolder.cbCheckAufgabe_eins.setVisibility(View.VISIBLE);
                myViewHolder.cbCheckAufgabe_deaktiviert.setVisibility(View.INVISIBLE);
            } else {
                myViewHolder.cbCheckAufgabe_eins.setVisibility(View.INVISIBLE);
                myViewHolder.cbCheckAufgabe_deaktiviert.setVisibility(View.VISIBLE);
            }
            double pixel = (double) size_done_teilaufgaben / (double) size_teilaufgaben * (double) pixel_width;
            int pixel_done = (int) pixel;

            myViewHolder.StatusBar_Done.getLayoutParams().width = pixel_done;
            myViewHolder.StatusBar_Grey.getLayoutParams().width = pixel_width - pixel_done;
            myViewHolder.StatusBar_Done.requestLayout();
            myViewHolder.StatusBar_Grey.requestLayout();


        } else {
            myViewHolder.cbCheckAufgabe_eins.setVisibility(View.VISIBLE);
            myViewHolder.cbCheckAufgabe_deaktiviert.setVisibility(View.INVISIBLE);

            myViewHolder.StatusBar_Done.setMinimumWidth(pixel_width);
            myViewHolder.StatusBar_Grey.setMinimumWidth(0);
            myViewHolder.StatusBar_Done.getLayoutParams().width = pixel_width;
            myViewHolder.StatusBar_Grey.getLayoutParams().width = 0;
            myViewHolder.StatusBar_Done.requestLayout();
            myViewHolder.StatusBar_Grey.requestLayout();

        }

        int Prioritaet = aufgabe.getPrioritaet();

        boolean hasDeadline = false;
        if (aufgabe.getDeadline() != null) {
            hasDeadline = true;
        }

        if (hasDeadline) {
            Days_Left = (int) getNumberOfLeftDays(aufgabe.getDeadline());

            if (Days_Left < 2) {
                myViewHolder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_deadline_eins));
                myViewHolder.tvDeadlineDays.setText(Integer.toString(Days_Left));
                setTitleNotiz_Normal(myViewHolder);
                if (Days_Left == 0) {
                    myViewHolder.title.setTextColor(context.getResources().getColor(R.color.Importance_one));
                    myViewHolder.tvDeadlineDays.setText("!");
                    myViewHolder.tvDeadlineDays.setTypeface(null, Typeface.BOLD);
                    myViewHolder.tvDeadlineDays.setTextColor(context.getResources().getColor(R.color.Importance_one));
                    myViewHolder.title.setTextColor(context.getResources().getColor(R.color.Importance_one));
                    myViewHolder.tvAufgabeTitle_mit_Notiz.setTextColor(context.getResources().getColor(R.color.Importance_one));
                    myViewHolder.tvNotiz.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
                }
            }


            if (Days_Left > 1 && Days_Left < 5) {
                myViewHolder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_deadline_drei));
                myViewHolder.tvDeadlineDays.setText(Integer.toString(Days_Left));
                setTitleNotiz_Normal(myViewHolder);
            }

            if (Days_Left >= 5) {
                myViewHolder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clock_green));
                myViewHolder.tvDeadlineDays.setText(Integer.toString(Days_Left));
                setTitleNotiz_Normal(myViewHolder);

            }

            myViewHolder.ibDeadline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(context, (context.getString(R.string.days_left) + " " + Integer.toString(Days_Left)), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            myViewHolder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alarm_grey));
            myViewHolder.tvDeadlineDays.setText("");
            setTitleNotiz_Normal(myViewHolder);
        }

        switch (Prioritaet)

        {
            case 1:
                myViewHolder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_prioritaet_eins));
                myViewHolder.ibPrioritaet.setVisibility(View.VISIBLE);
                break;
            case 2:
                myViewHolder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_prioritaet_zwei));
                myViewHolder.ibPrioritaet.setVisibility(View.VISIBLE);
                break;
            case 3:
                myViewHolder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alert_green));
                myViewHolder.ibPrioritaet.setVisibility(View.VISIBLE);
                //myViewHolder.ibPrioritaet.setVisibility(View.INVISIBLE);
                break;
        }

        /*
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
        };
        int[] colors = new int[] {
                mColorPrimary
        };

        if(android.os.Build.VERSION.SDK_INT >= 21) {
            myViewHolder.cbCheckAufgabe_eins.setButtonTintList(new ColorStateList(states, colors));
        }
        */

        myViewHolder.ibPrioritaet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPrioritaetUp(aufgabe.getId());
            }
        });

        myViewHolder.ibDeadline.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onDeadlineLongClick(aufgabe.getId());
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        int size = mAufgaben.size();
        return size;
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        View StatusBar_Done;
        View StatusBar_Grey;
        ImageButton ibDeadline;
        CheckBox cbCheckAufgabe_eins;
        CheckBox cbCheckAufgabe_deaktiviert;
        ImageButton ibPrioritaet;
        LinearLayout llAufgabe_Notiz;
        TextView tvAufgabeTitle_mit_Notiz;
        TextView tvNotiz;
        TextView tvDeadlineDays;


        public myViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tvAufgabenTitle);
            StatusBar_Done = itemView.findViewById(R.id.statusBar_done);
            StatusBar_Grey = itemView.findViewById(R.id.statusBar_grey);
            ibDeadline = (ImageButton) itemView.findViewById(R.id.ibDeadline_Indicator);
            cbCheckAufgabe_eins = (CheckBox) itemView.findViewById(R.id.cbCheckAufgabe_grün);
            ibPrioritaet = (ImageButton) itemView.findViewById(R.id.ibPrioritaet);
            cbCheckAufgabe_deaktiviert = (CheckBox) itemView.findViewById(R.id.cbCheckAufgabe_deaktiviert);
            tvAufgabeTitle_mit_Notiz = (TextView) itemView.findViewById(R.id.tvAufgabenTitle_mit_Notiz);
            tvNotiz = (TextView) itemView.findViewById(R.id.tvAufgabenNotiz);
            llAufgabe_Notiz = (LinearLayout) itemView.findViewById(R.id.llAufgabe_Notiz);
            tvDeadlineDays = (TextView) itemView.findViewById(R.id.tvDeadlineDays);

        }
    }

    public interface AufgabenAdapterListener {
        public void onAufgabeClicked(UUID uuid);

        public void onDeleteAufgabe(UUID uuid);
    }

    public interface AufgabenAdapter_Listener {
        public void onAufgabenItemSelected(UUID aufgabenId);
        public void onAufgabenItemDelete(UUID aufgabenId);
        public void onPrioritaetUp(UUID aufgabenId);
        void onDeadlineLongClick(UUID aufgabenId);
    }

    private int getPixelWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        Log.i("Pixel_Width: ", Integer.toString(metrics.widthPixels));
        return metrics.widthPixels;
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

    public long getNumberOfLeftDays(Calendar deadline) {
        Calendar today = Calendar.getInstance();
        long daysBetween = 0;

        while (today.before(deadline)) {
            today.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }


        Log.i("Days_Left: ", Long.toString(daysBetween));

        return daysBetween;
    }

    private void setTitleNotiz_Normal(Adapter_AufgabenListe.myViewHolder myViewHolder) {
        myViewHolder.tvDeadlineDays.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
        myViewHolder.title.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        myViewHolder.tvAufgabeTitle_mit_Notiz.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        myViewHolder.tvNotiz.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
        myViewHolder.tvDeadlineDays.setTypeface(null, Typeface.NORMAL);
    }

    private void setColors() {
        ColorTheme colorTheme = new ColorTheme(context);
        mColorPrimary = colorTheme.getColorPrimary();
        mColorPrimaryLight = colorTheme.getColorPrimaryLight();
        mColorPrimaryDark = colorTheme.getColorPrimaryDark();
    }
}
