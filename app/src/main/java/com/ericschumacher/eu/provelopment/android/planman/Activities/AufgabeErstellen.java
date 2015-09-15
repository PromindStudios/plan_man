package com.ericschumacher.eu.provelopment.android.planman.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ericschumacher.eu.provelopment.android.planman.Aufgaben.Aufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Aufgabe_Delete;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_DatePicker;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_NumberPicker;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Teilaufgabe_Add;
import com.ericschumacher.eu.provelopment.android.planman.Dialogs.Dialog_Teilaufgabe_Edit;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.Constans;
import com.ericschumacher.eu.provelopment.android.planman.HelperClasses.DragSortRecycler;
import com.ericschumacher.eu.provelopment.android.planman.R;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.Rubrik;
import com.ericschumacher.eu.provelopment.android.planman.Rubriken.RubrikLab;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.Teilaufgabe;
import com.ericschumacher.eu.provelopment.android.planman.Teilaufgaben.TeilaufgabenAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by eric on 16.07.2015.
 */
public class AufgabeErstellen extends AppCompatActivity implements Dialog_DatePicker.DatePickerListener, TeilaufgabenAdapter.TeilaufgabenListener, Dialog_Teilaufgabe_Add.DialogTeilaufgabeListener,
        Dialog_Teilaufgabe_Edit.DialogTeilaufgabeEditListener, Dialog_NumberPicker.NumberPickerListener, Dialog_Aufgabe_Delete.DeleteAufgabenListener{


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
    FloatingActionButton fabSave;
    TextView tvTitleAufgabe;
    ScrollView svScrollView;

    private int mPrioritaet;
    private Aufgabe mAufgabe;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aufgabe_erstellen);
        initialize();
        setSupportActionBar(mToolbar);
        aufgabeId = (UUID) getIntent().getSerializableExtra(Constans.ID_AUFGABE);
        rubrikId = (UUID) getIntent().getSerializableExtra(Constans.ID_RUBRIK);
        mRubrik = RubrikLab.get(this).getRubrik(rubrikId);
        mAufgabe = mRubrik.getAufgabe(aufgabeId);
        mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);

        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mAufgabe.getTitle());

        // set up the RecyclerView
        mAdapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this); // muss noch anderer Filename hin!
        rvTeilaufgaben.setAdapter(mAdapter);
        rvTeilaufgaben.setLayoutManager(new com.ericschumacher.eu.provelopment.android.planman.HelperClasses.LinearLayoutManager(this, LinearLayout.VERTICAL, false));


        // test
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


        if (mAufgabe.getDeadline() != null) {
            rgDeadline.check(R.id.rbDatum);
            Calendar cal = mAufgabe.getDeadline();
            GregorianCalendar date = new GregorianCalendar(getYear(cal), getMonth(cal), getDay(cal));
            rbDate.setText("  " + format(date));
            Calendar deadline = mAufgabe.getDeadline();
            rbDays.setText("  " + Integer.toString((int)getNumberOfLeftDays(deadline)) + " " + (getString(R.string.rbDays_Days)));
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
                            mAufgabe.setDeadline(null);
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
                    bundle.putInt(JAHR_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.YEAR));
                    bundle.putInt(MONAT_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.MONTH));
                    bundle.putInt(TAG_TEILAUFGABE, mAufgabe.getDeadline().get(Calendar.DAY_OF_MONTH));

                    DialogFragment newFragment = new Dialog_DatePicker();
                    newFragment.setArguments(bundle);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                } else {
                    DialogFragment newFragment = new Dialog_DatePicker();
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
                switch (checkedId) {
                    case R.id.rbPrioritaet_eins:
                        mAufgabe.setPrioritaet(1);
                        break;
                    case R.id.rbPrioritaet_zwei:
                        mAufgabe.setPrioritaet(2);
                        break;
                    case R.id.rbPrioritaet_drei:
                        mAufgabe.setPrioritaet(3);
                }
            }

        });

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

        etNotiz.setText(mAufgabe.getNotiz());
        etNotiz.addTextChangedListener(new

                                               TextWatcher() {
                                                   @Override
                                                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                   }

                                                   @Override
                                                   public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                       mAufgabe.setNotiz(s.toString());
                                                   }

                                                   @Override
                                                   public void afterTextChanged(Editable s) {
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
                                                       mAufgabe.setTitle(s.toString());
                                                       getSupportActionBar().setTitle(mAufgabe.getTitle());
                                                   }

                                                   @Override
                                                   public void afterTextChanged(Editable s) {
                                                   }
                                               }

        );


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
                } else {
                    finish();
                }
                Log.i("Activity: ", "Beended");
            /*Intent i = new Intent (AufgabeErstellen.this, AufgabenListe.class);
            i.putExtra(AufgabenListe.RUBRIK_ID, mRubrik.getId());
            startActivity(i);
            */

                return true;
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
    public void onDateSelected(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        mAufgabe.setDeadline(cal);


        GregorianCalendar date = new GregorianCalendar(year, month, day);
        rbDate.setText("  " + format(date));
        rbDays.setText("  " + Integer.toString((int) getNumberOfLeftDays(cal)) + " " + (getString(R.string.rbDays_Days)));


    }


    @Override
    public void onAdd() {
        // Create new Teilaufgabe, Add Teilaufgabe to ArrayList, before open a dilaog to add
        Teilaufgabe teilaufgabe = new Teilaufgabe(this);
        mAufgabe.addTeilaufgabe(teilaufgabe);
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
        mAufgabe.deleteTeilaufgabe(teilaufgabe);
        Log.i("Deleted", "1x");
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
        rvTeilaufgaben.setAdapter(adapter);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
    }

    @Override
    public void onTeilaufgabeChecked(UUID uuid) {
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        teilaufgabe.setDone(true);
        //mAufgabe.saveTeilaufgaben();
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        TeilaufgabenAdapter Adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
        rvTeilaufgaben.setAdapter(Adapter);
        Adapter.notifyDataSetChanged();


        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
    }

    @Override
    public void onTeilaufgabeUnChecked(UUID uuid) {
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        teilaufgabe.setDone(false);
        //mAufgabe.saveTeilaufgaben();
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        //mTeilaufgaben = mAufgabe.getTeilaufgabenArrayList(this);
        TeilaufgabenAdapter Adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
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
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
        rvTeilaufgaben.setAdapter(adapter);
    }

    @Override
    public void onMoveDown(int position) {
        int size = mTeilaufgaben.size();
        if (size-1 == position) {
            mTeilaufgaben.add(0, mTeilaufgaben.get(position));
            mTeilaufgaben.remove(position+1);
        } else {
            mTeilaufgaben.add(position+2, mTeilaufgaben.get(position));
            mTeilaufgaben.remove(position);
        }

        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
        rvTeilaufgaben.setAdapter(adapter);
    }

    @Override
    public void onAttachTeilaufgabe(String eingabe, UUID uuid) {
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);

        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        } else {
            teilaufgabe.setTitle(eingabe);
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
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
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
        } else {
            teilaufgabe.setTitle(eingabe);
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
            rvTeilaufgaben.setAdapter(adapter);
        }

        //close Keyboard
        etTitle.clearFocus();

    }

    @Override
    public void onAttachTeilaufgabeAndAdd(String eingabe, UUID uuid) {
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);

        if (eingabe.equals("")) {
            mAufgabe.deleteTeilaufgabe(teilaufgabe);
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        } else {
            teilaufgabe.setTitle(eingabe);
            TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
            rvTeilaufgaben.setAdapter(adapter);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            //rvTeilaufgaben.getLayoutParams().height = (int)px*(mTeilaufgaben.size()+1);
        }

        Teilaufgabe teilaufgabe_ = new Teilaufgabe(this);
        mAufgabe.addTeilaufgabe(teilaufgabe_);
        DialogFragment dialog = new Dialog_Teilaufgabe_Add();
        Bundle bundle = new Bundle();
        bundle.putString(UUID_TEILAUFGABE, (teilaufgabe_.getId()).toString());
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Dialog_Add");


    }

    @Override
    public void onDeleteTeilaufgabe(UUID uuid) {
        Log.i("Deleted", "2x");
        Teilaufgabe teilaufgabe = mAufgabe.getTeilaufgabe(uuid);
        mAufgabe.deleteTeilaufgabe(teilaufgabe);
        TeilaufgabenAdapter adapter = new TeilaufgabenAdapter(this, mTeilaufgaben, AufgabeErstellen.this);
        rvTeilaufgaben.setAdapter(adapter);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        rvTeilaufgaben.getLayoutParams().height = (int) px * (mTeilaufgaben.size() + 1);
    }


    @Override
    public void onDaysSelected(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        mAufgabe.setDeadline(cal);
        GregorianCalendar date = new GregorianCalendar(getYear(cal), getMonth(cal), getDay(cal));
        rbDate.setText("  " + format(date));
        Calendar deadline = cal;
        int Days_Left = (int) getNumberOfLeftDays(deadline);

        rbDays.setText("  " + Integer.toString(Days_Left) + " " + (getString(R.string.rbDays_Days)));
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
        finish();
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
}
