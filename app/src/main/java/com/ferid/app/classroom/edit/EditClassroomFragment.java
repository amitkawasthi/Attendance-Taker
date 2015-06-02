package com.ferid.app.classroom.edit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.RelativeLayout;

import com.ferid.app.classroom.R;
import com.ferid.app.classroom.adapters.ClassroomAdapter;
import com.ferid.app.classroom.database.DatabaseManager;
import com.ferid.app.classroom.interfaces.OnClick;
import com.ferid.app.classroom.interfaces.OnPrompt;
import com.ferid.app.classroom.material_dialog.MaterialDialog;
import com.ferid.app.classroom.material_dialog.PromptDialog;
import com.ferid.app.classroom.model.Classroom;

import java.util.ArrayList;

/**
 * Created by ferid.cafer on 4/15/2015.
 */
public class EditClassroomFragment extends Fragment {
    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView list;
    private ArrayList<Classroom> arrayList;
    private ClassroomAdapter adapter;

    private RelativeLayout emptyLayout;

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

        emptyLayout = (RelativeLayout) rootView.findViewById(R.id.emptyLayout);

        list = (ListView) rootView.findViewById(R.id.list);
        arrayList = new ArrayList<Classroom>();
        adapter = new ClassroomAdapter(context, R.layout.simple_text_item_big, arrayList);
        list.setAdapter(adapter);

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

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList != null && arrayList.size() > position) {
                    final Classroom classroom = arrayList.get(position);

                    final MaterialDialog materialDialog = new MaterialDialog(context);
                    materialDialog.setContent(classroom.getName() + getString(R.string.sureToDelete));
                    materialDialog.setPositiveButton(getString(R.string.ok));
                    materialDialog.setNegativeButton(getString(R.string.cancel));
                    materialDialog.setOnClickListener(new OnClick() {
                        @Override
                        public void OnPositive() {
                            materialDialog.dismiss();

                            new DeleteClassroom().execute(classroom.getId());
                        }

                        @Override
                        public void OnNegative() {
                            materialDialog.dismiss();
                        }
                    });
                    materialDialog.show();
                }
                return true;
            }
        });

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

        new SelectClassrooms().execute();


        setHasOptionsMenu(true);

        return rootView;
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

            //if empty, show message
            if (arrayList.size() == 0) {
                emptyLayout.setVisibility(View.VISIBLE);
            } else {
                emptyLayout.setVisibility(View.GONE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.add:
                addNewItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}