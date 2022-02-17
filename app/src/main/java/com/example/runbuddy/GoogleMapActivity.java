package com.example.runbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {


    private GoogleMap mMap;
    //private ActivityMapsBinding binding;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private Circle mRadiusCircle;
    private double radiusNumber = 0;
    private Marker mMarkLocation;
    private String cookie;
    private double radius,longitude, latitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        Intent intent = getIntent();
        cookie = intent.getStringExtra("cookie");
        SeekBar SeekBar = (SeekBar)findViewById(R.id.seekBar);
        // perform seek bar change listener event used for getting the progress value
        SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusNumber = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button apply = findViewById(R.id.apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setRadius(radius, longitude, latitude);
                }
                catch (Exception error){
                    Log.e("SEND_LOC", error.toString());
                }
            }
        });
        //add toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbargoogle);
            setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                 return true;
            case R.id.StartActivity:
                // User chose the "StartActivity" action, mark the current item
                // as a favorite...
                intent = new Intent(GoogleMapActivity.this, addRunActivity.class);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
                return true;
            case R.id.ShowActivities:
                // User chose the "ShowActivities" action, mark the current item
                // as a favorite...
                intent = new Intent(GoogleMapActivity.this, showActivitiesActivity.class);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMarkLocation = mMap.addMarker(new MarkerOptions()
                        .position(mDefaultLocation)
                        .title(getString((R.string.dropped_pin))));

        mRadiusCircle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(-33.8523341, 151.2106085))
                .radius(0)
                .fillColor(Color.TRANSPARENT)
                .strokeColor(Color.BLACK)
                .strokeWidth(10));

        Button ApplyButton = (Button) findViewById(R.id.apply);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation,DEFAULT_ZOOM));
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
        }

        updateLocationUI();
        getDeviceLocation();

        mMap.setOnMarkerClickListener(this);

        //on click inside the circle
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });

        // Allow users to add a marker using a long click
        setMapLongClick(mMap);

        //POI listener
        setPoiClick(mMap);
    }

    private void setSearchCircle(LatLng newLatlng) {
        if (mMap != null) {
             mRadiusCircle = mMap.addCircle(new CircleOptions()
                    .center(newLatlng)
                     .radius((radiusNumber*10))
                    .fillColor(Color.TRANSPARENT)
                    .strokeColor(Color.BLACK)
                    .strokeWidth(10));
        }
    }

    private void moveSearchCircle(LatLng newLatlng) {
        mRadiusCircle.setCenter(newLatlng);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mDefaultLocation != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            moveSearchCircle(currentLatLng);
        }
    }

    //Add POI listener with radius circle
    private void setPoiClick(GoogleMap googleMap){

        googleMap.setOnPoiClickListener(poi ->
                {
                    mMarkLocation.setPosition(poi.latLng);
                    mMarkLocation.setTitle(poi.name);
                    mMarkLocation.showInfoWindow();
                    //setSearchCircle(poi.latLng);
                    mRadiusCircle.setCenter(poi.latLng);
                    mRadiusCircle.setRadius((10*radiusNumber));
                    mRadiusCircle.setFillColor(Color.TRANSPARENT);
                    mRadiusCircle.setStrokeColor(Color.BLACK);
                    mRadiusCircle.setStrokeWidth(10);
                    //moveSearchCircle(poi.latLng);
                }
        );
    }


    //Allow users to add a marker using a long click
    public void setMapLongClick(GoogleMap googleMap) {

        googleMap.setOnMapLongClickListener(latLng ->
                {
                String snippet = String.format(Locale.getDefault(), "Lat:%1$.5f, Long:%2$.5f", latLng.latitude, latLng.longitude);
                mMarkLocation.setPosition(latLng);
                mMarkLocation.setTitle(getString((R.string.dropped_pin)));
                mMarkLocation.setSnippet(snippet);
                mMarkLocation.showInfoWindow();
                //setSearchCircle(latLng);
                mRadiusCircle.setCenter(latLng);
                mRadiusCircle.setRadius((10*radiusNumber));
                mRadiusCircle.setFillColor(Color.TRANSPARENT);
                mRadiusCircle.setStrokeColor(Color.BLACK);
                mRadiusCircle.setStrokeWidth(10);
                radius = 10*radiusNumber;
                longitude = latLng.longitude;
                latitude = latLng.latitude;
                //moveSearchCircle(latLng);
                });
    }

    private void updateLocationUI(){
        if (mMap == null){
            return;
        }
        try{
            if (mLocationPermissionGranted){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else{
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        }
        catch (SecurityException e){
            Log.i("exeption: %s", e.getMessage());
        }
    }

    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); //not need
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

        updateLocationUI();
    }

    //getting last known location and moving the map to it.
    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(GoogleMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //if permitted
                Task locationResult = mfusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(GoogleMapActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        try{
                            mLastKnownLocation = (Location) task.getResult();
                            Log.i("my location: ", mLastKnownLocation.toString());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));
                        }catch(Exception e){
                            Log.e("Null Location", "Exception", e );
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation,DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                });
            }else{
                requestLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //asking for location permission
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    private void setRadius(double radius, double longitude, double latitude){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://10.0.2.2:5000/loc/set_radius";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("radius", radius);
            jsonBody.put("longitude", longitude);
            jsonBody.put("latitude", latitude);

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
            };
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}