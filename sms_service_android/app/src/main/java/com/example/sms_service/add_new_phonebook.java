package com.example.sms_service;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class add_new_phonebook extends AppCompatActivity {
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String passid = "passid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_phonebook);
    }

    public void savephonebook(View view){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/save_phonebook_from_phone/";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                try {
                    Object obj = new JSONParser().parse(response.toString());
                    JSONObject jo = (JSONObject) obj;
                    String reply = (String) jo.get("reply");
                    TextView tv = (TextView) findViewById(R.id.errorphonebook);
                    tv.setText(reply);
                    if(reply.equals("success")){
                        Intent intent = new Intent(add_new_phonebook.this,get_number.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
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
                EditText te = (EditText) findViewById(R.id.phonebookeditText);
                MyData.put("phonebook_name", te.getText().toString()); //Add the data you'd like to send to the server.
                MyData.put("username",id);
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    public void get_device_name(View view){
        EditText te = (EditText) findViewById(R.id.phonebookeditText);
        te.setText(Build.MODEL);
    }
}
