/*
 * Copyright (C) 2015 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.darbose.classroom.attendance;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.darbose.classroom.R;
import in.darbose.classroom.adapters.AttendanceAdapter;
import in.darbose.classroom.database.DatabaseManager;
import in.darbose.classroom.date_time_pickers.CustomDatePickerDialog;
import in.darbose.classroom.date_time_pickers.CustomTimePickerDialog;
import in.darbose.classroom.date_time_pickers.DatePickerFragment;
import in.darbose.classroom.date_time_pickers.TimePickerFragment;
import in.darbose.classroom.interfaces.BackNavigationListener;
import in.darbose.classroom.model.Classroom;
import in.darbose.classroom.model.Student;
import in.darbose.classroom.past_attendances.PastAttendancesListActivity;

/**
 * Created by ferid.cafer on 4/16/2015.
 */
public class AttendanceActivity extends AppCompatActivity implements BackNavigationListener {
    private Context context;
    private Toolbar toolbar;

    private ListView list;
    private ArrayList<Student> arrayList;
    private AttendanceAdapter adapter;

    private Classroom classroom;
    private String classDate = "";

    //date and time pickers
    private DatePickerFragment datePickerFragment;
    private TimePickerFragment timePickerFragment;
    private CustomDatePickerDialog datePickerDialog;
    private CustomTimePickerDialog timePickerDialog;
    private Date changedDate;

    private FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_listview_with_toolbar);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            classroom = (Classroom) args.getSerializable("classroom");
        }

        context = this;

        //toolbar
        setToolbar();

        list = (ListView) findViewById(R.id.list);
        arrayList = new ArrayList<Student>();
        adapter = new AttendanceAdapter(context, R.layout.checkable_text_item, arrayList);
        list.setAdapter(adapter);

        //empty list view text
        TextView emptyText = (TextView) findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.emptyMessageSave));
        list.setEmptyView(emptyText);

        setListItemClickListener();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        startButtonAnimation();

        new SelectStudents().execute();
    }

    /**
     * Create toolbar and set its attributes
     */
    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Date dateTime = new Date();
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        classDate = targetFormat.format(dateTime);

        setTitle(classroom.getName());
        toolbar.setSubtitle(classDate);
    }

    /**
     * Set floating action button with its animation
     */
    private void startButtonAnimation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.SlideInUp).playOn(floatingActionButton);
                floatingActionButton.setImageResource(R.drawable.ic_action_save);
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        }, 400);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewAttendance();
            }
        });
    }

    /**
     * setOnItemClickListener
     */
    private void setListItemClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList.size() > position) {
                    Student student = arrayList.get(position);
                    boolean isPresent = !student.isPresent();
                    arrayList.get(position).setPresent(isPresent);

                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Shows date picker
     */
    private void changeDate() {
        if (Build.VERSION.SDK_INT < 21) {
            datePickerDialog = new CustomDatePickerDialog(context);
            datePickerDialog.show();
        } else {
            datePickerFragment = new DatePickerFragment();
            datePickerFragment.show(getSupportFragmentManager(), "DatePickerFragment");
        }
    }

    /**
     * Shows time picker
     */
    private void changeTime() {
        if (Build.VERSION.SDK_INT < 21) {
            timePickerDialog = new CustomTimePickerDialog(context);
            timePickerDialog.show();
        } else {
            timePickerFragment = new TimePickerFragment();
            timePickerFragment.show(getSupportFragmentManager(), "TimePickerFragment");
        }
    }

    /**
     * Makes the change both on variable that will be send to DB and on the toolbar subtitle
     */
    private void changeDateTime() {
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        classDate = targetFormat.format(changedDate);
        toolbar.setSubtitle(classDate);
    }

    @Override
    public void OnPress(int dayOfMonth, int month, int year) {
        changedDate.setYear(year - 1900);
        changedDate.setMonth(month);
        changedDate.setDate(dayOfMonth);

        changeTime();
    }

    @Override
    public void OnPress(int minute, int hour) {
        changedDate.setHours(hour);
        changedDate.setMinutes(minute);

        changeDateTime();
    }

    /**
     * Go to past attendaces of the given classroom
     */
    private void goToPastAttendances() {
        Intent intent = new Intent(context, PastAttendancesListActivity.class);
        intent.putExtra("classroom", classroom);
        startActivity(intent);
        overridePendingTransition(R.anim.move_in_from_bottom, R.anim.stand_still);
    }

    /**
     * Inserts a new attendance after check its existence
     */
    private void insertNewAttendance() {
        new IsAlreadyExist().execute();
    }

    private void closeWindow() {
        finish();
        overridePendingTransition(R.anim.stand_still, R.anim.move_out_to_bottom);
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.changeDateTime:
                changedDate = new Date();
                changeDate();
                return true;
            case R.id.pastAttendances:
                goToPastAttendances();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Select students from DB
     */
    private class SelectStudents extends AsyncTask<Void, Void, ArrayList<Student>> {

        @Override
        protected ArrayList<Student> doInBackground(Void... params) {
            ArrayList<Student> tmpList = null;
            if (classroom != null) {
                DatabaseManager databaseManager = new DatabaseManager(context);
                tmpList = databaseManager.selectStudents(classroom.getId());
            }

            return tmpList;
        }

        @Override
        protected void onPostExecute(ArrayList<Student> tmpList) {
            arrayList.clear();

            if (tmpList != null) {
                arrayList.addAll(tmpList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class IsAlreadyExist extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isExist = false;
            if (classroom != null) {
                DatabaseManager databaseManager = new DatabaseManager(context);
                isExist = databaseManager.selectAttendanceToCheckExistance(classroom.getId(),
                        classDate);
            }

            return isExist;
        }

        @Override
        protected void onPostExecute(Boolean isExist) {
            if (isExist) {
                Snackbar.make(list, getString(R.string.couldNotInsertAttendance),
                        Snackbar.LENGTH_LONG).show();
            } else {
                new InsertAttendance().execute();
            }
        }
    }

    /**
     * Insert attendance name into DB
     */
    private class InsertAttendance extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccessful = false;

            if (arrayList != null) {
                DatabaseManager databaseManager = new DatabaseManager(context);
                isSuccessful = databaseManager.insertAttendance(arrayList, classDate);
            }

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }
            closeWindow();
        }
    }
}