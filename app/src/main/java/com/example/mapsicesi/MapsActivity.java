package com.example.mapsicesi;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private String user;
    private LocationManager manager;
    private Marker me;
    private ArrayList<Marker> points;
    private ArrayList<Pothole> potholes;
    private Button addBtn;
    private Button ubiBtn;
    private LocationWorker locationWorker;
    private Position currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        addBtn = findViewById(R.id.addBtn);
        ubiBtn = findViewById(R.id.ubiBtn);

        user = getIntent().getExtras().getString("user");
        points = new ArrayList<>();
        potholes = new ArrayList<Pothole>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }




    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,2,this);
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
       setInitialPos();

       mMap.setOnMapClickListener(this);
       mMap.setOnMapLongClickListener(this);
       mMap.setOnMarkerClickListener(this);

       addBtn.setOnClickListener(
               (v) -> {
                Intent i = new Intent(MapsActivity.this, PopUp.class);

                   Geocoder geocoder = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                   try {
                       List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);



                   String coord = (me.getPosition().latitude + ", " + me.getPosition().longitude).toString();
                       double latitud = me.getPosition().latitude;
                       double longitud = me.getPosition().longitude;
                   Toast.makeText(this, me.getPosition().latitude + ", " + me.getPosition().longitude, Toast.LENGTH_LONG).show();
                    i.putExtra("coordenadas",coord);

                    String addr = addresses.get(0).getAddressLine(0);
                    i.putExtra("direccion", addr);
                    i.putExtra("latitud",latitud);
                    i.putExtra("longitud",longitud);
                    i.putExtra("user", user);
                    startActivity(i);

                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
       );

        ubiBtn.setOnClickListener(
                (v) -> {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me.getPosition(), 8));
                }
        );

    locationWorker = new LocationWorker(this);
    locationWorker.start();

    }

    protected void onDestroy() {
        locationWorker.finish();

        super.onDestroy();
    }
    @SuppressLint("MissingPermission")
    public void setInitialPos(){
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            updateMyLocation(location);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(me == null) {
            me = mMap.addMarker(new MarkerOptions().position(myPos).title("Yo"));

        } else {
            me.setPosition(myPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos,18));
        mMap.setOnMarkerClickListener(this);


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updatePotholes(){
        HTTPSWebUtilDomi httpsWebUtilDomi = new HTTPSWebUtilDomi();
        new Thread(
                () ->{


                    String response = httpsWebUtilDomi.GETrequest("https://reto1apps-542cb.firebaseio.com/potholes");

                    Gson gson = new Gson();
                    Type type = new TypeToken<HashMap<String,Pothole>>(){}.getType();

                    HashMap<String,Pothole> ph = gson.fromJson(response,type);
                    ph.forEach(
                            (key, value)->{
                                boolean isListed = false;
                                for(Pothole pothole : potholes){
                                    if (pothole.getId().equals(value.getId())){
                                        pothole.setConfirmed(value.isConfirmed());
                                        if(pothole.getMarker()== null){
                                            runOnUiThread(
                                                    () ->{
                                                        pothole.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(value.getLatitude(), value.getLongitude())).title("hueco")));
                                                        pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(50));
                                                    }

                                            );
                                        }else{
                                            runOnUiThread(
                                                    () ->{
                                                        if(pothole.isConfirmed()){
                                                            pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(35));
                                                        }else{
                                                            pothole.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(50));
                                                        }
                                                        //pothole.setMarker(new LatLng(pothole.getLatitude(), pothole.getLongitude()));
                                                    }
                                            );
                                        }
                                        isListed = true;
                                    }
                                }
                            }
                    );

                }
        ).start();
    }


    public void updateMyLocation(Location location){
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(me==null){
            me = mMap.addMarker(new MarkerOptions().position(myPos).title("Yo"));
        }else{
            me.setPosition(myPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myPos));

        currentPosition = new Position(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( latLng, 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker p = mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador"));
        points.add(p);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        marker.showInfoWindow();
        return true;
    }


    public Position getCurrentPosition(){
        return currentPosition;
    }

    public String getUser(){
        return user;
    }
}