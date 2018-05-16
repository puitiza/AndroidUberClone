package pe.anthony.androiduberclone;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Welcome extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

//      Play Services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mLocationTest;

    private static int UPDATE_INTERVAL = 5000; //This method sets the rate in milliseconds at which your app prefers to receive location updates
    private static int FATEST_INTERVAL = 3000; //This method sets the fastest rate in milliseconds at which your app can handle location updates
    private static int DISPLACEMENT =10; //desplazamiento

    DatabaseReference drivers;
    GeoFire geoFire;

    MaterialAnimatedSwitch location_switch;

    GoogleMap mMap;
    SupportMapFragment mapFragment;
    Marker mCurrent;
    FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//       Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    //mLastLocation = location;
                    if (mCurrent != null) {
                        mCurrent.remove();
                    }

                    //Place current location marker ... Al crear la activity se va a crear
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(latLng);
//                    markerOptions.title("Current Position");
//                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//                    mCurrent = mMap.addMarker(markerOptions);
//
//                    //move map camera
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                }
            }
        };

//        Inicializa los view
        location_switch = findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if(isOnline){
                    mLocationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if(mLocationTest == null) {
                                mLocationTest = locationResult.getLastLocation();
                                //Toast.makeText(getApplicationContext(),"Latitud : "+mLocationTest.getLatitude()+
                                //      "| Longitud : "+mLocationTest.getLongitude(),Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                                builder.setTitle("Longitud : " + mLocationTest.getLongitude() + "\nLatitud : " + mLocationTest.getLatitude());
                                builder.create();
                                builder.show();

                                for (Location location : locationResult.getLocations()) {
                                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                                    mLastLocation = location;
                                    if (mCurrent != null) {
                                        mCurrent.remove();
                                    }

                                    //Place current location marker ... Al crear la activity se va a crear
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title("You");
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                                    mCurrent = mMap.addMarker(markerOptions);

                                    //move map camera
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                                    //Draw animation rotate marker
                                    rotateMarker(mCurrent, -360, mMap);
                                    // Toast.makeText(getApplicationContext(),"Latitud : "+mLastLocation.getLatitude()+
                                    //         "| Longitud : "+mLastLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    };
                    startLocationUpdates();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),R.string.online,Snackbar.LENGTH_SHORT).show();
                }else {
                    mLocationTest = null;
                    stopLocationUpdates();
                    if(mCurrent!= null){
                        mCurrent.remove();
                    }
                    Snackbar.make(mapFragment.getView(),R.string.offline,Snackbar.LENGTH_SHORT).show();
                }
            }
        });
//        GeoFire
        //drivers = FirebaseDatabase.getInstance().getReference("Drivers");
        //geoFire = new GeoFire(drivers);
        setUpLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        if(location_switch.isChecked()){
                            displayLocation();
                        }
                    }
                }
        }
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//              Request runtime permission
            String[] Permissions ={Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, Permissions,MY_PERMISSION_REQUEST_CODE);
        }else {
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest(); //---estoy comentando esto porque ya esta definido en el onConnected
                if(location_switch.isChecked()){
                    displayLocation();
                }
            }
        }
    }

    private void createLocationRequest() {
        /*The priority of PRIORITY_HIGH_ACCURACY, combined with the ACCESS_FINE_LOCATION permission setting
        that you've defined in the app manifest, and a fast update interval of 5000 milliseconds (5 seconds),
        causes the fused location provider to return location updates that are accurate to within a few feet.
        This approach is appropriate for mapping apps that display the location in real time.*/
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //This will return the finest location available.
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); //Set the minimum displacement between location updates in meters
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .addApi(LocationServices.API)
                                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
//      Esta funcion es para compromar los play services del dispositivos
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resulCode = googleAPI.isGooglePlayServicesAvailable(this);
        if(resulCode != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(resulCode)){
                googleAPI.getErrorDialog(this, resulCode, PLAY_SERVICE_RES_REQUEST).show();
            }
            else{
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation !=null){
            if (location_switch.isChecked()){
                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();
                if(mCurrent != null){
                    mCurrent.remove();//Remove already marker
                    LatLng latLng = new LatLng(latitude,longitude);
                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                            .position(latLng)
                            .title("You"));
//                              Move camera to this positon
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
//                              Draw animation rotate marker
                    rotateMarker(mCurrent,-360,mMap);
                }
//                Actualiza en firebase
                /*geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
//                        Add Marker
                        if(mCurrent != null){
                                mCurrent.remove();//Remove already marker
                                LatLng latLng = new LatLng(latitude,longitude);
                                mCurrent = mMap.addMarker(new MarkerOptions()
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                                                .position(latLng)
                                                                .title("You"));
//                              Move camera to this positon
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
//                              Draw animation rotate marker
                                rotateMarker(mCurrent,-360,mMap);
                        }
                    }
                });*/
            }
        }else {
            Log.d("ERROR","CANNOT GET YOUR LOCATION");
        }
    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation= mCurrent.getRotation();
        final float duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot > 180?rot/2:rot);
                if (t<1.0){
                        handler.postDelayed(this,16);
                }
            }
        });
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
            buildGoogleApiClient();
/*      Esto es para setear el blue dot ---  punto azul de tu localizacon
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }*/
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        displayLocation();
//    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
/*        displayLocation();
        startLocationUpdates();*/
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
//        stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }
}
