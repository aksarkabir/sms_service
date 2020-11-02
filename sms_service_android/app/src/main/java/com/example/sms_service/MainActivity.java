package com.example.sms_service;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class MainActivity extends AppCompatActivity {
    public static final String msg = "this is a message for next page";
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String passid = "passid";
    public static final String status = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void checklogin(View view){
        ProgressBar pb = (ProgressBar) findViewById(R.id.loading);
        pb.setVisibility(View.VISIBLE);
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/api-data/";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                ProgressBar pb = (ProgressBar) findViewById(R.id.loading);
                pb.setVisibility(View.INVISIBLE);

                try{
                    Object obj = new JSONParser().parse(response.toString());
                    JSONObject jo = (JSONObject) obj;
                    String reply = (String) jo.get("reply");
                    System.out.println(reply);
                    TextView rp = (TextView) findViewById(R.id.rply);
//                    rp.setText(reply);
                    if (reply.equals("no")){
                        EditText pass = (EditText) findViewById(R.id.password);
                        pass.setText("");
                        rp.setText("invalid user or wrong password");
                    }else{
                        Intent intent = new Intent(MainActivity.this,showservice.class);
                        EditText p = (EditText) findViewById(R.id.id);
                        EditText pa = (EditText) findViewById(R.id.password);
                        SharedPreferences sharedPreferences = getSharedPreferences(share,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(loginid,p.getText().toString());
                        editor.putString(passid,pa.getText().toString());
                        editor.putString(status,"1");
                        editor.apply();
                        intent.putExtra(msg,p.getText().toString());
                        startActivity(intent);
                    }
                }catch (Exception e){
                    System.out.println(e.toString());
                    TextView rp = (TextView) findViewById(R.id.rply);
                }

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                System.out.println(error.toString());
                ProgressBar pb = (ProgressBar) findViewById(R.id.loading);
                pb.setVisibility(View.INVISIBLE);
                TextView rp = (TextView) findViewById(R.id.rply);
                rp.setText("network error");
            }
        }) {
            protected Map<String, String> getParams() {
                EditText id = (EditText) findViewById(R.id.id);
                EditText pass = (EditText) findViewById(R.id.password);
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("id", id.getText().toString()); //Add the data you'd like to send to the server.
                MyData.put("pass",pass.getText().toString());
                String device_name = Build.BRAND+" "+Build.MODEL+" "+Build.ID;
                MyData.put("device_name",device_name);
                MyData.put("brand",Build.BRAND);
                MyData.put("model",Build.MODEL);
                MyData.put("id_phone", Build.ID);
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }



    public void clearText(View view){

    }
}
