package com.example.runbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

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
    private Circle radiusCircle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

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
        switch (item.getItemId()) {
            case R.id.RadiusSetting:
                // User chose the "RadiusSetting" item, show the app settings UI...
                Toast.makeText(this, "RadiusSetting selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.StartActivity:
                // User chose the "StartActivity" action, mark the current item
                // as a favorite...
                Toast.makeText(this, "StartActivity selected", Toast.LENGTH_SHORT).show();
                this.finish();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Button ApplyButton = (Button) findViewById(R.id.button);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation,DEFAULT_ZOOM));
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
        }


        updateLocationUI();
        getDeviceLocation();

        mMap.setOnMarkerClickListener(this);

        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,DEFAULT_ZOOM));
        //mMap.setOnMarkerClickListener(this);

        //draw circle
        //mMap.addCircle(new CircleOptions()
                //.center(new LatLng(-34, 151))
                //.center(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                //.radius(1000)
                //.strokeWidth(10)
                //.strokeColor(Color.BLACK)
                //.fillColor(Color.argb(128, 255, 0, 0))
               //.clickable(true));

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
             radiusCircle = mMap.addCircle(new CircleOptions()
                    .center(newLatlng)
                    //.radius(1000)
                     .radius(searchRadius())
                    .fillColor(Color.TRANSPARENT)
                    .strokeColor(Color.BLACK)
                    .strokeWidth(10));
        }
    }

    private double searchRadius(){

        return 500;
    }

    private void moveSearchCircle(LatLng newLatlng) {
        if(radiusCircle != null){
            radiusCircle.setCenter(newLatlng);
        }
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
                    MarkerOptions lid = new MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name);
                    Marker poiMarker = googleMap.addMarker(lid);
                    poiMarker.showInfoWindow();
                    setSearchCircle(poi.latLng);

                }
        );
    }


    //Allow users to add a marker using a long click
    public void setMapLongClick(GoogleMap googleMap) {

        googleMap.setOnMapLongClickListener(latLng ->
                {
                String snippet = String.format(Locale.getDefault(), "Lat:%1$.5f, Long:%2$.5f", latLng.latitude, latLng.longitude);
                MarkerOptions lid = new MarkerOptions()
                            .position(latLng)
                            .title(getString((R.string.dropped_pin)))
                            .snippet(snippet);
                googleMap.addMarker(lid);
                setSearchCircle(latLng);
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

}