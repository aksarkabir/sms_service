package com.example.sms_service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class get_sms extends IntentService {
    String ending = "not";
    int chk = 0;
    int check_time = 0;
    int cn=1;
    public String msg = "start";
    public static final String sp = "my_shared_pref";
    public static final String message = "message";
    public static final String number = "number";
    public static final String sms_id = "msg_id";
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String status = "status";

    public get_sms(){
        super("get_sms");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("service is created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("service is ending");
        ending = "end";
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        long currenttime = Calendar.getInstance().getTimeInMillis();
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        while (true){
            if(ending.equals("end")){
                break;
            }
            if(check_time == 0){
                final Uri SMS_INBOX = Uri.parse("content://sms/inbox");

                //Retrieves all SMS (if you want only unread SMS, put "read = 0" for the 3rd parameter)
                Cursor cursor = getContentResolver().query(SMS_INBOX, null, "read=0", null, null);
                final JSONArray newsms = new JSONArray();
                //Get all lines
                while (cursor.moveToNext()) {
                    JSONObject jo = new JSONObject();
                    //Gets the SMS information
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    String person = cursor.getString(cursor.getColumnIndex("person"));
                    String date = cursor.getString(cursor.getColumnIndex("date"));
                    long tm =cursor.getLong(cursor.getColumnIndex("date"));
                    String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
                    String read = cursor.getString(cursor.getColumnIndex("read"));
                    String status = cursor.getString(cursor.getColumnIndex("status"));
                    String type = cursor.getString(cursor.getColumnIndex("type"));
                    String subject = cursor.getString(cursor.getColumnIndex("subject"));
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    if(tm>=currenttime){
                        currenttime = tm+1;
                        System.out.println(body);
                        chk = 1;
                        address = address.replace("+88","");
                        jo.put("address", address);
                        jo.put("msg", body);
                        newsms.add(jo);
                    }
                    //Do what you want
                }
                try {
                    Thread.sleep(5000);
                    System.out.println("survice is running");
                }catch (Exception e){
                    System.out.println(e.toString());
                }

                if(chk == 1){
                    check_time =1;
                    final SharedPreferences sharedPreferences = this.getSharedPreferences(share, MODE_PRIVATE);
                    chk = 0;
                    String url = "http://192.168.43.224/sms/get-sms-from-phone/";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //This code is executed if the server responds, whether or not the response contains data.
                            //The String 'response' contains the server's response.
                        }
                    }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //This code is executed if there is an error.
                        }
                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<String, String>();
                            MyData.put("message", newsms.toJSONString()); //Add the data you'd like to send to the server.
                            System.out.println(newsms.toJSONString());
                            String username = sharedPreferences.getString(loginid,"");
                            MyData.put("username",username);
                            return MyData;
                        }
                    };
                    MyRequestQueue.add(MyStringRequest);
                }
                cursor.close();
            }else{
                cn++;
                if(cn==11){
                    check_time = 0;
                    cn =1;
                }
                try {
                    Thread.sleep(1000);
                    System.out.println("survice is running");
                }catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        }
    }
}
