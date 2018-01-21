package pe.anthony.androiduberclone;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pe.anthony.androiduberclone.Common.Common;
import pe.anthony.androiduberclone.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Welcome extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

//      Play Services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

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

    //Car animation
    private List<LatLng> polyLineList;
    private  Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPostion,endPosition,currentPosition;
    private int index,next;
    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyline,greyPolyline;
    private IGoogleAPI mService;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1){
                index++;
                next = index+1;
            }
            if(index<polyLineList.size()-1){
                startPostion = polyLineList.get(index);
                endPosition = polyLineList.get(index);
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPostion.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPostion.latitude;
                    LatLng newPos = new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPostion,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(newPos).zoom(15.5f).build()));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startPostion, LatLng endPosition) {
        double lat = Math.abs(startPostion.latitude - endPosition.latitude);
        double lng = Math.abs(startPostion.longitude - endPosition.longitude);
        if(startPostion.latitude < endPosition.latitude && startPostion.longitude < endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }else if(startPostion.latitude >= endPosition.latitude && startPostion.longitude < endPosition.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        }else if(startPostion.latitude >= endPosition.latitude && startPostion.longitude >= endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }else if(startPostion.latitude < endPosition.latitude && startPostion.longitude >= endPosition.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        }
        return -1;
    }




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
                    mLastLocation = location;
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
                                markerOptions.title("Your location");
//                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                                mCurrent = mMap.addMarker(markerOptions);

                                //move map camera
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
                                //Draw animation rotate marker
                                //rotateMarker(mCurrent,-360,mMap);
                            }
                        }
                    };
                    startLocationUpdates();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),R.string.online,Snackbar.LENGTH_SHORT).show();
                }else {
                    stopLocationUpdates();
                    if(mCurrent!= null){
                        mCurrent.remove();
                        mMap.clear();
                        if(handler!=null){
                            handler.removeCallbacks(drawPathRunnable);
                        }
                    }
                    Snackbar.make(mapFragment.getView(),R.string.offline,Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        polyLineList = new ArrayList<>();
        btnGo = findViewById(R.id.btnGo);
        edtPlace = findViewById(R.id.edtPlace);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destination = edtPlace.getText().toString();
                destination = destination.replace(" ","+");//replace space with + for fetch data
                Log.d("LISTO",destination);
                getDirection();
            }
        });



//        GeoFire
        drivers = FirebaseDatabase.getInstance().getReference("Drivers");
        geoFire = new GeoFire(drivers);
        setUpLocation();

        mService = Common.getGoogleAPI();
    }

    private void getDirection() {
        currentPosition = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                        "mode=driving&"+
                        "transit_routing_preference=less_driving&"+
                        "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                        "destination="+destination+"&"+
                        "key="+getResources().getString(R.string.google_direction_api);
            Log.d("KEY", requestApi);//print the URL for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try{
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray =  jsonObject.getJSONArray("routes");
                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
//                                    Adjusting bounds
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for(LatLng latLng:polyLineList)
                                        builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                    mMap.animateCamera(mCameraUpdate);

                                    polylineOptions= new PolylineOptions();
                                    polylineOptions.color(Color.GRAY);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap( new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polyLineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.endCap(new SquareCap());
                                    blackPolylineOptions.jointType(JointType.ROUND);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                    mMap.addMarker(new MarkerOptions()
                                                        .position(polyLineList.get(polyLineList.size()-1))
                                                        .title("Pickup Location"));
//                                    Animation
                                    ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                    polyLineAnimator.setDuration(2000);
                                    polyLineAnimator.setInterpolator(new LinearInterpolator());
                                    polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            List<LatLng> points = greyPolyline.getPoints();
                                            int percentValue = (int)valueAnimator.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int)(size*(percentValue/100.0f));
                                            List<LatLng> p =points.subList(0,newPoints);
                                            blackPolyline.setPoints(p);
                                        }
                                    });
                                    polyLineAnimator.start();
                                    carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).flat(true)
                                                                                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                    handler = new Handler();
                                    index= -1;
                                    next=1;
                                    handler.postDelayed(drawPathRunnable,3000);
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List decodePoly(String encoded) {
            List poly = new ArrayList();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
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

//                Actualiza en firebase
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
//                        Add Marker
                        if(mCurrent != null){
                                mCurrent.remove();//Remove already marker
                                LatLng latLng = new LatLng(latitude,longitude);
                                mCurrent = mMap.addMarker(new MarkerOptions()
//                                                              .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                                                .position(latLng)
                                                                .title("Your Location"));
//                              Move camera to this positon
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
//                              Draw animation rotate marker
//                                rotateMarker(mCurrent,-360,mMap);
                        }
                    }
                });
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
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
