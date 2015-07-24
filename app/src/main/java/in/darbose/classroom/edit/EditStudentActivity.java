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

package in.darbose.classroom.edit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import in.darbose.classroom.R;
import in.darbose.classroom.adapters.StudentAdapter;
import in.darbose.classroom.database.DatabaseManager;
import in.darbose.classroom.interfaces.OnClick;
import in.darbose.classroom.interfaces.OnPrompt;
import in.darbose.classroom.material_dialog.CustomAlertDialog;
import in.darbose.classroom.material_dialog.PromptDialog;
import in.darbose.classroom.model.Classroom;
import in.darbose.classroom.model.Student;

/**
 * Created by ferid.cafer on 4/15/2015.
 * Updated by DR AMIT K AWASTHI on 4/15/2015.
 */
public class EditStudentActivity extends AppCompatActivity {
    private Context context;

    private ListView list;
    private ArrayList<Student> arrayList;
    private StudentAdapter adapter;

    private Classroom classroom;

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
        adapter = new StudentAdapter(context, R.layout.simple_text_item_small, arrayList);
        list.setAdapter(adapter);

        //empty list view text
        TextView emptyText = (TextView) findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.emptyMessageStudent));
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle(classroom.getName());
    }

    /**
     * setOnItemClickListener
     */
    private void setListItemClickListener() {
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList != null && arrayList.size() > position) {
                    final Student student = arrayList.get(position);

                    //alert
                    CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                    customAlertDialog.setMessage(student.getName()
                            + getString(R.string.sureToDelete));
                    customAlertDialog.setPositiveButtonText(getString(R.string.delete));
                    customAlertDialog.setNegativeButtonText(getString(R.string.cancel));
                    customAlertDialog.setOnClickListener(new OnClick() {
                        @Override
                        public void OnPositive() {
                            new DeleteStudent().execute(student.getId());
                        }

                        @Override
                        public void OnNegative() {
                            //do nothing
                        }
                    });
                    customAlertDialog.showDialog();
                }
                return true;
            }
        });
    }

    /**
     * Set floating action button with its animation
     */
    private void startButtonAnimation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.SlideInUp).playOn(floatingActionButton);
                floatingActionButton.setImageResource(R.drawable.ic_action_add);
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        }, 400);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
                //importStudents();
            }
        });
    }

    private void importStudents()
    {
       // Toast.makeText(getApplicationContext(), "Inserting", Toast.LENGTH_LONG).show();
        File exportDir = new File(Environment.getExternalStorageDirectory(), "Documents");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
       // Toast.makeText(getApplicationContext(), exportDir.toString(), Toast.LENGTH_LONG).show();
        File file = new File(exportDir, "Students.csv");
      //  Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_LONG).show();
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String [] row;
            try {
                while ((row = reader.readNext()) != null) {

                    // nextLine[] is an array of values from the line

                    String rollno=row[0];
                    String sname=row[1];
                    //String address=nextLine[2];
                    //String email=nextLine[3];

                    if(sname.equalsIgnoreCase("Roll No"))
                    {
                        Toast.makeText(getApplicationContext(), "Duplicate! Ignored.", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        //Toast.makeText(getApplicationContext(), "Inserting", Toast.LENGTH_LONG).show();
                        new InsertStudent().execute(rollno, sname);
                        //if(value==1)
                        //{

                        //}
                    }

                }
                Toast.makeText(getApplicationContext(), "Data inerted into table", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * Add new student item
     */
    private void addNewItem() {
        final PromptDialog promptDialog = new PromptDialog(context);
        promptDialog.setTitle(getString(R.string.studentName));
        promptDialog.setPositiveButton(getString(R.string.ok));
        promptDialog.setOnPositiveClickListener(new OnPrompt() {
            @Override
            public void OnPrompt(String promptText) {
                promptDialog.dismiss();

                closeKeyboard();

                if (!promptText.toString().equals(""))
                    new InsertStudent().execute("",promptText);
            }
        });
        promptDialog.show();
    }

    /**
     * Closes keyboard for disabling interruption
     */
    private void closeKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.action_import_students:
                importStudents();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_students, menu);
        return true;
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

    /**
     * Insert student name into DB
     */
    private class InsertStudent extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean isSuccessful = false;
            String rollno = params[0];
            String student = params[1];
            if (classroom != null) {
                DatabaseManager databaseManager = new DatabaseManager(context);
                isSuccessful = databaseManager.insertStudent(classroom.getId(),rollno, student);
            }

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful)
                new SelectStudents().execute();
        }
    }

    /**
     * Delete a student item from DB
     */
    private class DeleteStudent extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            int studentId = params[0];
            DatabaseManager databaseManager = new DatabaseManager(context);
            boolean isSuccessful = databaseManager.deleteStudent(studentId, classroom.getId());

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful)
                new SelectStudents().execute();
        }
    }


}