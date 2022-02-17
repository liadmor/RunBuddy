package com.example.runbuddy;

import androidx.appcompat.app.AppCompatActivity;

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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

            CustomStringRequest stringRequest = new CustomStringRequest(Request.Method.POST,null, URL, new Response.Listener<CustomStringRequest.ResponseM>() {

                @Override
                public void onResponse(CustomStringRequest.ResponseM result) {
                    //From here you will get headers
                    String sessionId = result.headers.get("Set-Cookie");
                    try {
                        JSONObject responseString = new JSONObject(result.response);
                        if (!responseString.getBoolean("result")) {
                            showMessage(responseString.getString("reason"));
                        }else{
                            Intent mapIntent = new Intent(MainActivity.this, GoogleMapActivity.class);
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
    

}