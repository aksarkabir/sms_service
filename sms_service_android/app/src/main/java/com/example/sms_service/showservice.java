package com.example.sms_service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class showservice extends AppCompatActivity {
    public static final String sp = "my_shared_pref";
    public static final String message = "message";
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String passid = "passid";
    public static final String service_status = "service_status";

    int tmr = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showservice2);
        loadunsendmessage();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 20 seconds
                System.out.println("hello");
                loadunsendmessage();
                if(tmr==0){
                    handler.postDelayed(this, 10000);
                }
            }
        }, 10000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.item_1){
            System.out.println("hi i am shihab");
        }else if(item.getItemId()==R.id.item_2){
            Intent intent = new Intent(this,get_number.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void savedata(View view){
        System.out.println("closing all the services");
        stopService(new Intent(showservice.this,sms_send.class));
        stopService(new Intent(this,get_sms.class));

    }

    public void runback(View view){
        SharedPreferences sharedPreferences = getSharedPreferences(sp,MODE_PRIVATE);
        SharedPreferences.Editor spe = sharedPreferences.edit();
        spe.putString(message,"i am shihab");
        System.out.println("run");
        startService(new Intent(showservice.this,sms_send.class));
        startService(new Intent(this,get_sms.class));
    }

    public void loadunsendmessage(){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/get-unsend-msg-count/";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                try {
                    Object obj = new JSONParser().parse(response.toString());
                    JSONObject jo = (JSONObject)obj;
                    String reply = (String) jo.get("reply");
                    TextView tv = (TextView) findViewById(R.id.unsendmsg);
                    tv.setText(reply);
                    if(!reply.equals("0")){
//                        startService(new Intent(showservice.this,sms_send.class));
                    }
                }catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                System.out.println(error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                SharedPreferences sharedPreferences = getSharedPreferences(share,MODE_PRIVATE);
                String id = sharedPreferences.getString(loginid,"");
                MyData.put("username", id); //Add the data you'd like to send to the server.
                MyData.put("active","yes");
                String device_name = Build.BRAND+" "+Build.MODEL+" "+Build.ID;
                MyData.put("name", device_name); //Add the data you'd like to send to the server.
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tmr = 1;
        System.out.println("bye bye");
    }

    public void make_active_phone(final String sts){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/make-active/";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                System.out.println(response.toString());
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                System.out.println(error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                String device_name = Build.BRAND+" "+Build.MODEL+" "+Build.ID;
                MyData.put("name", device_name); //Add the data you'd like to send to the server.
                SharedPreferences sharedPreferences = getSharedPreferences(share,MODE_PRIVATE);
                String id = sharedPreferences.getString(loginid,"");
                MyData.put("username",id);
                System.out.println(id);
                MyData.put("active",sts);
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

}
