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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import in.darbose.classroom.R;
import in.darbose.classroom.adapters.ClassroomAdapter;
import in.darbose.classroom.database.DatabaseManager;
import in.darbose.classroom.interfaces.OnClick;
import in.darbose.classroom.interfaces.OnPrompt;
import in.darbose.classroom.material_dialog.CustomAlertDialog;
import in.darbose.classroom.material_dialog.PromptDialog;
import in.darbose.classroom.model.Classroom;

/**
 * Created by ferid.cafer on 4/15/2015.
 * Updated by DR AMIT K AWASTHI on 4/15/2015.
 */
public class EditClassroomFragment extends Fragment {
    private static final String SAMPLE_DB_NAME ="rollcall.db" ;
    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView list;
    private ArrayList<Classroom> arrayList;
    private ClassroomAdapter adapter;


    public EditClassroomFragment() {}

    public static EditClassroomFragment newInstance() {
        EditClassroomFragment editClassroomFragment = new EditClassroomFragment();
        return editClassroomFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.simple_listview, container, false);

        context = rootView.getContext();

        list = (ListView) rootView.findViewById(R.id.list);
        arrayList = new ArrayList<Classroom>();
        adapter = new ClassroomAdapter(context, R.layout.simple_text_item_big, arrayList);
        list.setAdapter(adapter);

        //empty list view text
        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        list.setEmptyView(emptyText);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new SelectClassrooms().execute();
            }
        });

        setListItemClickListener();

        new SelectClassrooms().execute();


        setHasOptionsMenu(true);

        return rootView;
    }

    /**
     * setOnItemClickListener & setOnItemLongClickListener
     */
    private void setListItemClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList != null && arrayList.size() > position) {
                    Intent intent = new Intent(context, EditStudentActivity.class);
                    intent.putExtra("classroom", arrayList.get(position));
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.move_in_from_bottom,
                            R.anim.stand_still);
                }
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList != null && arrayList.size() > position) {
                    final Classroom classroom = arrayList.get(position);

                    //alert
                    CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
                    customAlertDialog.setMessage(classroom.getName()
                            + getString(R.string.sureToDelete));
                    customAlertDialog.setPositiveButtonText(getString(R.string.delete));
                    customAlertDialog.setNegativeButtonText(getString(R.string.cancel));
                    customAlertDialog.setOnClickListener(new OnClick() {
                        @Override
                        public void OnPositive() {
                            new DeleteClassroom().execute(classroom.getId());
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
     * Add new class item
     */
    private void addNewItem() {
        final PromptDialog promptDialog = new PromptDialog(context);
        promptDialog.setTitle(getString(R.string.classroomName));
        promptDialog.setPositiveButton(getString(R.string.ok));
        promptDialog.setAllCaps();
        promptDialog.setOnPositiveClickListener(new OnPrompt() {
            @Override
            public void OnPrompt(String promptText) {
                promptDialog.dismiss();

                closeKeyboard();

                if (!promptText.toString().equals(""))
                    new InsertClassroom().execute(promptText);
            }
        });
        promptDialog.show();
    }

    /**
     * Closes keyboard for disabling interruption
     */
    private void closeKeyboard(){
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);
    }

    private void exportDB() {


        FileInputStream fis = null;
        try {
            File file = context.getDatabasePath("ClassroomManager");
            fis = new FileInputStream(file);
            String outputDB = Environment.getExternalStorageDirectory() + "/Download/rollcall";
            OutputStream os = new FileOutputStream(outputDB);

// Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
// Close the streams
            os.flush();
            os.close();
            fis.close();
            Toast.makeText(this.context, outputDB.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void importDB() {


        FileInputStream fis = null;
        try {
            File file = context.getDatabasePath("ClassroomManager");
            String outputDB = Environment.getExternalStorageDirectory() + "/Download/rollcall";
            fis = new FileInputStream(outputDB);
            OutputStream os = new FileOutputStream(file);

// Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
// Close the streams
            os.flush();
            os.close();
            fis.close();
            Toast.makeText(this.context, file.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.add:
                addNewItem();
                return true;
            case R.id.action_backup:
                exportDB();
                return true;
            case R.id.action_restore:
                importDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Select classrooms from DB
     */
    private class SelectClassrooms extends AsyncTask<Void, Void, ArrayList<Classroom>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<Classroom> doInBackground(Void... params) {
            DatabaseManager databaseManager = new DatabaseManager(context);
            ArrayList<Classroom> tmpList = databaseManager.selectClassrooms();

            return tmpList;
        }

        @Override
        protected void onPostExecute(ArrayList<Classroom> tmpList) {
            swipeRefreshLayout.setRefreshing(false);

            arrayList.clear();

            if (tmpList != null) {
                arrayList.addAll(tmpList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Insert classroom name into DB
     */
    private class InsertClassroom extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String classroom = params[0];
            DatabaseManager databaseManager = new DatabaseManager(context);
            boolean isSuccessful = databaseManager.insertClassroom(classroom);

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful)
                new SelectClassrooms().execute();
        }
    }


 //       File sd = Environment.getExternalStorageDirectory();
 //       File data = Environment.getDataDirectory();
 //       FileChannel source=null;
 //       FileChannel destination=null;
 //       String currentDBPath = "/data/"+ "com.your.package" +"/databases/ClassroomManager";
//        String backupDBPath = SAMPLE_DB_NAME;
//        File currentDB = new File(data, currentDBPath);
//        File backupDB = new File(sd, backupDBPath);
//        try {
//            source = new FileInputStream(currentDB).getChannel();
//            destination = new FileOutputStream(backupDB).getChannel();
//            destination.transferFrom(source, 0, source.size());
/*            source.close();
            destination.close();
            Toast.makeText(this.context, backupDB.toString(), Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
            Toast.makeText(this.context, backupDB.toString(), Toast.LENGTH_LONG).show();
        }

*/



      //  try {
    //        File sd = Environment.getExternalStorageDirectory();
  //          File data = Environment.getDataDirectory();
//
          //  if (sd.canWrite()) {
        //        String currentDBPath = "//data//"+ packageName +"//databases//"+dbList[0];
      //          String backupDBPath = dbList[0];
    //            File currentDB = new File(data, currentDBPath);
  //              File backupDB = new File(sd, backupDBPath);
//
         //       FileChannel src = new FileInputStream(currentDB).getChannel();
        //        FileChannel dst = new FileOutputStream(backupDB).getChannel();
        //        dst.transferFrom(src, 0, src.size());
        //        src.close();
        //        dst.close();
        //        Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
        //    }
        //} catch (Exception e) {
        //    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        //}

    /**
     * Delete a classroom item from DB
     */
    private class DeleteClassroom extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            int classroomId = params[0];
            DatabaseManager databaseManager = new DatabaseManager(context);
            boolean isSuccessful = databaseManager.deleteClassroom(classroomId);

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (isSuccessful)
                new SelectClassrooms().execute();
        }
    }
}