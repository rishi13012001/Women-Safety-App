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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class register extends Fragment {
    private EditText name,mobile,email,password,password2;
    private Button submit;
    RequestQueue queue;
    JSONArray jsonArray;
    public ViewPager viewPager;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);
        queue = Volley.newRequestQueue(getContext());
        viewPager = rootView.findViewById(R.id.viewPager);
        name=(EditText)rootView.findViewById(R.id.ret_name);
        email=(EditText)rootView.findViewById(R.id.ret_email);
        mobile=(EditText)rootView.findViewById(R.id.ret_mobile);
        password=(EditText)rootView.findViewById(R.id.ret_password);
        password2=(EditText)rootView.findViewById(R.id.ret_repassword);
        submit=(Button) rootView.findViewById(R.id.btn_register);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().equals("") || name.getText().toString().equals("")
                ||mobile.getText().toString().equals("")|| password.getText().toString().equals("")||password2.getText().toString().equals(""))
                {
                    ((MainActivity) getActivity()).AlertMedthod("Please fill all fields",getContext());
                }else {
                    if(!password.getText().toString().equals(password2.getText().toString())){
                        ((MainActivity) getActivity()).AlertMedthod("Password doesn't match",getContext());
                    }  else if(!isValidEmail(email.getText().toString())){
                        ((MainActivity) getActivity()).AlertMedthod("Invalid Email Address",getContext());
                        email.setError("Invalid Email Address");
                    }else{
                        registerService();
                    }
                }
            }
        });
        return rootView;
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public void registerService() {
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/register.php",
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
                                    Toast.makeText(getActivity(), "Registered Successfully,Please login", Toast.LENGTH_LONG).show();
                                    name.setText("");
                                    email.setText("");
                                    mobile.setText("");
                                    password.setText("");
                                    password2.setText("");
                                    ((MainActivity) getActivity()).FragmentMethod();
                                } else if(obj.getString("Status").equals("-1")){
                                    Toast.makeText(getActivity(), "Email Already exists", Toast.LENGTH_SHORT).show();
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
                params.put("name", name.getText().toString());
                params.put("mobile", mobile.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        queue.add(postRequest);
    }
}
