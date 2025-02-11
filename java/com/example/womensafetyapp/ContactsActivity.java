package com.example.womensafetyapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerviewAdapter recyclerviewAdapter;
    private RecyclerTouchListener touchListener;
    TextView txt;
    User user;
    List<Contact> taskList = new ArrayList<>();
    RequestQueue queue;
    JSONArray jsonArray;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    FragmentManager fragmentManager;
    CustomDialogFragment newFragment;
    boolean isLargeLayout;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        queue = Volley.newRequestQueue(getApplicationContext());
        fragmentManager = getSupportFragmentManager();
        isLargeLayout = getResources().getBoolean(R.bool.large_layout);
        sharedpreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        Gson gson = new Gson();
        String json = sharedpreferences.getString("User", "");
        user=gson.fromJson(json, User.class);
        recyclerView = findViewById(R.id.recyclerview);
        txt=findViewById(R.id.task_nodata);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerviewAdapter = new RecyclerviewAdapter(this);
        getContacts();
        touchListener = new RecyclerTouchListener(this,recyclerView);
        touchListener
                .setClickable(new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(int position) {
                        Toast.makeText(getApplicationContext(),"Swipe to left for more options", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, int position) {

                    }
                })
                .setSwipeOptionViews(R.id.delete_task,R.id.edit_task)
                .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                    @Override
                    public void onSwipeOptionClicked(int viewID, int position) {
                        switch (viewID){
                            case R.id.delete_task:
                                ContactServiceDelete(taskList.get(position).getId());
                                //Call after delete
//                                taskList.remove(position);
//                                recyclerviewAdapter.setTaskList(taskList);
                                break;
                            case R.id.edit_task:
                                newFragment = new CustomDialogFragment("Update",taskList.get(position),user);
                                newFragment.show(fragmentManager, "dialog");
                                break;

                        }
                    }
                });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFragment = new CustomDialogFragment("Add",new Contact(),user);
//                if (isLargeLayout) {
                    // The device is using a large layout, so show the fragment as a dialog
                    newFragment.show(fragmentManager, "dialog");
//                } else {
//                    // The device is smaller, so show the fragment fullscreen
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    // For a little polish, specify a transition animation
//                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                    // To make it fullscreen, use the 'content' root view as the container
//                    // for the fragment, which is always the root view for the activity
//                    transaction.add(android.R.id.content, newFragment)
//                            .addToBackStack(null).commit();
//                }
            }
        });
    }
    public void closeFragment(){
        newFragment.dismiss();
    }
    public void getContacts(){
        taskList.clear();
        taskList.addAll(user.getContacts());
        if(taskList.isEmpty()){
            txt.setVisibility(View.VISIBLE);
        } else{
            txt.setVisibility(View.INVISIBLE);
        }
        recyclerviewAdapter.setTaskList(taskList);
        recyclerView.setAdapter(recyclerviewAdapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        recyclerView.addOnItemTouchListener(touchListener);
    }
    public void ContactService(final String Name,final String Number,final String Task,final String position){
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/contacts.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        closeFragment();
                        // response
                        Log.d("Response", response);
                        try {
                            jsonArray = new JSONArray(response);
                            //looping through all the elements in json array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //getting json object from the json array
                                JSONObject obj = jsonArray.getJSONObject(i);
                                if(obj.getString("Status").equals("1")){
                                    Toast.makeText(getApplicationContext(), obj.getString("Response"), Toast.LENGTH_LONG).show();
                                    UpdateContacts();
                                } else if(obj.getString("Status").equals("-1")){
                                    Toast.makeText(getApplicationContext(), obj.getString("Response"), Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(getApplicationContext(), "Something went wrong try later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                            Log.e("exceptions","exceptions"+ex);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("Name", Name);
                params.put("Number", Number);
                params.put("UserId", user.getId());
                if(Task.equals("Update")){
                    params.put("Id", position);
                }
                params.put("Task", Task);
                return params;
            }
        };
        queue.add(postRequest);
    }
    private void ContactServiceDelete(final String id) {
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/delete.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            jsonArray = new JSONArray(response);
                            //looping through all the elements in json array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //getting json object from the json array
                                JSONObject obj = jsonArray.getJSONObject(i);
                                if(obj.getString("Status").equals("1")){
                                    Toast.makeText(getApplicationContext(), "Deleted Successful", Toast.LENGTH_LONG).show();
                                    UpdateContacts();
                                } else if(obj.getString("Status").equals("-1")){
                                    Toast.makeText(getApplicationContext(), "Delete Failed", Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(getApplicationContext(), "Something went wrong try later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                            Log.e("exceptions","exceptions"+ex);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("Id", id);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void UpdateContacts() {
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/getContacts.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            jsonArray = new JSONArray(response);
                            //looping through all the elements in json array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //getting json object from the json array
                                JSONObject obj = jsonArray.getJSONObject(i);
                                if(obj.getString("Status").equals("1")){
                                    Gson gson = new Gson();
                                    user=gson.fromJson(obj.getString("data"),  User.class);
                                    user.setLoggedIn(true);
                                    String json = gson.toJson(user);
                                    editor.clear();
                                    editor.putString("User", json);
                                    editor.commit();
                                    getContacts();
                                }
                            }
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                            Log.e("exceptions","exceptions"+ex);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("Id", user.getId());
                return params;
            }
        };
        queue.add(postRequest);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
