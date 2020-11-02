package com.example.sms_service;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class save_number extends AppCompatActivity {
    ListView list;
    ArrayList <ArrayList>gt;
    ArrayList mobileArray;
//    JSONObject num_details = new JSONObject();
    JSONArray number = new JSONArray();
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String passid = "passid";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_number);
        Intent intent = getIntent();
        String msg = intent.getStringExtra(get_number.phonebookname);
        TextView tv = (TextView) findViewById(R.id.phonebooknametextview);
        tv.setText(msg);
        gt = getAllContacts();
        list = findViewById(R.id.contact_list);
        Collections.sort(gt.get(0));
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text2,gt.get(0));
        list.setAdapter(adapter);
        JSONObject ja = new JSONObject();
        JSONArray jo = new JSONArray();

        Map m = new LinkedHashMap(2);
        ja.put("type", "home");
        ja.put("number", "212 555-1234");
        jo.add(ja);
        // adding map to list
        ja.put("type", "sasda");
        ja.put("number", "212 555-1234asdasdad");
        // adding map to list
        jo.add(ja);

//        System.out.println(jo.toJSONString());
//        System.out.println(number.toJSONString());


    }

    private ArrayList getAllContacts() {
        ArrayList<ArrayList>getnamenum = new ArrayList<ArrayList>();
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> numList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                JSONObject jo = new JSONObject();
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                nameList.add(name);
                jo.put("name", name);
                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        numList.add(phoneNo);
                        jo.put("number",phoneNo);
                        break;
                    }
                    pCur.close();
                }
                number.add(jo);
            }
        }
        if (cur != null) {
            cur.close();
        }
        getnamenum.add(nameList);
        getnamenum.add(numList);
        return getnamenum;
    }

    public void savenumbertoserver(View view){
        SharedPreferences sharedPreferences = getSharedPreferences(share,MODE_PRIVATE);
        final String id = sharedPreferences.getString(loginid,"");
        final TextView tv = (TextView) findViewById(R.id.phonebooknametextview);
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/save_number_from_phone/";

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                Toast.makeText(save_number.this, "All numbers saved to server", Toast.LENGTH_SHORT).show();
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
                MyData.put("phonebook",tv.getText().toString());
                MyData.put("number_details",number.toJSONString());
                MyData.put("username",id);
                System.out.println(number.toJSONString());
                return MyData;
            }
        };
        int MY_SOCKET_TIMEOUT_MS=40000;
        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyRequestQueue.add(MyStringRequest);
        Intent intent = new Intent(this,showservice.class);
        startActivity(intent);
    }

}
