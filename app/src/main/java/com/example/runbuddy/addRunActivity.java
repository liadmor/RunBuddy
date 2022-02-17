package com.example.runbuddy;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class addRunActivity extends AppCompatActivity {
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private EditText datePickerText;
    private EditText timePickerText;
    private EditText distance;
    private Button addButton;
    private String cookie;
    private Calendar combinedCal = new GregorianCalendar(TimeZone.getTimeZone("GMT+2"));
    private boolean addActSuccess = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_run);
        Toolbar myToolbar = findViewById(R.id.toolbargoogle);
        setSupportActionBar(myToolbar);
        Intent intent = getIntent();
        cookie = intent.getStringExtra("cookie");

        datePickerText = findViewById(R.id.datePicker);
        timePickerText = findViewById(R.id.timePicker);
        distance = findViewById(R.id.distance);
        addButton = findViewById(R.id.addButton);

        datePickerText.setInputType(InputType.TYPE_NULL);
        timePickerText.setInputType(InputType.TYPE_NULL);

        datePickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datePicker = new DatePickerDialog(addRunActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                combinedCal.set(year, monthOfYear, dayOfMonth);
                                datePickerText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePicker.show();
            }
        });
        timePickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);

                // time picker dialog
                timePicker = new TimePickerDialog(addRunActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                combinedCal.set(Calendar.HOUR_OF_DAY, sHour);
                                combinedCal.set(Calendar.MINUTE, sMinute);
                                timePickerText.setText(sHour + ":" + sMinute);
                            }
                        }, hour, minutes, true);
                timePicker.show();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addActivity(combinedCal.getTimeInMillis() / 1000, Double.parseDouble(distance.getText().toString()));
                setFailure();
            }
        });

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
                intent = new Intent(addRunActivity.this, GoogleMapActivity.class);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
            case R.id.StartActivity:
                // User chose the "StartActivity" action, mark the current item
                // as a favorite...
                return true;
            case R.id.ShowActivities:
                // User chose the "ShowActivities" action, mark the current item
                // as a favorite...
                intent = new Intent(addRunActivity.this, showActivitiesActivity.class);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
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

    private void setSuccess(){
//        Toast.makeText(addRunActivity.this, "Added successfully",  Toast.LENGTH_LONG).show();
        Intent intent = new Intent(addRunActivity.this, GoogleMapActivity.class);
        intent.putExtra("cookie", cookie);
        startActivity(intent);
    }

    private void setFailure(){
        TextView error = findViewById(R.id.error);
        error.setVisibility(View.VISIBLE);
    }


    private void addActivity(long time, Double distance){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://10.0.2.2:5000/loc/add_activity";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("time", time);
            jsonBody.put("distance", distance);

            final String requestBody = jsonBody.toString();

            CustomStringRequest stringRequest = new CustomStringRequest(Request.Method.POST, cookie, URL, new Response.Listener<CustomStringRequest.ResponseM>() {
                @Override
                public void onResponse(CustomStringRequest.ResponseM result) {
                    //From here you will get headers
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

                @Override
                protected Response<ResponseM> parseNetworkResponse(NetworkResponse response) {
                    int mStatusCode = response.statusCode;
                    if(mStatusCode == 200){
                        setSuccess();
                    }

                    return super.parseNetworkResponse(response);
                }
            };
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}