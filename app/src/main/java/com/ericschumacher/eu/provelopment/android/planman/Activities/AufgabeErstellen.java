package com.ericschumacher.eu.provelopment.android.planman.Activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.AppWidget.MyAppWidgetProvider;
import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Delete;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_DatePicker;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Get_Premium;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_NumberPicker;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Rocketplan;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Teilaufgabe_Add;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Teilaufgabe_Edit;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.AnalyticsApplication;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.ColorTheme;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constants;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabHelper;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.IabResult;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.Inventory;
import com.ericschumacher.eu.provelopment.android.planman.InAppPurchase.Purchase;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.TeilaufgabenAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eric on 16.07.2015.
 */
public class AufgabeErstellen extends AppCompatActivity implements Dialog_DatePicker.DatePickerListener, TeilaufgabenAdapter.TeilaufgabenListener, Dialog_Teilaufgabe_Add.DialogTeilaufgabeListener,
        Dialog_Teilaufgabe_Edit.DialogTeilaufgabeEditListener, Dialog_NumberPicker.NumberPickerListener, Dialog_Aufgabe_Delete.DeleteAufgabenListener, Dialog_Get_Premium.PremiumListener {


    private final static String TAG = "AufgabeErstellen";

    // Layout Components
    EditText etTitle;
    TextView tvDeadline;
    EditText etNotiz;
    RadioGroup rgDeadline;
    RadioButton rbDate;
    RadioButton rbDays;
    RadioButton rbKeine;
    RadioGroup rgPrioritaet;
    RadioButton rbPrioritaet_eins;
    RadioButton rbPrioritaet_zwei;
    RadioButton rbPrioritaet_drei;
    RecyclerView rvTeilaufgaben;
    //FloatingActionButton fabSave;
    TextView tvTitleAufgabe;
    ScrollView svScrollView;

    // Analytics
    Tracker mTracker;

    // Synchronisation
    AsyncTask<String, String, String> AT_GetChanges;
    AsyncTask<String, String, String> AT_SaveChanges;
    Boolean mConnected;

    private int mPrioritaet;
    private Aufgabe mAufgabe;
    private Aufgabe oldAufgabe;
    private Rubrik mRubrik;
    private UUID aufgabeId;
    private UUID rubrikId;
    private TeilaufgabenAdapter mAdapter;

    private ArrayList<Teilaufgabe> mTeilaufgaben;
    private String FILENAME;
    private String FILENAME_TEILAUFGABE;

    private Toolbar mToolbar;


    public static final String EXTRA_AUFGABE_ID = "aufgabeintent.AUFGABE_ID";
    public static final String MY_RUBRIK_ID = "aufgabenintent.RUBRIK_ID";
    public static final String TITLE_TEILAUFGABE = "teilaufgabe_constant";
    public static final String UUID_TEILAUFGABE = "uuid_teilaufgabe";

    public static final String JAHR_TEILAUFGABE = "jahr";
    public static final String MONAT_TEILAUFGABE = "monat";
    public static final String TAG_TEILAUFGABE = "tag";

    // Premium

    private boolean mPremium = true;
    private boolean mTestVersion = false;


    // In App Purchase
    private IabHelper mHelper;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener;
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;

    // SharedPreferences
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_erstellen);

        // check if user has Premium!
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        mSharedPreferencesEditor = mSharedPreferences.edit();
        mPremium = mSharedPreferences.getBoolean(Constants.USER_HAS_PREMIUM, false);

        if (mPremium) {
            mTestVersion = false;
            Log.i("Premium_Erstellen: ", "true");
        } else {
            Log.i("Premium_Erstellen: ", "false");
            int number_of_tasks = mSharedPreferences.getInt(Constants.NUMBER_OF_TASKS_CREATED, 0);
            if (number_of_tasks < getResources().getInteger(R.integer.test_version_tasks)) {
                mTestVersion = true;
                Log.i("Test-Version_Erste: ", "true");
            } else {
                mTestVersion = false;
                Log.i("Test-Version_Erste: ", "false");
            }

            // InAppPurchase
            String beat = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjCT9zyzV/Vx58WIOUuLPQUVKLeZopUyuC1ngh4m74lcd0IR7TJLls52Q";
            String getup = "Rxab6FM1t4Qh/KuZ3K3+9JSb0bSchJaE9JPaqoSn7qQb1j4IfeReJkC2YkJ6Vjwc9Io4iOhhKyG4uEFYHl/dc8NX89xwYcFAwmJ6";
            String grind = Constants.GRIND;
            String rise = Constants.RISE;

            mHelper = new IabHelper(this, beat + getup + grind + rise);

            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Log.d("Iab: Problem", "Problem setting up In-app Billing: " + result);
                    } else {
                        Log.i("StartSetup", "Finished");
                        //get Information about available Products
                        ArrayList<String> additionalSkuList = new ArrayList<String>();
                        additionalSkuList.add("primelist_update1");
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    }
                }
            });
        }

        mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    Log.d("Error", "Error purchasing: " + result);
                    return;
                } else
                    Log.i("Purchase: ", "Finished!!");
                mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, true);
                mSharedPreferencesEditor.commit();
                Intent intent = new Intent(AufgabeErstellen.this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                // give user access to premium content and update the UI

            }
        };

        // create mGotInventoryListener
        mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                if (result.isFailure()) {
                    // handle error here
                } else {
                    // does the user have the premium upgrade?
                    Boolean mIsPremium = inventory.hasPurchase("primelist_update1");
                    if (mIsPremium) {
                        Log.i("Premium: ", "true - QueryInventory");
                        mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, true);
                        mSharedPreferencesEditor.commit();
                        mPremium = true;
                        Log.i("Update?: ", "User hat Update auf dem Handy!!");
                    } else {
                        Log.i("Premium: ", "false - QueryInventory");
                        mSharedPreferencesEditor.putBoolean(Constants.USER_HAS_PREMIUM, false);
                        mSharedPreferencesEditor.commit();
                        mPremium = false;
                        Log.i("Update?: ", "User hat kein Premium");
                    }

                    // update UI accordingly
                }
            }
        };

        initialize();
        setSupportActionBar(mToolbar);
        aufgabeId = (UUID) getIntent().getSerializableExtra(Constants.ID_AUFGABE);
        rubrikId = (UUID) getIntent().getSerializableExtra(Constants.ID_RUBRIK);
        mRubrik = RubrikLab.get(this).getRubrik(rubrikId);
        mConnected = mRubrik.isConnected();
        mAufgabe = mRubrik.getAufgabe(aufgabeId);
        mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // set up the RecyclerView
        mAdapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion); // muss noch anderer Filename hin!
        rvTeilaufgaben.setAdapter(mAdapter);
        rvTeilaufgaben.setLayoutManager(new com.ericschumacher.eu.provelopment.android.planman.HelperClasses.LinearLayoutManager(this, LinearLayout.VERTICAL, false));


        // testt
        /*
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        int size_Teilaufgaben;
        if (mTeilaufgaben != null) {
            size_Teilaufgaben = mTeilaufgaben.size();
        } else {
            size_Teilaufgaben = 0;
        }
        */

        updateUI();


        rgDeadline.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbDatum:

                        break;
                    case R.id.rbDays:

                        break;
                    case R.id.rbKeine:

                        if (mAufgabe.getDeadline() != null) {
                            //AT_GetChanges.cancel(true);
                            //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                            mAufgabe.setDeadline(null);
                            saveChanges();
                        }
                        rbDays.setText("  " + (getString(R.string.rbDays_Days)));
                        rbDate.setText("  " + (getString(R.string.rbDate_Date)));
                }
            }

        });

        rbDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAufgabe.getDeadline() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.JAHR_AUFGABE, mAufgabe.getDeadline().get(Calendar.YEAR));
                    bundle.putInt(Constants.MONAT_AUFGABE, mAufgabe.getDeadline().get(Calendar.MONTH));
                    bundle.putInt(Constants.TAG_AUFGABE, mAufgabe.getDeadline().get(Calendar.DAY_OF_MONTH));
                    bundle.putString(Constants.ID_AUFGABE, "null");
                    DialogFragment newFragment = new Dialog_DatePicker();
                    newFragment.setArguments(bundle);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                } else {
                    Calendar today = Calendar.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.JAHR_AUFGABE, today.get(Calendar.YEAR));
                    bundle.putInt(Constants.MONAT_AUFGABE, today.get(Calendar.MONTH));
                    bundle.putInt(Constants.TAG_AUFGABE, today.get(Calendar.DAY_OF_MONTH));
                    bundle.putString(Constants.ID_AUFGABE, "null");
                    DialogFragment newFragment = new Dialog_DatePicker();
                    newFragment.setArguments(bundle);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                }


            }
        });
        rbDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAufgabe.getDeadline() != null) {
                    Bundle bundle = new Bundle();
                    Log.i(TAG, "Calendar not null");
                    bundle.putInt(JAHR_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.YEAR));
                    bundle.putInt(MONAT_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.MONTH));
                    bundle.putInt(TAG_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.DAY_OF_MONTH));

                    Log.i("Tage: ", Integer.toString(mAufgabe.getDeadline().get(Calendar.DAY_OF_MONTH)));
                    DialogFragment newFragment = new Dialog_NumberPicker();
                    newFragment.setArguments(bundle);
                    newFragment.show(getSupportFragmentManager(), "numberPicker");
                } else {
                    DialogFragment newFragment = new Dialog_NumberPicker();
                    newFragment.show(getSupportFragmentManager(), "numberPicker");
                }
            }
        });

        rgPrioritaet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mPremium || mTestVersion) {
                    switch (checkedId) {
                        case R.id.rbPrioritaet_eins:
                            //AT_GetChanges.cancel(true);
                            //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                            mAufgabe.setPrioritaet(1);
                            saveChanges();
                            break;
                        case R.id.rbPrioritaet_zwei:
                            //AT_GetChanges.cancel(true);
                            //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                            mAufgabe.setPrioritaet(2);
                            saveChanges();
                            break;
                        case R.id.rbPrioritaet_drei:
                            //AT_GetChanges.cancel(true);
                            //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                            mAufgabe.setPrioritaet(3);
                            saveChanges();
                    }
                } else {
                    openDialog_FunctionDeactivated();
                    mAufgabe.setPrioritaet(3);
                }

            }

        });


        //etNotiz.setText(mAufgabe.getNotiz());
        etNotiz.addTextChangedListener(new

                                               TextWatcher() {
                                                   @Override
                                                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                   }

                                                   @Override
                                                   public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                   }

                                                   @Override
                                                   public void afterTextChanged(Editable s) {
                                                       //AT_GetChanges.cancel(true);
                                                       //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                                                       mAufgabe.setNotiz(s.toString(), mConnected);
                                                       //saveChanges();
                                                   }
                                               }

        );

        etTitle.addTextChangedListener(new

                                               TextWatcher() {
                                                   @Override
                                                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                   }

                                                   @Override
                                                   public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                   }

                                                   @Override
                                                   public void afterTextChanged(Editable s) {
                                                       //AT_GetChanges.cancel(true);
                                                       //mAufgabe = mRubrik.getAufgabe(aufgabeId);
                                                       mAufgabe.setTitle(s.toString());
                                                       getSupportActionBar().setTitle(mAufgabe.getTitle());
                                                       saveChanges();
                                                   }
                                               }

        );

        /*
        fabSave = (FloatingActionButton) findViewById(R.id.fabSave);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAufgabe.getTitle().equals("") || mAufgabe.getTitle() == null) {
                    mRubrik.deleteAufgabe(mAufgabe);
                    finish();
                } else {
                    finish();
                }
            }
        });
        */


        // set up Analytics
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // set Colors
        ColorTheme colorTheme = new ColorTheme(this);

        // colorComponents
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, colorTheme.getColorPrimary()));

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, colorTheme.getColorPrimaryDark()));

            // set Color of RadioButtons
            rbDate.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));
            rbDays.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));
            rbKeine.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));
            rbPrioritaet_drei.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));
            rbPrioritaet_zwei.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));
            rbPrioritaet_eins.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));

            //fabSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorTheme.getColorPrimary())));

        } else {
            int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
            rbDate.setButtonDrawable(id);
            rbDays.setButtonDrawable(id);
            rbKeine.setButtonDrawable(id);
            rbPrioritaet_drei.setButtonDrawable(id);
            rbPrioritaet_zwei.setButtonDrawable(id);
            rbPrioritaet_eins.setButtonDrawable(id);
        }


        // Add Rocketplan

        int counter = mSharedPreferences.getInt(Constants.AD_ROCKETPLAN_COUNTER, 0);
        mSharedPreferencesEditor.putInt(Constants.AD_ROCKETPLAN_COUNTER, counter + 1);
        mSharedPreferencesEditor.commit();
        //if (counter % 5 == 0) {
        if (false) {
            DialogFragment dialog = new Dialog_Rocketplan();
            dialog.show(getSupportFragmentManager(), "dialog_rocketplan");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (etTitle.getText().equals("")) {
            etTitle.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
            Log.i("Equal: ", "not working");

        } else {
            Log.i("Equal: ", "working");

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
            svScrollView.smoothScrollTo(0, 0);

        }

        /* how synchronisation used to be handled
        // start looking for Changes
        AT_GetChanges = new GetChanges();
        AT_GetChanges.execute();

        */

        // Set up name for Analytics
        mTracker.setScreenName("Image~" + "AufgabeErstellen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    protected void onPostResume() {

        try {
            super.onPostResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aufgabeerstellen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ic_delete) {
            Log.i("Activity: ", "Geloescht");
            DialogFragment dialog = new Dialog_Aufgabe_Delete();
            dialog.show(getSupportFragmentManager(), "delete_aufgabe_dialog_fragment");
            // muss noch berücksichtigt werden

            return true;

        } else {

            if (id == R.id.ic_save_menu) {
                if (mAufgabe.getTitle().equals("") || mAufgabe.getTitle() == null) {
                    mRubrik.deleteAufgabe(mAufgabe);
                    finish();
                } else {
                    finish();
                }
                return true;

            } else {
                if (mAufgabe.getTitle().equals("") || mAufgabe.getTitle() == null) {
                    mRubrik.deleteAufgabe(mAufgabe);
                    finish();
                    return true;
                } else {
                    Log.i("Activity: ", "Beended");
                    finish();
                    return true;

                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        String Filename = mRubrik.getFilenameAufgaben();
        Log.i("Filename!: ", Filename);
        mRubrik.saveAufgaben();
        mAufgabe.saveTeilaufgaben();

        /* how changes used to be handled

        // stop looking for Changes;
        AT_GetChanges.cancel(true);

        // start last update
        Intent s = new Intent(this, UpdateRubrik.class);
        s.putExtra(Constants.RUBRIK_UUID, mRubrik.getId().toString());
        startService(s);

        */

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName name = new ComponentName(this, MyAppWidgetProvider.class);
        int[] ids = appWidgetManager.getAppWidgetIds(name);
        int length = ids.length;
        for (int i = 0; i < length; i++) {
            appWidgetManager.notifyAppWidgetViewDataChanged(ids[i], R.id.lvAppWidget);
            Log.i("Main: ", "Paused: Update App Widget");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAufgabe.getTitle().equals("") || mAufgabe.getTitle() == null) {
            mRubrik.deleteAufgabe(mAufgabe);
            finish();
        } else {
            finish();
        }
    }

    private void initialize() {
        etTitle = (EditText) findViewById(R.id.etTitel);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        etNotiz = (EditText) findViewById(R.id.etNotiz);
        rvTeilaufgaben = (RecyclerView) findViewById(R.id.rvTeilaufgaben);
        rgDeadline = (RadioGroup) findViewById(R.id.rgDeadline);
        rbDate = (RadioButton) findViewById(R.id.rbDatum);
        rbDays = (RadioButton) findViewById(R.id.rbDays);
        rbKeine = (RadioButton) findViewById(R.id.rbKeine);
        rbKeine.setText("  " + getString(R.string.Keine));
        rgPrioritaet = (RadioGroup) findViewById(R.id.rgPrioritaet);
        rbPrioritaet_zwei = (RadioButton) findViewById(R.id.rbPrioritaet_zwei);
        rbPrioritaet_zwei.setText("  " + getString(R.string.Prioritaet_zwei));
        rbPrioritaet_eins = (RadioButton) findViewById(R.id.rbPrioritaet_eins);
        rbPrioritaet_eins.setText("  " + getString(R.string.Prioritaet_eins));
        rbPrioritaet_drei = (RadioButton) findViewById(R.id.rbPrioritaet_drei);
        rbPrioritaet_drei.setText("  " + getString(R.string.Prioritaet_drei));
        tvTitleAufgabe = (TextView) findViewById(R.id.tvTitle_aufgabe);
        svScrollView = (ScrollView) findViewById(R.id.svAufgabeErstellen);


    }

    @Override
    public void onDateSelected(int year, int month, int day, String uuid) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);


        mAufgabe.setDeadline(cal);
        saveChanges();

        GregorianCalendar date = new GregorianCalendar(year, month, day);
        rbDate.setText("  " + format(date));
        int days_left = (int) getNumberOfLeftDays(cal);
        if (days_left == 1) {
            rbDays.setText("  " + Integer.toString(days_left) + " " + (getString(R.string.rbDays_Day)));
        } else {
            rbDays.setText("  " + Integer.toString((int) getNumberOfLeftDays(cal)) + " " + (getString(R.string.rbDays_Days)));
        }
    }


    @Override
    public void onAdd() {
        // Create new Teilaufgabe, Add Teilaufgabe to ArrayList, before open a dilaog to add
        Teilaufgabe teilaufgabe = new Teilaufgabe(this);
        //AT_GetChanges.cancel(true);
        //mAufgabe = mRubrik.getAufgabe(aufgabeId);
        mAufgabe.addTeilaufgabe(teilaufgabe);
        saveChanges();
        DialogFragment dialog = new Dialog_Teilaufgabe_Add();
        Bundle bundle = new Bundle();
        bundle.putString(UUID_TEILAUFGABE, (teilaufgabe.getId()).toString());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Dialog_Add");

    }

    @Override
    public void onEdit(UUID uuid) {
        DialogFragment dialog = new Dialog_Teilaufgabe_Edit();
        Bundle bundle = new Bundle();
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        bundle.putString(UUID_TEILAUFGABE, (teilaufgabe.getId().toString()));
        bundle.putString(TITLE_TEILAUFGABE, (teilaufgabe.getTitle().toString()));
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Dialog_Edit");
    }

    @Override
    public void onDelete(UUID uuid, boolean checked) {
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        //AT_GetChanges.cancel(true);
        //mAufgabe = mRubrik.getAufgabe(aufgabeId);
        mAufgabe.deleteTeilaufgabe(teilaufgabe);
        saveChanges();
        Log.i("Deleted", "1x");
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(adapter);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
    }

    @Override
    public void onTeilaufgabeChecked(UUID uuid) {

        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        teilaufgabe.setDone(true);
        saveChanges();
        //mAufgabe.saveTeilaufgaben();
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        TeilaufgabenAdapter Adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(Adapter);
        Adapter.notifyDataSetChanged();


        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
    }

    @Override
    public void onTeilaufgabeUnChecked(UUID uuid) {

        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        teilaufgabe.setDone(false);
        saveChanges();
        //mAufgabe.saveTeilaufgaben();
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        TeilaufgabenAdapter Adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(Adapter);
        Adapter.notifyDataSetChanged();

        //mAdapter.notifyDataSetChanged();


        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
    }

    @Override
    public void onMoveUp(int position) {
        if (position == 0) {
            mTeilaufgaben.add(mTeilaufgaben.get(position));
            mTeilaufgaben.remove(0);
        } else {
            mTeilaufgaben.add(position - 1, mTeilaufgaben.get(position));
            mTeilaufgaben.remove(position + 1);
        }
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(adapter);
    }

    @Override
    public void onMoveDown(int position) {
        int size = mTeilaufgaben.size();
        if (size - 1 == position) {
            mTeilaufgaben.add(0, mTeilaufgaben.get(position));
            mTeilaufgaben.remove(position + 1);
        } else {
            mTeilaufgaben.add(position + 2, mTeilaufgaben.get(position));
            mTeilaufgaben.remove(position);
        }

        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(adapter);
    }

    @Override
    public void onFunctionDeactivatd() {
        openDialog_FunctionDeactivated();
    }

    @Override
    public void onAttachTeilaufgabe(String eingabe, UUID uuid) {
        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);

        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
            saveChanges();
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        } else {
            teilaufgabe.setTitle(eingabe);
            saveChanges();
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        }

        //close Keyboard
        etTitle.clearFocus();
    }

    @Override
    public void onHandleTeilaufgabeEditName(UUID uuid, String eingabe) {
        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
            saveChanges();
        } else {
            teilaufgabe.setTitle(eingabe);
            saveChanges();
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
            rvTeilaufgaben.setAdapter(adapter);
        }

        //close Keyboard
        etTitle.clearFocus();

    }

    @Override
    public void onAttachTeilaufgabeAndAdd(String eingabe, UUID uuid) {
        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);

        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
            saveChanges();
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        } else {
            teilaufgabe.setTitle(eingabe);
            saveChanges();
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        }

        Teilaufgabe teilaufgabe_ = new Teilaufgabe(this);
        mAufgabe.addTeilaufgabe(teilaufgabe_);
        saveChanges();
        DialogFragment dialog = new Dialog_Teilaufgabe_Add();
        Bundle bundle = new Bundle();
        bundle.putString(UUID_TEILAUFGABE, (teilaufgabe_.getId()).toString());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Dialog_Add");


    }

    @Override
    public void onDeleteTeilaufgabe(UUID uuid) {
        Log.i("Deleted", "2x");
        //AT_GetChanges.cancel(true);
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        mAufgabe.deleteTeilaufgabe(teilaufgabe);
        saveChanges();
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this, mPremium, mTestVersion);
        rvTeilaufgaben.setAdapter(adapter);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        rvTeilaufgaben.getLayoutParams().height = (int) px * (mTeilaufgaben.size() + 1);
    }


    @Override
    public void onDaysSelected(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        //AT_GetChanges.cancel(true);
        mAufgabe.setDeadline(cal);
        saveChanges();
        GregorianCalendar date = new GregorianCalendar(getYear(cal), getMonth(cal), getDay(cal));
        rbDate.setText("  " + format(date));
        Calendar deadline = cal;
        int days_left = (int) getNumberOfLeftDays(deadline);
        if (days_left == 1) {
            rbDays.setText("  " + Integer.toString(days_left) + " " + (getString(R.string.rbDays_Day)));
        } else {
            rbDays.setText("  " + Integer.toString((int) getNumberOfLeftDays(deadline)) + " " + (getString(R.string.rbDays_Days)));
        }
    }

    public static String format(GregorianCalendar calendar) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd. MMMM yyyy");
        fmt.setCalendar(calendar);
        String dateFormatted = fmt.format(calendar.getTime());
        return dateFormatted;
    }

    public int getDay(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth(Calendar cal) {
        return cal.get(Calendar.MONTH);
    }

    public int getYear(Calendar cal) {
        return cal.get(Calendar.YEAR);
    }

    @Override
    public void deleteAufgabe() {
        mRubrik.deleteAufgabe(mAufgabe);
        mRubrik.saveAufgaben();
        finish();
    }

    @Override
    public void onPremiumSelected() {
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

        int n = new Random().nextInt(10001);
        String s = UUID.randomUUID().toString();
        int number = settings.getInt(Constants.RESPONSE_CODE_PURCHASE, n);
        String text = settings.getString(Constants.UNIQUE_STRING_PURCHASE, s);

        try {
            mHelper.launchPurchaseFlow(this, "primelist_update1", number, mPurchaseFinishedListener, s);
        } catch (Exception e) {

        }

    }

    public interface BackButtonListener {
        public void onBackButtonPressed();
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

    class GetChanges extends AsyncTask<String, String, String> {

        String uuidChange;
        Boolean changesMade;

        protected String doInBackground(String... params) {

            Log.i("CheckForChanges: ", "Checks!");

            // Do work
            String response = "";


            // get Shared Preferences
            SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

            try {
                // Create JSONObject which will be send to Server
                JSONObject JsonObject = new JSONObject();

                int user_id = settings.getInt(Constants.USER_ID, 0);
                JsonObject.put("user_id", Integer.toString(user_id));

                Log.i("Data prepared: ", JsonObject.toString());

                URL url = new URL("http://provelopment-server.de/Primelist/checkForChanges.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(JsonObject.toString());

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
                //Log.i("Response_CheckChanges: ", response);

                JSONObject jObjPost = new JSONObject(response);

                if (jObjPost.getInt("result") == 1) {
                    // get Rubrik
                    changesMade = true;

                    RubrikLab mRubrikLab = RubrikLab.get(AufgabeErstellen.this);
                    Rubrik newRubrik = new Rubrik(jObjPost.getString("rubrikName"), getApplicationContext());
                    newRubrik.setId(UUID.fromString(jObjPost.getString("rubrikUUID")));
                    newRubrik.setIsConnected(true);

                    // get old Rubrik and delete it
                    uuidChange = jObjPost.getString("rubrikUUID");
                    Rubrik oldRubrik = mRubrikLab.getRubrik(UUID.fromString(uuidChange));

                    mRubrikLab.deleteRubrik(oldRubrik);

                    mRubrikLab.addRubrik(newRubrik);

                    // delete all Aufgaben in Rubrik
                    //newRubrik.deleteAufgabenArrayList();

                    // get AufgabenArray
                    JSONArray aufgabenArray = jObjPost.getJSONArray("aufgabenArray");

                    for (int i = 0; i < aufgabenArray.length(); i++) {
                        JSONObject jsonObject = aufgabenArray.getJSONObject(i);
                        // Error
                        Aufgabe aufgabe = new Aufgabe(jsonObject.getString("rubrikName"));
                        try {
                            aufgabe.setId(UUID.fromString(jsonObject.getString("uuid")));
                        } catch (IllegalArgumentException e) {
                            aufgabe.setId(UUID.randomUUID());
                        }
                        aufgabe.setTitle(jsonObject.getString("title"));
                        aufgabe.setNotiz(jsonObject.getString("notiz"), false);

                        if (jsonObject.getInt("jahrDeadline") > 0) {
                            Calendar deadline = Calendar.getInstance();
                            deadline.set(Calendar.DAY_OF_MONTH, jsonObject.getInt("tagDeadline"));
                            deadline.set(Calendar.MONTH, jsonObject.getInt("monatDeadline"));
                            deadline.set(Calendar.YEAR, jsonObject.getInt("jahrDeadline"));
                            aufgabe.setDeadline(deadline);
                        } else {
                            aufgabe.setDeadline(null);
                        }
                        aufgabe.setPrioritaet(jsonObject.getInt("prioritaet"));

                        // get TeilaufgabenArray
                        JSONArray teilaufgabenArray = jsonObject.getJSONArray("teilaufgabenArray");

                        for (int i2 = 0; i < teilaufgabenArray.length(); i++) {
                            JSONObject jsonObject2 = aufgabenArray.getJSONObject(i2);
                            Teilaufgabe teilaufgabe = new Teilaufgabe(getApplicationContext());
                            try {
                                teilaufgabe.setId(UUID.fromString(jsonObject2.getString("uuid")));
                            } catch (IllegalArgumentException ex) {
                                teilaufgabe.setId(UUID.randomUUID());
                            }

                            teilaufgabe.setTitle(jsonObject2.getString("title"));
                            teilaufgabe.setDone(jsonObject2.getBoolean("done"));
                            aufgabe.addTeilaufgabe(teilaufgabe);
                        }

                        newRubrik.addAufgabe(aufgabe);
                        aufgabe.saveTeilaufgaben();
                    }
                    mRubrikLab.saveRubriken();
                    newRubrik.saveAufgaben();
                    Log.i("CheckForChanges: ", "changes made!");

                    // restart Service quicker
                } else {
                    changesMade = false;
                    Log.i("CheckForChanges: ", "no changes!");

                    // restart Service
                }

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
            // set layout here constant, if uuid are the same and there have been changes
            if (changesMade || mRubrik.getId().toString().equals(uuidChange)) {
                mRubrik = RubrikLab.get(AufgabeErstellen.this).getRubrik(UUID.fromString(uuidChange));
                mAufgabe = mRubrik.getAufgabe(aufgabeId);
                updateUI();
            }

            restartGetChanges();


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

    private void updateUI() {
        getSupportActionBar().setTitle(mAufgabe.getTitle());

        if (mAufgabe.getDeadline() != null) {
            rgDeadline.check(R.id.rbDatum);
            Calendar cal = mAufgabe.getDeadline();
            GregorianCalendar date = new GregorianCalendar(getYear(cal), getMonth(cal), getDay(cal));
            rbDate.setText("  " + format(date));
            Calendar deadline = mAufgabe.getDeadline();
            int days_left = (int) getNumberOfLeftDays(deadline);
            if (days_left == 1) {
                rbDays.setText("  " + Integer.toString(days_left) + " " + (getString(R.string.rbDays_Day)));
            } else {
                rbDays.setText("  " + Integer.toString((int) getNumberOfLeftDays(deadline)) + " " + (getString(R.string.rbDays_Days)));
            }
        } else {
            rgDeadline.check(R.id.rbKeine);
            rbDays.setText("  " + (getString(R.string.rbDays_Days)));
            rbDate.setText("  " + (getString(R.string.rbDate_Date)));
        }

        if (mAufgabe.getNotiz() != null) {
            etNotiz.setText(mAufgabe.getNotiz());
        }

        if (mAufgabe.getTitle() != null) {
            etTitle.setText(mAufgabe.getTitle());
            if (mAufgabe.getTitle().equals("")) {
                Log.i("NULL: ", "Erkannt");
                etTitle.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        mPrioritaet = mAufgabe.getPrioritaet();
        switch (mPrioritaet) {
            case 1:
                rgPrioritaet.check(R.id.rbPrioritaet_eins);
                break;
            case 2:
                rgPrioritaet.check(R.id.rbPrioritaet_zwei);
                break;
            case 3:
                rgPrioritaet.check(R.id.rbPrioritaet_drei);
        }
    }

    class SaveChanges extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {


            //Rubrik rubrik = RubrikLab.get(AufgabeErstellen.this).getRubrik(UUID.fromString(params[0]));
            String response = "";

            // get Shared Preferences
            SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);

            try {
                // Create JSONObject which will be send to Server
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rubrik_uuid", mRubrik.getId().toString());
                jsonObject.put("rubrik_name", mRubrik.getTitle());
                int user_id = settings.getInt(Constants.USER_ID, 0);
                jsonObject.put("user_id", Integer.toString(user_id));

                ArrayList<Aufgabe> aufgaben = mRubrik.getAufgabenArrayList(AufgabeErstellen.this);

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
                    ArrayList<Teilaufgabe> teilaufgaben = aufgabe.getTeilaufgabenArrayList(AufgabeErstellen.this);
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
                Log.i("Response_upd.Rubrik: ", response);

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            //AT_GetChanges = new GetChanges();
            //AT_GetChanges.execute();
        }

    }

    public void restartGetChanges() {
        AT_GetChanges.cancel(true);
        AT_GetChanges = new GetChanges();
        AT_GetChanges.execute();
    }

    public void saveChanges() {

        /*
        mRubrik.saveAufgaben();
        // check if we have to cancel saveChanges

        if (AT_SaveChanges != null) {
            AT_SaveChanges.cancel(true);
        }

        AT_SaveChanges = new SaveChanges();
        String[] data = {rubrikId.toString()};
        AT_SaveChanges.execute(data);

        */
    }

    private void openDialog_FunctionDeactivated() {
        DialogFragment dialog = new Dialog_Get_Premium();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TITLE, getString(R.string.dialog_getPremium_Title_Deaktiviert));
        bundle.putString(Constants.DESCRIPTION, getString(R.string.dialog_getPremium_Description_Deaktiviert));
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialog_check_aufgabe");
    }
}
