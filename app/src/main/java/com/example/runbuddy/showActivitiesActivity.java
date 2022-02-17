package com.example.runbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

public class showActivitiesActivity extends AppCompatActivity {
    private String cookie;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_activities);
        Toolbar myToolbar = findViewById(R.id.toolbargoogle);
        setSupportActionBar(myToolbar);
        Intent intent = getIntent();
        cookie = intent.getStringExtra("cookie");
        getActivities();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.RadiusSetting:
                // User chose the "RadiusSetting" item, show the app settings UI...
                intent = new Intent(showActivitiesActivity.this, GoogleMapActivity.class);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
                return true;
            case R.id.StartActivity:
                // User chose the "StartActivity" action, mark the current item
                intent = new Intent(showActivitiesActivity.this, addRunActivity.class);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
                return true;
            case R.id.ShowActivities:
                // User chose the "ShowActivities" action, mark the current item
                // as a favorite...
                return true;
            case R.id.logout:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    private void getActivities(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://10.0.2.2:5000/loc/get_activities";
        RecyclerView recyclerView1 = findViewById(R.id.showAll);

        CustomStringRequest stringRequest = new CustomStringRequest(Request.Method.GET, cookie, URL, new Response.Listener<CustomStringRequest.ResponseM>() {
            @Override
            public void onResponse(CustomStringRequest.ResponseM result) {
                //From here you will get headers

                JSONArray activities = null;
                try {
                    activities = new JSONArray(result.response);
                    MyAdaptar adapter = new MyAdaptar(showActivitiesActivity.this, activities);
                    recyclerView1.setAdapter(adapter);
                    recyclerView1.setLayoutManager(new LinearLayoutManager(showActivitiesActivity.this));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("VOLLEY", result.response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };
        requestQueue.add(stringRequest);
    }
}