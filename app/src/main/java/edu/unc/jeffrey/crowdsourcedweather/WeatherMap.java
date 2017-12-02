package edu.unc.jeffrey.crowdsourcedweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;     // important distinction; multiple versions exist
//import android.location.LocationListener;                  // wrong version of the location listener
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class WeatherMap extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /*
    Note:
    1) Make sure you've installed Google Play Services via the SDK Manager
        (try testing out the app before doing this though, might not be necessary right now)
    2) Make sure the correct location import is assigned.
    3) Make sure the app has permissions to access location services (go into phone's app settings manually after installing APK)
     */
    private String TAG = "ERROR";
    protected GoogleApiClient c;
    protected LocationRequest req;
    protected Location lastLocation;
    protected String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_map);
        buildApiClient();
    }

    // Establish the Google API client
    public void buildApiClient(){
        if(c==null){
            c = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle bundle){
        try{
            // Don't really need to get the last location; not using it right now
            Location loc = LocationServices.FusedLocationApi.getLastLocation(c);
        }catch(SecurityException ex){ex.printStackTrace();}
        req = new LocationRequest();
        req.setInterval(10000);
        req.setFastestInterval(5000);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Make sure location permission has been granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(c, req, this);
        }else{
            Log.d(TAG, "Location permission not granted.  Check permissions.");
        }

    }

    @Override
    public void onLocationChanged(Location location){
        lastLocation = location;
        double lat = location.getLatitude(); double lon = location.getLongitude();
        TextView tv = (TextView) findViewById(R.id.locview);
        Geocoder g = new Geocoder(this);
        try{
            List<Address> la = g.getFromLocation(lat, lon, 1);
            // Parse the address list to get a simplified string version of the address
            address = la.get(0).getAddressLine(0) +" "+la.get(0).getAddressLine(1) +" "+la.get(0).getAddressLine(2);
        }catch(Exception ex){ex.printStackTrace();}
        // Update the TextView on the activity
        tv.setText("Latitude: "+lat+" Longitude: "+lon+"\n"+address);
        tv.invalidate();
    }

    @Override
    public void onConnectionSuspended(int i){
        c.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    protected void onStart(){
        super.onStart();
        c.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        c.disconnect();
    }



}
