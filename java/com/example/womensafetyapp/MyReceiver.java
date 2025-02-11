package com.example.womensafetyapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

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
import java.util.List;
import java.util.Map;

public class MyReceiver extends BroadcastReceiver {
    static int countPowerOff = 0;
    private Activity activity = null;
    private SmsManager sm;
    RequestQueue queue;
    User user;

    public MyReceiver(Activity activity,User user) {
        this.activity = activity;
        this.user=user;
        sm=SmsManager.getDefault();
//        queue = Volley.newRequestQueue(activity.getApplicationContext());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive", "Power button is pressed.");
//
//        Toast.makeText(context, "power button clicked", Toast.LENGTH_LONG)
//                .show();

//        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if(countPowerOff>=3){

                GPSTracker mGPS = new GPSTracker(activity);
                mGPS.getLocation();
                if(mGPS.canGetLocation){
                    sendSMS(mGPS.getLatitude() + "," + mGPS.getLongitude());
                    countPowerOff=0;
                }
            } else{
                countPowerOff++;
            }
//        }
//        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            if (countPowerOff == 3) {
//            }
//        }

    }
    public void sendSMS(String location){
        Log.d("loc","calling");
        Log.d("loc",location);
        List<Contact> contacts=user.getContacts();
        for(int i=0;i<contacts.size();i++){
            Log.d("contacts",contacts.get(i).getMobile());
            sm.sendTextMessage(contacts.get(i).getMobile(),null,"I'm in danger..My current location is http://maps.google.com/?q="+location,null,null);
        }
        Toast.makeText(activity,"Messages sent",Toast.LENGTH_LONG).show();
//        SMSService(contacts,location);
    }
    public void SMSService(final List<Contact> contacts,final String location){
        String url = new Connect().geturl();
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://"+url+"/WomenSafety/SendSms.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                                if(response.equals("1")){
                                    Toast.makeText(activity,"Messages sent",Toast.LENGTH_LONG).show();
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
                params.put("Contacts", new Gson().toJson(contacts));
                params.put("Location", location);
                return params;
            }
        };
        queue.add(postRequest);
    }
}