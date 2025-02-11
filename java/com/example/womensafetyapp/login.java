package com.example.womensafetyapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class login extends Fragment {
    public String Email,Password;
    private EditText email,password;
    private Button submit;
    RequestQueue queue;
    JSONArray jsonArray;
    public ViewPager viewPager;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public login() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        queue = Volley.newRequestQueue(getContext());
        email=(EditText)rootView.findViewById(R.id.et_email);
        password=(EditText)rootView.findViewById(R.id.et_password);
        submit=(Button) rootView.findViewById(R.id.btn_login);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = email.getText().toString();
                Password = password.getText().toString();
                if ((Email.equals("")) || (Password.equals(""))) {
                    ((MainActivity) getActivity()).AlertMedthod("Please fill all fields",getContext());
                }else if(!isValidEmail(email.getText().toString())){
                    ((MainActivity) getActivity()).AlertMedthod("Invalid Email Address",getContext());
                    email.setError("Invalid Email Address");
                }else {
                    loginService();
                }
            }
        });
        return rootView;
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public void loginService() {
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/login.php",
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
                                    Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_LONG).show();
                                    email.setText("");
                                    password.setText("");
                                    ((MainActivity) getActivity()).MoveToHome(obj.getString("data"));
                                } else if(obj.getString("Status").equals("-1")){
                                    Toast.makeText(getActivity(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                    email.setText("");
                                    password.setText("");
                                } else{
                                    Toast.makeText(getActivity(), "Something went wrong try later", Toast.LENGTH_SHORT).show();
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
                params.put("email", email.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        queue.add(postRequest);
    }
}
