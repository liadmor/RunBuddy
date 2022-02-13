package com.example.runbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button btn = (Button)findViewById(R.id.button_signin);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = findViewById(R.id.et_username);
                EditText password = findViewById(R.id.et_password);
                loginRequest(username.getText().toString(), password.getText().toString());
            }
        });



    }

    private void showMessage(String reason){
        TextView login_error = findViewById(R.id.login_error);

        switch (reason){
            case "U":
                login_error.setText("User is incorrect, Try again!");
                break;
            case "UP":
                login_error.setText("Password is incorrect, Try again!");
                break;
        }
    }


    private void loginRequest(String username, String password){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://10.0.2.2:5000/auth/login";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            final String requestBody = jsonBody.toString();

            CustomStringRequest stringRequest = new CustomStringRequest(Request.Method.POST, URL, new Response.Listener<CustomStringRequest.ResponseM>() {
                @Override
                public void onResponse(CustomStringRequest.ResponseM result) {
                    //From here you will get headers
                    String sessionId = result.headers.get("Set-Cookie");
                    try {
                        JSONObject responseString = new JSONObject(result.response);
                        if (!responseString.getBoolean("result")) {
                            showMessage(responseString.getString("reason"));
                        }else{
                            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                            mapIntent.putExtra("cookie",sessionId);
                            startActivity(mapIntent);
                        }


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

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                Toast.makeText(this, "RadiusSetting selected", Toast.LENGTH_SHORT).show();
                //intent = new Intent(this, GoogleMapActivity.class);
                //startActivity(intent);
                this.finish();
                return true;

            case R.id.StartActivity:
                // User chose the "StartActivity" action, mark the current item
                // as a favorite...
                Toast.makeText(this, "StartActivity selected", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.ShowActibities:
                // User chose the "ShowActibities" action, mark the current item
                // as a favorite...
                Toast.makeText(this, "ShowActibities selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}