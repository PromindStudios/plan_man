package com.ericschumacher.eu.provelopment.android.planman.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
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
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Connect;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Get_Premium_Ad;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Info;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Notification;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Add;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Delete;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rubrik_Edit;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_UserInformation;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AlarmSetter;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabHelper;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabResult;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikAdapter;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Services.CheckForChanges;
import com.ericschumacher.eu.provelopment.android.planman.Services.UpdateRubrik;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 16.06.2015.
 */
public class RubrikListe extends Fragment implements RubrikAdapter.RubrikItemListener, Dialog_Rubrik_Edit.EditDialogListener, Dialog_Rubrik_Delete.DeleteRubrikListener,
        Dialog_Connect.DialogConnect_Listener, Dialog_ColorTheme.DialogColorTheme_Listener, Dialog_Get_Premium_Ad.GetPremium_AdListener{

    // Layout Components
    private ImageButton ibOverview;
    private RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton mAdd;
    private ImageButton ibSort;
    private ImageButton ibColor;
    private ImageButton ibPremium;
    private ImageButton mSettings;
    private ImageButton mInfo;
    private RelativeLayout rlHeader;
    private ActionBarDrawerToggle mDrawerToggle;
    private RubrikAdapter adapter;
    private AufgabeErstellen.BackButtonListener mBackButtonListener;
    private ImageButton ibNotification;
    private RelativeLayout rlAppInfo;

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

    // Premium
    private boolean mPremium = true;
    private boolean mTestVersion = false;

    // SharedPreferences
    SharedPreferences mSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_rubrikliste, container, false);

        mSharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mPremium = mSharedPreferences.getBoolean(Constants.USER_HAS_PREMIUM, false);

        if (mPremium) {
            mTestVersion = false;
            Log.i("Premium: ", "true");
        } else {
            Log.i("Premium: ", "false");
            int number_of_tasks = mSharedPreferences.getInt(Constants.NUMBER_OF_TASKS_CREATED, 0);
            if (number_of_tasks < getResources().getInteger(R.integer.test_version_tasks)) {
                mTestVersion = true;
                Log.i("Test-Version: ", "true");
            } else {
                mTestVersion = false;
                Log.i("Test-Version: ", "false");
            }

        }

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

        /*
        ibSort = (ImageButton)layout.findViewById(R.id.ibSort_Rubriken);
        ibSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sortIndicator = RubrikLab.get(getActivity()).getSortIndicator();


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
        */











        ibPremium = (ImageButton)layout.findViewById(R.id.ibPremium);
        if (mPremium) {
            ibPremium.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_star));
            ibPremium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.Toast_youHavePremium),Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ibPremium.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_empty));
            ibPremium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment dialog = new Dialog_Get_Premium_Ad();
                    dialog.setTargetFragment(RubrikListe.this, 0);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TITLE, getActivity().getResources().getString(R.string.Dialog_PremiumAd_Title));
                    bundle.putString(Constants.DESCRIPTION, getActivity().getResources().getString(R.string.Dialog_PremiumAd_Descreption));
                    dialog.setArguments(bundle);
                    dialog.show(getActivity().getSupportFragmentManager(), "getPremiumAd_Fragment");
                }
            });
        }
        //ibPerson.setVisibility(View.INVISIBLE);

        ibColor = (ImageButton)layout.findViewById(R.id.ibColor);
        ibOverview = (ImageButton)layout.findViewById(R.id.ibOverview);
        ibNotification = (ImageButton)layout.findViewById(R.id.ibNotification);

        if (mPremium || mTestVersion) {
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

            boolean alarmActivated_2;

            final SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
            boolean alarmActivated = settings.getBoolean(Constants.SP_ALARM_SET_BY_USER, true);

            ibNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmSetter alarmSetter = new AlarmSetter();

                    boolean alarmActivated = settings.getBoolean(Constants.SP_ALARM_SET_BY_USER, true);

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

            if (alarmActivated) {
                ibNotification.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bell));
            } else {
                ibNotification.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bell_strikethrough));
            }



            ibNotification.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DialogFragment dialog = new Dialog_Notification();
                    dialog.show(getActivity().getSupportFragmentManager(), "dialog_notification");

                    return true;
                }
            });

            if(android.os.Build.VERSION.SDK_INT >= 21) {
                ibColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment dialog = new Dialog_ColorTheme();

                        dialog.setTargetFragment(RubrikListe.this, 0);

                        dialog.show(getActivity().getSupportFragmentManager(), "dialog_colorTheme");
                    }
                });
            }
            else {
                //ibColor.setVisibility(View.INVISIBLE);

                ibColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment dialog = new Dialog_ColorTheme();

                        dialog.setTargetFragment(RubrikListe.this, 0);

                        dialog.show(getActivity().getSupportFragmentManager(), "dialog_colorTheme");
                    }
                });
            }
        } else {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFunctionDeactivated();
                }
            };
            ibColor.setOnClickListener(onClickListener);
            ibOverview.setOnClickListener(onClickListener);
            ibNotification.setOnClickListener(onClickListener);

            ibNotification.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_bell_strikethrough));

            SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
            Boolean alarmActivated = settings.getBoolean(Constants.SP_ALARM_SET_BY_USER, true);

            if (alarmActivated) {
                AlarmSetter alarmSetter = new AlarmSetter();
                alarmSetter.cancelAlarm(getActivity());
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.SP_ALARM_SET_BY_USER, false);
                editor.commit();
            }
        }






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

        // setColor
        ColorTheme colorTheme = new ColorTheme(getActivity());
        rlHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary()));

        rlAppInfo = (RelativeLayout)layout.findViewById(R.id.rlAppInfo);
        rlAppInfo.setBackgroundColor(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimaryLight()));

        mAdd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), colorTheme.getColorPrimary())));



        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Main)context;
        mBackButtonListener = (AufgabeErstellen.BackButtonListener)(Main)context;
        mListener = (RubrikListe_Listener)(Main)context;

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

        Intent i = new Intent(getActivity(), CheckForChanges.class);
        getActivity().startService(i);
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
    public void onLoadRubrikToServer(UUID uuid) {

        // open Dialog_connect
        DialogFragment dialog = new Dialog_Connect();

        // in order to call methods in the TargetFragment
        dialog.setTargetFragment(this, 0);

        // add Bundle with uuid
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RUBRIK_UUID, uuid.toString());
        dialog.setArguments(bundle);

        // start DialogFragment
        dialog.show(getActivity().getSupportFragmentManager(), "dialog_check_aufgabe");
    }

    @Override
    public void onDialogPositiveClick(String text, String id) {
        UUID uuid = UUID.fromString(id);
        Rubrik rubrik = mRubrikLab.getRubrik(uuid);
        mRubrikLab.editRubrik(rubrik, text);
        adapter.notifyDataSetChanged();
        mRubrikLab.saveRubriken();
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

    @Override
    public void onConnect(int id_otherUser, int key_otherUser, String uuid) {
        Rubrik rubrik = mRubrikLab.getRubrik(UUID.fromString(uuid));

        // set Rubrik up for the first time, with Data Check
        String[] data = {Integer.toString(id_otherUser), Integer.toString(key_otherUser), uuid};
        new SendData_Rubrik_withDataCheck().execute(data);

        // Set everything up in Server!
        //new SendData_Rubrik().execute(rubrikUUID);

    }

    @Override
    public void onRestart() {
        Intent intent = new Intent(getActivity(), Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onGetPremiumAdSelected() {
        mListener.onInAppPurchase();
    }

    public interface RubrikListe_Listener {
        public void onSelectedOverview();
        public void onInAppPurchase();
        public void onFunctionDeactivated();
    }

    class SendData_Rubrik extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {


            Rubrik rubrik = mRubrikLab.getRubrik(UUID.fromString(params[0]));
            String response = "";

            try {
                // Create JSONObject which will be send to Server
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rubrik_uuid", params[0]);
                jsonObject.put("rubrik_name", rubrik.getTitle());

                ArrayList<Aufgabe> aufgaben = rubrik.getAufgabenArrayList(getActivity());

                JSONArray jsonArray_aufgaben = new JSONArray();

                for (int i = 0; i < aufgaben.size(); i++) {
                    JSONObject jsonObject_aufgabe = new JSONObject();
                    Aufgabe aufgabe = aufgaben.get(i);
                    jsonObject_aufgabe.put("aufgabe_uuid", aufgabe.getId());
                    jsonObject_aufgabe.put("aufgabe_title", aufgabe.getTitle());
                    jsonObject_aufgabe.put("aufgabe_notiz", aufgabe.getNotiz());
                    Calendar deadline = aufgabe.getDeadline();
                    if (deadline != null) {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", deadline.get(Calendar.YEAR));
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", deadline.get(Calendar.MONTH));
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", deadline.get(Calendar.DAY_OF_MONTH));
                    } else {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", -1);
                    }

                    jsonObject_aufgabe.put("aufgabe_prioritaet", aufgabe.getPrioritaet());
                    jsonObject_aufgabe.put("aufgabe_rubrikName", aufgabe.getRubrikName());

                    // get Teilaufgaben and pack them into the jsonObject as an JSONArray
                    ArrayList<Teilaufgabe> teilaufgaben = aufgabe.getTeilaufgabenArrayList(getActivity());
                    JSONArray jsonArray_teilaufgaben = new JSONArray();

                    for (int i2 = 0; i2 < teilaufgaben.size(); i2++) {
                        JSONObject jsonObject_teilaufgabe = new JSONObject();
                        Teilaufgabe teilaufgabe = teilaufgaben.get(i2);
                        jsonObject_teilaufgabe.put("teilaufgabe_uuid", teilaufgabe.getId().toString());
                        jsonObject_teilaufgabe.put("teilaufgabe_title", teilaufgabe.getTitle());
                        int done = (teilaufgabe.isDone()) ? 1 : 0;
                        jsonObject_teilaufgabe.put("teilaufgabe_done", done);

                        jsonArray_teilaufgaben.put(jsonObject_teilaufgabe);
                    }
                    jsonObject_aufgabe.put("teilaufgaben_jsonArray", jsonArray_teilaufgaben);

                    jsonArray_aufgaben.put(jsonObject_aufgabe);
                    // weiter mit Teilaufgaben
                }
                jsonObject.put("aufgaben_jsonArray", jsonArray_aufgaben);

                Log.i("Data prepared: ", jsonObject.toString());

                URL url = new URL("http://provelopment-server.de/Primelist/sendData_Rubrik.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonObject.toString());

                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
                Log.i("Response: ", response);

            } catch (Exception e) {
                e.printStackTrace();
            }


            try {

            } catch (Exception e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

        }
    }

    class SendData_Rubrik_withDataCheck extends AsyncTask<String, String, String> {

        String rubrikUUID;
        String userId;
        String userId_otherUser;
        String userKey_otherUser;

        Boolean dataIsValid;

        protected String doInBackground(String... params) {

            // Load SharedPreferences
            SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
            userId = Integer.toString(settings.getInt(Constants.USER_ID, 0));
            userId_otherUser = params[0];
            userKey_otherUser = params[1];
            rubrikUUID = params[2];


            Rubrik rubrik = mRubrikLab.getRubrik(UUID.fromString(rubrikUUID));
            String response = "";

            try {
                // Create JSONObject which will be send to Server
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                jsonObject.put("userId_otherUser", userId_otherUser);
                jsonObject.put("userKey_otherUser", userKey_otherUser);
                jsonObject.put("rubrik_uuid", rubrikUUID);
                jsonObject.put("rubrik_name", rubrik.getTitle());

                ArrayList<Aufgabe> aufgaben = rubrik.getAufgabenArrayList(getActivity());

                JSONArray jsonArray_aufgaben = new JSONArray();

                for (int i = 0; i < aufgaben.size(); i++) {
                    JSONObject jsonObject_aufgabe = new JSONObject();
                    Aufgabe aufgabe = aufgaben.get(i);
                    jsonObject_aufgabe.put("aufgabe_uuid", aufgabe.getId());
                    jsonObject_aufgabe.put("aufgabe_title", aufgabe.getTitle());
                    if (aufgabe.getNotiz() == null) {
                        aufgabe.setNotiz("", false);
                    }
                    jsonObject_aufgabe.put("aufgabe_notiz", aufgabe.getNotiz());
                    Calendar deadline = aufgabe.getDeadline();
                    if (deadline != null) {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", deadline.get(Calendar.YEAR));
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", deadline.get(Calendar.MONTH));
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", deadline.get(Calendar.DAY_OF_MONTH));
                    } else {
                        jsonObject_aufgabe.put("aufgabe_jahrDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_monatDeadline", -1);
                        jsonObject_aufgabe.put("aufgabe_tagDeadline", -1);
                    }

                    jsonObject_aufgabe.put("aufgabe_prioritaet", aufgabe.getPrioritaet());
                    jsonObject_aufgabe.put("aufgabe_rubrikName", aufgabe.getRubrikName());

                    // get Teilaufgaben and pack them into the jsonObject as an JSONArray
                    ArrayList<Teilaufgabe> teilaufgaben = aufgabe.getTeilaufgabenArrayList(getActivity());
                    JSONArray jsonArray_teilaufgaben = new JSONArray();

                    for (int i2 = 0; i2 < teilaufgaben.size(); i2++) {
                        JSONObject jsonObject_teilaufgabe = new JSONObject();
                        Teilaufgabe teilaufgabe = teilaufgaben.get(i2);
                        jsonObject_teilaufgabe.put("teilaufgabe_uuid", teilaufgabe.getId().toString());
                        jsonObject_teilaufgabe.put("teilaufgabe_title", teilaufgabe.getTitle());
                        int done = (teilaufgabe.isDone()) ? 1 : 0;
                        jsonObject_teilaufgabe.put("teilaufgabe_done", done);

                        jsonArray_teilaufgaben.put(jsonObject_teilaufgabe);
                    }
                    jsonObject_aufgabe.put("teilaufgaben_jsonArray", jsonArray_teilaufgaben);

                    jsonArray_aufgaben.put(jsonObject_aufgabe);
                    // weiter mit Teilaufgaben
                }
                jsonObject.put("aufgaben_jsonArray", jsonArray_aufgaben);

                Log.i("Data prepared: ", jsonObject.toString());

                URL url = new URL("http://provelopment-server.de/Primelist/sendData_Rubrik_withDataCheck.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonObject.toString());

                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }


                Log.i("Response: ", response);


                if (Integer.parseInt(response) == 0) {
                    dataIsValid = false;
                } else {
                    dataIsValid = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (dataIsValid) {
                Rubrik rubrik = RubrikLab.get(getActivity()).getRubrik(UUID.fromString(rubrikUUID));
                rubrik.setIsConnected(true);
            } else {
                Toast.makeText(getActivity(),getActivity().getString(R.string.wrong_UserId_OR_Key), Toast.LENGTH_SHORT).show();
            }

        }
    }

    class GetUserInformation extends AsyncTask<String, String, String> {

        int userId;
        int userKey;

        protected String doInBackground(String... params) {

            SharedPreferences settings = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
            userId = settings.getInt(Constants.USER_ID, 0);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("USER_ID", Integer.toString(userId));
            String response = "";

            try {
                URL url = new URL("http://provelopment-server.de/Primelist/get_UserKey.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(map));

                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
                Log.i("Response: ", response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Option 1 -- JSONObject
            try {
                JSONObject jObjPost = new JSONObject(response);
                int Response_Code = jObjPost.getInt("RESULT");

                if (Response_Code == 1) {
                    userKey= jObjPost.getInt("USER_KEY");

                } else {
                    userKey = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            // open Dialog and submit information
            DialogFragment dialog = new Dialog_UserInformation();

            // deliver a bundle to DialogFragment
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.USER_ID, userId);
            bundle.putInt(Constants.USER_KEY, userKey);
            dialog.setArguments(bundle);

            // start DialogFragment
            dialog.show(getActivity().getSupportFragmentManager(), "dialog_check_aufgabe");
        }
    }

    // Needed Helper Method for the AsyncTask
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Log.i("Code Username: ", result.toString());
        return result.toString();
    }
}
