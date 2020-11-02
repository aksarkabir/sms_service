package com.example.sms_service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class sms_send extends IntentService {

    public String msg = "start";
    public static final String sp = "my_shared_pref";
    public static final String message = "message";
    public static final String number = "number";
    public static final String sms_id = "msg_id";
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String status = "status";
    public sms_send(){
        super("sms_send");
    }
    int a =1;

    @Override
    public void onCreate() {
        System.out.println("created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        final SharedPreferences sharedPreferences = this.getSharedPreferences(share, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();


        while (true){
            try{
//                Toast.makeText(this, "running", Toast.LENGTH_SHORT).show();
                if(msg.equals("end")){
                    break;
                }
                if(a==-1){
                    break;
                }
                String sts = sharedPreferences.getString(status,"1");
                System.out.println(sts);
                if(sts.equals("1")){
                    editor.putString(status,"0");
                    editor.apply();
                    String url = "http://192.168.43.224/sms/get-unsend-msg/";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //This code is executed if the server responds, whether or not the response contains data.
                            //The String 'response' contains the server's response.
                            if(response.toString().equals("{}")){
                               System.out.println("empty");
                               editor.putString(status,"1");
                               editor.apply();
                               a++;
                            }else{
                                a=0;
                                try{
                                    Object obj = new JSONParser().parse(response.toString());
                                    JSONObject jo = (JSONObject)obj;
                                    editor.putString(message,jo.get("message").toString());
                                    editor.putString(number,jo.get("number").toString());
                                    editor.putString(sms_id,jo.get("id").toString());
                                    editor.putString(status,"2");
                                    editor.apply();
                                }catch (Exception e){
                                    System.out.println(e.toString());
                                }
                            }
                        }
                    }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //This code is executed if there is an error.
                            editor.putString(status,"1");
                            editor.apply();
                            System.out.println(error.toString());
                        }
                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<String, String>();
                            String username = sharedPreferences.getString(loginid,"");
                            MyData.put("username", username); //Add the data you'd like to send to the server.
                            return MyData;
                        }
                    };
                    MyRequestQueue.add(MyStringRequest);
                }else if (sts.equals("2")){
                    try{
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(sharedPreferences.getString(number,""),null,sharedPreferences.getString(message,""),null,null);
                        editor.putString(status,"3");
                        editor.apply();
                    }catch (Exception e){
                        editor.putString(status,"1");
                        editor.apply();
                        System.out.println(e.toString());
                    }
                }else if(sts.equals("3")){
                    System.out.println("message send successfully");
                    String url = "http://192.168.43.224/sms/sms-status-save/";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //This code is executed if the server responds, whether or not the response contains data.
                            //The String 'response' contains the server's response.
                            try{
                                Object obj = new JSONParser().parse(response.toString());
                                JSONObject jo = (JSONObject) obj;
                                String reply = (String) jo.get("reply");
                                editor.putString(status,"1");
                                editor.apply();
                            }catch (Exception e){
                                System.out.println(e.toString());
                            }
                        }
                    }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //This code is executed if there is an error.
                        }
                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<String, String>();
                            MyData.put("sms_id", sharedPreferences.getString(sms_id,"").toString()); //Add the data you'd like to send to the server.
                            MyData.put("username",sharedPreferences.getString(loginid,"").toString());
                            return MyData;
                        }
                    };
                    MyRequestQueue.add(MyStringRequest);
                }
                Thread.sleep(3000);
            }catch (Exception e){

            }
        }

    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        msg = "end";
        System.out.println("clossing the service");
    }
}
