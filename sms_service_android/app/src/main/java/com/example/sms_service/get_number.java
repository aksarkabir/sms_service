package com.example.sms_service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.sms_service.R.layout.activity_get_number;

public class get_number extends AppCompatActivity {
    public static final String share = "share";
    public static final String loginid = "loginid";
    public static final String passid = "passid";
    public static final String phonebookname = "get_phonebook";
    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_get_number);
//        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,mobileArray);
//
//        ListView listView = (ListView) findViewById(R.id.number_list);
//        listView.setAdapter(adapter);
        checklogin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.get_number_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_phonebook){
            Intent intent = new Intent(this,add_new_phonebook.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void checklogin(){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.43.224/sms/get_phonebook/";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                try{
                    Object object = new JSONParser().parse(response.toString());
                    JSONArray array =  (JSONArray)object;
                    final ArrayList<String>narray = new ArrayList<String>();
                    for (int z=0;z<array.size();z++){
                        JSONObject jo = (JSONObject)array.get(z);
                        String tmpnameofphonebook = (String) jo.get("phonebook_name");
                        narray.add(tmpnameofphonebook);
                        System.out.println(jo.get("phonebook_name"));
                    }
                    ArrayAdapter adapter = new ArrayAdapter(get_number.this,android.R.layout.simple_list_item_1,narray);
                    ListView lv = (ListView) findViewById(R.id.number_list);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            System.out.println(narray.get(i));
                            Intent intent = new Intent(get_number.this,save_number.class);
                            intent.putExtra(phonebookname,narray.get(i));
                            startActivity(intent);
                        }
                    });
                    ProgressBar pb = (ProgressBar) findViewById(R.id.loadingforphonebook);
                    pb.setVisibility(View.INVISIBLE);

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
                SharedPreferences sharedPreferences = getSharedPreferences(share,MODE_PRIVATE);
                String id = sharedPreferences.getString(loginid,"");
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("username", id); //Add the data you'd like to send to the server.
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }
}
