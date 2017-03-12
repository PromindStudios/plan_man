package com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.R;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 02.07.2015.
 */



public class TeilaufgabenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_ADD = 0;
    private final int TYPE_TEILAUFGABE = 1;

    TeilaufgabenListener mTeilaufgabenListener;

    // Colors
    ColorTheme colorTheme;

    private LayoutInflater inflator;
    private ArrayList<Teilaufgabe> mTeilaufgaben;

    private Context mContext;

    // Premium
    boolean mPremium;
    boolean mTestversion;

    public TeilaufgabenAdapter(Context context, ArrayList<Teilaufgabe> teilaufgaben, TeilaufgabenListener teilaufgabenListener, boolean premium, boolean testversion) {
        mContext = context;
        mTeilaufgaben = teilaufgaben;
        inflator = LayoutInflater.from(context);
        mTeilaufgabenListener = teilaufgabenListener;

        colorTheme = new ColorTheme(context);
        mPremium = premium;
        mTestversion = testversion;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ADD) {
            View view = inflator.inflate(R.layout.item_teilaufgabe_add, parent, false);
            return new ViewHolder_Add(view);
        }
        if (viewType == TYPE_TEILAUFGABE) {
        View view = inflator.inflate(R.layout.item_teilaufgabe, parent, false);

        return new ViewHolder_Teilaufgabe(view);

        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (position == 0) {
            TeilaufgabenAdapter.ViewHolder_Add Holder= (TeilaufgabenAdapter.ViewHolder_Add)holder;
            Log.i("Add: ", "Created");


            Holder.rlAddTeilaufgabe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPremium || mTestversion) {
                        mTeilaufgabenListener.onAdd();
                    } else {
                        mTeilaufgabenListener.onFunctionDeactivatd();
                    }

                }

            });
            Holder.ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPremium || mTestversion) {
                        mTeilaufgabenListener.onAdd();
                    } else {
                        mTeilaufgabenListener.onFunctionDeactivatd();
                    }

                }
            });

        } else {
            final TeilaufgabenAdapter.ViewHolder_Teilaufgabe Holder= (TeilaufgabenAdapter.ViewHolder_Teilaufgabe)holder;
            final Teilaufgabe teilaufgabe = mTeilaufgaben.get(position - 1);
            final Boolean Done = teilaufgabe.isDone();
            Holder.tvTeilaufgabeTitle.setText(teilaufgabe.getTitle());
            if (Done) {
                Holder.tvTeilaufgabeTitle.setPaintFlags(Holder.tvTeilaufgabeTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                Holder.tvTeilaufgabeTitle.setPaintFlags(Holder.tvTeilaufgabeTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            }
            Holder.tvTeilaufgabeTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    final CharSequence[] items = {mContext.getString(R.string.edit), mContext.getString(R.string.delete), mContext.getString(R.string.move_up), mContext.getString(R.string.move_down)};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //builder.setTitle(mContext.getString(R.string.teilaufgabe_settings_title));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                                case 0:
                                    mTeilaufgabenListener.onEdit(teilaufgabe.getId());
                                    break;
                                case 1:
                                    boolean checked = Holder.cbTeilaufgabe.isChecked();
                                    mTeilaufgabenListener.onDelete(teilaufgabe.getId(), checked);
                                    break;
                                case 2:
                                    mTeilaufgabenListener.onMoveUp(position-1);
                                    break;
                                case 3:
                                    mTeilaufgabenListener.onMoveDown(position - 1);
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


            Holder.cbTeilaufgabe.setChecked(Done);


            Log.i("Done at: " + Integer.toString(position), Boolean.toString(teilaufgabe.isDone()));


            Holder.cbTeilaufgabe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Done) {
                        mTeilaufgabenListener.onTeilaufgabeUnChecked(teilaufgabe.getId());
                    } else {
                        mTeilaufgabenListener.onTeilaufgabeChecked(teilaufgabe.getId());
                    }
                }
            });

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                Holder.cbTeilaufgabe.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, colorTheme.getColorPrimary())));
            } else {
                int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
                Holder.cbTeilaufgabe.setButtonDrawable(id);
                //myViewHolder.cbCheckAufgabe_deaktiviert.setButtonDrawable(id);
            }

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

    class ViewHolder_Add extends RecyclerView.ViewHolder  {

        ImageButton ibAdd;
        RelativeLayout rlAddTeilaufgabe;

        public ViewHolder_Add(View itemView) {
            super(itemView);
            ibAdd = (ImageButton)itemView.findViewById(R.id.ibAddTeilaufgbe);
            rlAddTeilaufgabe = (RelativeLayout)itemView.findViewById(R.id.rlAdd_Teilaufgabe);
        }

    }

    class ViewHolder_Teilaufgabe extends  RecyclerView.ViewHolder {

        CheckBox cbTeilaufgabe;
        TextView tvTeilaufgabeTitle;
        RelativeLayout rlContainer;

        public ViewHolder_Teilaufgabe(View itemView) {
            super(itemView);
            cbTeilaufgabe = (CheckBox)itemView.findViewById(R.id.cbDone);
            tvTeilaufgabeTitle = (TextView)itemView.findViewById(R.id.tvTeilaufgabeTitle);
            rlContainer = (RelativeLayout)itemView.findViewById(R.id.rlTeilaufgabenContainer);

        }
    }

    public interface TeilaufgabenListener {

        public void onAdd ();
        public void onEdit (UUID uuid);
        public void onDelete (UUID uuid, boolean checked);
        public void onTeilaufgabeChecked(UUID uuid);
        public void onTeilaufgabeUnChecked(UUID uuid);

        public void onMoveUp(int position);
        public void onMoveDown(int position);

        public void onFunctionDeactivatd();
    }
}
