package com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 23.07.2015.
 */
public class TeilaufgabenAdapter_Dialog extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final int TYPE_ADD = 0;
    private final int TYPE_TEILAUFGABE = 1;

    private LayoutInflater inflator;
    private ArrayList<Teilaufgabe> mTeilaufgaben;

    private Context mContext;
    private Aufgabe mAufgabe;
    private Rubrik mRubrik;


    public TeilaufgabenAdapter_Dialog(Context context, ArrayList<Teilaufgabe> teilaufgaben, UUID aufgabe_id, UUID rubrik_id) {
        mContext = context;
        mTeilaufgaben = teilaufgaben;
        inflator = LayoutInflater.from(context);

        mRubrik = RubrikLab.get(mContext).getRubrik(rubrik_id);
        mAufgabe = mRubrik.getAufgabe(aufgabe_id);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = inflator.inflate(R.layout.item_teilaufgabe_add_dialog, parent, false);
            return new ViewHolder_Add_dialog(view);
        } else {
            View view = inflator.inflate(R.layout.item_teilaufgabe_dialog, parent, false);
            return new ViewHolder_Teilaufgabe_dialog(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            final TeilaufgabenAdapter_Dialog.ViewHolder_Add_dialog Holder = (TeilaufgabenAdapter_Dialog.ViewHolder_Add_dialog) holder;
            Holder.ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Holder.ibSave.getVisibility() == View.INVISIBLE) {
                        Holder.ibSave.setVisibility(View.VISIBLE);
                        Holder.etEingabe.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(Holder.etEingabe, InputMethodManager.SHOW_IMPLICIT);
                        Holder.ibAdd.setImageResource(R.drawable.ic_delete);
                    } else {
                        Holder.ibSave.setVisibility(View.INVISIBLE);
                        Holder.ibAdd.setImageResource(R.drawable.ic_add_toolbar);
                        Holder.etEingabe.setText("");
                        Holder.etEingabe.clearFocus();
                        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(Holder.etEingabe.getWindowToken(), 0);
                    }
                }
            });
            Holder.ibSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Teilaufgabe teilaufgabe = new Teilaufgabe(mContext);
                    teilaufgabe.setTitle(Holder.etEingabe.getText().toString());
                    mTeilaufgaben.add(teilaufgabe);
                    TeilaufgabenAdapter_Dialog.this.notifyDataSetChanged();
                    Holder.ibSave.setVisibility(View.INVISIBLE);
                    Holder.ibAdd.setImageResource(R.drawable.ic_add_toolbar);
                    Holder.etEingabe.setText("");
                    Holder.etEingabe.clearFocus();
                    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Holder.etEingabe.getWindowToken(), 0);
                }
            });

        } else {
            TeilaufgabenAdapter_Dialog.ViewHolder_Teilaufgabe_dialog Holder= (TeilaufgabenAdapter_Dialog.ViewHolder_Teilaufgabe_dialog)holder;
            final Teilaufgabe teilaufgabe = mTeilaufgaben.get(position - 1);
            Holder.tvTeilaufgabeTitle.setText(teilaufgabe.getTitle());

            Holder.cbTeilaufgabe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAufgabe.deleteTeilaufgabe(teilaufgabe);
                    TeilaufgabenAdapter_Dialog.this.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (mTeilaufgaben.size() == 0) {
            return 1;
        } else {
            return (mTeilaufgaben.size()+1);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return TYPE_ADD;
        } else {
            return TYPE_TEILAUFGABE;
        }
    }

    class ViewHolder_Add_dialog extends RecyclerView.ViewHolder {

        ImageButton ibAdd;
        EditText etEingabe;
        ImageButton ibSave;

        public ViewHolder_Add_dialog(View itemView) {
            super(itemView);
            ibAdd = (ImageButton)itemView.findViewById(R.id.ibAddTeilaufgbe_Dialog);
            etEingabe = (EditText)itemView.findViewById(R.id.etTeilaufgabe_add_dialog);
            ibSave = (ImageButton)itemView.findViewById(R.id.ibSave_Teilaufgabe_Dialog);
        }
    }

    class ViewHolder_Teilaufgabe_dialog extends RecyclerView.ViewHolder {

        CheckBox cbTeilaufgabe;
        TextView tvTeilaufgabeTitle;

        public ViewHolder_Teilaufgabe_dialog(View itemView) {
            super(itemView);
            cbTeilaufgabe = (CheckBox)itemView.findViewById(R.id.cbTeilaufgabe_Done_Dialog);
            tvTeilaufgabeTitle = (TextView)itemView.findViewById(R.id.tvTeilaufgabeTitle_Dialog);
        }
    }
}
