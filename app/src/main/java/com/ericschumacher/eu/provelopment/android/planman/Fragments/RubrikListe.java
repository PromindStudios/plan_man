package com.ericschumacher.eu.provelopment.android.planman.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ericschumacher.eu.provelopment.android.planman.Activities.AufgabeErstellen;
import com.ericschumacher.eu.provelopment.android.planman.Activities.Main;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Info;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Notification;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Add;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Delete;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Edit;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmSetter;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikAdapter;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by eric on 16.06.2015.
 */
public class RubrikListe extends Fragment implements RubrikAdapter.RubrikItemListener, Dialog_Rubrik_Edit.EditDialogListener, Dialog_Rubrik_Delete.DeleteRubrikListener{

    // Layout Components
    private ImageButton ibOverview;
    private RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton mAdd;
    private ImageButton ibSort;
    private ImageButton mSettings;
    private ImageButton mInfo;
    private RelativeLayout rlHeader;
    private ActionBarDrawerToggle mDrawerToggle;
    private RubrikAdapter adapter;
    private AufgabeErstellen.BackButtonListener mBackButtonListener;
    private ImageButton ibNotification;

    // Content
    private ArrayList<Rubrik> mRubriken;
    private RubrikAdapter.RubrikItemListener mRubrikItemListener;
    private Rubrik rubrik;
    private RubrikLab mRubrikLab;

    // Constants
    public static final String UUI_RUBRIK = "rubrik_id";
    public static final String EDIT_RUBRIK_KEY = "edit_rubrik_key";
    public static final String ID_EDIT_RUBRIK_KEY = "id_edit_rubrik_key";

    // Listener
    private RubrikListe_Listener mListener;

    // Others
    private Main mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_rubrikliste, container, false);

        mRubrikLab = RubrikLab.get(getActivity());
        mRubriken = mRubrikLab.getRubriken();
        mRubrikItemListener = this;

        mRecyclerView = (RecyclerView)layout.findViewById(R.id.rvRubrikliste);
        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final FragmentManager fragmentManager = mContext.getSupportFragmentManager();
        mAdd = (FloatingActionButton)layout.findViewById(R.id.fabAddRubrik);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new Dialog_Rubrik_Add();
                dialog.show(fragmentManager, "Dialog_Add");

            }
        });

        ibSort = (ImageButton)layout.findViewById(R.id.ibSort_Rubriken);
        ibSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sortIndicator = RubrikLab.get(getActivity()).getSortIndicator();

                /*
                mRubriken = RubrikLab.get(getActivity()).sortieren_nach_Aufgaben_Anzahl();
                adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
                mRecyclerView.setAdapter(adapter);
                Toast.makeText(getActivity(), getActivity().getString(R.string.sortiert_nach_Anzahl), Toast.LENGTH_SHORT).show();
                */

                switch (sortIndicator) {
                    case 1:
                        mRubriken = RubrikLab.get(getActivity()).sortieren_nach_Aufgaben_Anzahl();
                        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
                        mRecyclerView.setAdapter(adapter);
                        RubrikLab.get(getActivity()).setSortIndicator(2);
                        Toast.makeText(getActivity(), getActivity().getString(R.string.sortiert_nach_Anzahl), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mRubriken = RubrikLab.get(getActivity()).sortieren_nach_Prioritaet();
                        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
                        mRecyclerView.setAdapter(adapter);
                        RubrikLab.get(getActivity()).setSortIndicator(3);
                        Toast.makeText(getActivity(), getActivity().getString(R.string.sortiert_nach_Prioritaet_rubriken), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        mRubriken = RubrikLab.get(getActivity()).sortieren_nach_Deadline();
                        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
                        mRecyclerView.setAdapter(adapter);
                        RubrikLab.get(getActivity()).setSortIndicator(1);
                        Toast.makeText(getActivity(), getActivity().getString(R.string.sortiert_nach_Deadline_rubriken), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        mRubriken = RubrikLab.get(getActivity()).sortieren_nach_Aufgaben_Anzahl();
                        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
                        mRecyclerView.setAdapter(adapter);
                        RubrikLab.get(getActivity()).setSortIndicator(2);
                        Toast.makeText(getActivity(), getActivity().getString(R.string.sortiert_nach_Anzahl), Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        ibSort.setVisibility(View.INVISIBLE);

        ibOverview = (ImageButton)layout.findViewById(R.id.ibOverview);
        ibOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent i = new Intent (getActivity(), Main.class);
                i.putExtra(Main.SHOW_OVERVIEW, true);
                startActivity(i);
                getActivity().finish();
                */
                mListener.onSelectedOverview();
            }
        });
        ibNotification = (ImageButton)layout.findViewById(R.id.ibNotification);
        ibNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmSetter alarmSetter = new AlarmSetter();

                SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
                Boolean alarmActivated = settings.getBoolean(Constants.SP_ALARM_SET_BY_USER, true);

                if (alarmActivated) {
                    alarmSetter.cancelAlarm(getActivity());
                    ibNotification.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bell_strikethrough));

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(Constants.SP_ALARM_SET_BY_USER, false);
                    editor.commit();
                    Toast.makeText(getActivity(),getActivity().getString(R.string.Notification_Deactivated), Toast.LENGTH_SHORT).show();
                } else {
                    alarmSetter.setAlarm(getActivity());
                    ibNotification.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bell));

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(Constants.SP_ALARM_SET_BY_USER, true);
                    editor.commit();
                    Toast.makeText(getActivity(),getActivity().getString(R.string.Notification_Activated), Toast.LENGTH_SHORT).show();
                }

            }
        });

        ibNotification.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DialogFragment dialog = new Dialog_Notification();
                dialog.show(getActivity().getSupportFragmentManager(), "dialog_notification");

                return true;
            }
        });


        mInfo = (ImageButton)layout.findViewById(R.id.ibAppInfo);
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new Dialog_Info();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        rlHeader = (RelativeLayout)layout.findViewById(R.id.rlHeader);
        rlHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // nothing
            }
        });


        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Main) activity;
        mBackButtonListener = (AufgabeErstellen.BackButtonListener)activity;
        mListener = (RubrikListe_Listener)activity;

    }



    @Override
    public void onResume() {
        super.onResume();
        Log.i("RubrikListe: ", "onResume");

        mRubriken = RubrikLab.get(getActivity()).getRubriken();
        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
        mRecyclerView.setAdapter(adapter);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    mBackButtonListener.onBackButtonPressed();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        RubrikLab.get(mContext).saveRubriken();
    }

    public void setUp(DrawerLayout drawerLayout, final Toolbar toolbar) {
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();

            }
        });
    }


    @Override
    public void Refresh() {
        adapter.notifyDataSetChanged();
    }

    public void Edit(UUID uuid) {
        Rubrik rubrik = mRubrikLab.getRubrik(uuid);
        String id = rubrik.getId().toString();
        Bundle bundle = new Bundle();
        bundle.putString(EDIT_RUBRIK_KEY, rubrik.getTitle());
        bundle.putString(ID_EDIT_RUBRIK_KEY, id);
        DialogFragment dialog = new Dialog_Rubrik_Edit();
        dialog.setTargetFragment(this, 0);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "Dialog_Edit");

    }

    @Override
    public void Selected(UUID uuid) {
        Intent i = new Intent(getActivity(), Main.class);
        i.putExtra(Main.RUBRIK_ID, uuid);
        i.putExtra(Main.SHOW_OVERVIEW, false);
        getActivity().startActivity(i);
        getActivity().finish();
    }

    @Override
    public void delete(UUID uuid) {
        DialogFragment dialog = new Dialog_Rubrik_Delete();
        Bundle bundle = new Bundle();
        bundle.putString(UUI_RUBRIK, uuid.toString());
        dialog.setTargetFragment(this, 0);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "dialog_delete_rubrik");
    }

    @Override
    public void onMoveUp(int position) {
        if (position == 0) {
            mRubriken.add(mRubriken.get(position));
            mRubriken.remove(0);
        } else {
            mRubriken.add(position - 1, mRubriken.get(position));
            mRubriken.remove(position + 1);
        }
        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onMoveDown(int position) {
        int size = mRubriken.size();
        if (size-1 == position) {
            mRubriken.add(0, mRubriken.get(position));
            mRubriken.remove(position+1);
        } else {
            mRubriken.add(position+2, mRubriken.get(position));
            mRubriken.remove(position);
        }
        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDialogPositiveClick(String text, String id) {
        UUID uuid = UUID.fromString(id);
        Rubrik rubrik = mRubrikLab.getRubrik(uuid);
        mRubrikLab.editRubrik(rubrik, text);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public void onUpdate() {
        mRubriken = RubrikLab.get(getActivity()).getRubriken();
        adapter = new RubrikAdapter(getActivity(), mRubriken, mRubrikItemListener);
        mRecyclerView.setAdapter(adapter);
    }



    @Override
    public void deleteRubrik_finally(String uuid) {
        UUID Uuid = UUID.fromString(uuid);
        Rubrik rubrik = mRubrikLab.getRubrik(Uuid);
        mRubrikLab.deleteRubrik(rubrik);
        adapter.notifyDataSetChanged();
    }

    public interface RubrikListe_Listener {
        public void onSelectedOverview();
    }
}
