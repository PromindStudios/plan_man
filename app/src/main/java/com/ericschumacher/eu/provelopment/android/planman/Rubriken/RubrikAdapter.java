package com.ericschumacher.eu.provelopment.android.planman.Rubriken;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by eric on 19.06.2015.
 */
public class RubrikAdapter extends RecyclerView.Adapter<RubrikAdapter.myViewHolder> {

    private LayoutInflater inflator;
    ArrayList<Rubrik> mRubriken;
    Context context;
    RubrikItemListener mRubrikItemListener;
    RubrikAdapter_Listener mRubrikAdapter_listener;

    public RubrikAdapter(Context context, ArrayList<Rubrik> rubriken, RubrikItemListener rubrikItemListener) {

        inflator = LayoutInflater.from(context);
        this.context = context;
        mRubrikItemListener = rubrikItemListener;
        mRubrikAdapter_listener = (Main)context;
        mRubriken = rubriken;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.item_rubrik, parent, false);
        myViewHolder holder = new myViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, final int position) {
        final Rubrik rubrik = mRubriken.get(position);
        holder.title.setText(rubrik.getTitle());
        ArrayList<Aufgabe> aufgaben = rubrik.getAufgabenArrayList(context);
        int size = aufgaben.size();
        if (size == 1) {
            holder.tvCount_Aufgaben.setText(Integer.toString(size) + " " + context.getString(R.string.Task));
        } else {
            holder.tvCount_Aufgaben.setText(Integer.toString(size) + " " + context.getString(R.string.Tasks));
        }

        /*
        int Prioritaet = 3;
        int PrioritaetCounter = 0;
        for (Aufgabe a : aufgaben) {
            if (a.getPrioritaet() == 1) {
                Prioritaet = 1;
                PrioritaetCounter++;
                break;
            } else {
                if (a.getPrioritaet() == 2) {
                    Prioritaet = 2;
                }
            }
        }

        if (Prioritaet == 1) {

            holder.ibPrioritaet.setVisibility(View.VISIBLE);
            holder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_prioritaet_eins));
            final int finalPrioritaetCounter = PrioritaetCounter;
            holder.ibPrioritaet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalPrioritaetCounter == 1) {
                        Toast.makeText(context, Integer.toString(finalPrioritaetCounter) + " " + context.getString(R.string.Task), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, Integer.toString(finalPrioritaetCounter) + " " + context.getString(R.string.Tasks), Toast.LENGTH_LONG).show();
                    }

                }
            });
        } else {
            if (Prioritaet == 2){
                holder.ibPrioritaet.setVisibility(View.VISIBLE);
                holder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_prioritaet_zwei));
            } else {
                holder.ibPrioritaet.setVisibility(View.VISIBLE);
                holder.ibPrioritaet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alert_green));
                //holder.ibPrioritaet.setVisibility(View.INVISIBLE);
            }

        }

        */

        boolean hasDeadline = false;
        boolean hasOverdoTask = false;
        int Days_Left = -100000;
        for (Aufgabe a : aufgaben) {
            if (a.getDeadline() != null) {
                hasDeadline = true;
                Calendar deadline = a.getDeadline();

                int daysBetween = (int)getNumberOfLeftDays(deadline);
                if (daysBetween == 0) {
                    hasOverdoTask = true;
                    break;
                }
            }
        }

        if (hasOverdoTask) {
            holder.ibDeadline.setVisibility(View.VISIBLE);
            holder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_deadline_eins));
            holder.tvDeadlineDays.setVisibility(View.VISIBLE);
            holder.tvDeadlineDays.setText("!");
            //holder.title.setTextColor(context.getResources().getColor(R.color.Importance_one));

        } else {
            holder.ibDeadline.setVisibility(View.INVISIBLE);
            holder.tvDeadlineDays.setVisibility(View.INVISIBLE);
            //holder.title.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
        }
        holder.ibPrioritaet.setVisibility(View.INVISIBLE);

        /*
        if (hasDeadline) {

            if (Days_Left < 3) {
                holder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_deadline_eins));

            }
            if (Days_Left > 2 && Days_Left < 6) {
                holder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_deadline_drei));

            }

            if (Days_Left >= 6) {
                holder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clock_green));
            }


        } else {
            holder.ibDeadline.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_alarm_grey));
        }
        */




        holder.llTitel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mRubrikItemListener.Selected(rubrik.getId());
                mRubrikAdapter_listener.onRubrikItemSelected(rubrik.getId());

            }
        });

        holder.llTitel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final RubrikLab rubrikLab = RubrikLab.get(context);

                final CharSequence[] items = {context.getString(R.string.edit), context.getString(R.string.delete), context.getString(R.string.move_up), context.getString(R.string.move_down)};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                mRubrikItemListener.Edit(rubrik.getId());
                                break;
                            case 1:
                                mRubrikItemListener.delete(rubrik.getId());
                                break;
                            case 2:
                                mRubrikItemListener.onMoveUp(position);
                                break;
                            case 3:
                                mRubrikItemListener.onMoveDown(position);
                                break;
                            default:
                                break;
                        }

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }


        });
    }

    @Override
    public int getItemCount() {
        return mRubriken.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder {


        TextView title;
        TextView tvCount_Aufgaben;
        ImageButton ibPrioritaet;
        ImageButton ibDeadline;
        LinearLayout llTitel;
        TextView tvDeadlineDays;

        public myViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tvRubrikTitle);
            tvCount_Aufgaben = (TextView) itemView.findViewById(R.id.tvRubrik_Aufgaben_Anzahl);
            tvDeadlineDays = (TextView) itemView.findViewById(R.id.tvDeadlineDays_Rubrik);
            ibPrioritaet = (ImageButton) itemView.findViewById(R.id.ibPrioritaet_Rubrik);
            ibDeadline = (ImageButton) itemView.findViewById(R.id.ibDeadline_Indicator_Rubrik);
            llTitel = (LinearLayout) itemView.findViewById(R.id.llRubrik_Titel);

        }


    }

    public interface RubrikItemListener {
        public void Refresh();

        public void Edit(UUID uuid);

        public void Selected(UUID uuid);

        public void delete(UUID uuid);

        public void onMoveUp(int position);

        public void onMoveDown(int position);
    }

    public interface RubrikAdapter_Listener {
        public void onRubrikItemSelected(UUID rubrikId);
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


}
