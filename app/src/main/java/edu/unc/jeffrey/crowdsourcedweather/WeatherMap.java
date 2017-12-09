package edu.unc.jeffrey.crowdsourcedweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class WeatherMap extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    GoogleMap googleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Marker marker;
    public static Location _location;
    JSONObject obj;
    String temp;
    String conditions;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_map);

        new HttpAsyncTask().execute("https://weatherclone.hopto.org/pullWeather.php");

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        new HttpAsyncTask().execute("https://weatherclone.hopto.org/pullWeather.php");

    }

    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;

        setUpMap();

    }

    public void setUpMap() {

        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        _location = location;

        //unregister location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        new HttpAsyncTask().execute("http://api.wunderground.com/api/d5ec695718ce668d/conditions/q/" + latLng.latitude + "," + latLng.longitude + ".json");
        //place marker where user just clicked
        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void report(View view) {
        Intent newIntent = new Intent(this, WeatherReport.class);
        startActivity(newIntent);
    }

    public String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);

            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            result = GET(urls[0]);
            if (urls[0].contains("weatherclone")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.v("ARRAY", jsonArray.toString() + "");
                        Log.v("SIZE", jsonArray.length() + "");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                obj = jsonArray.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                Marker m = googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(obj.getString("lat")), Double.parseDouble(obj.getString("long"))))
                                        .title(new java.util.Date((long) obj.getInt("time") * 1000).toString())
                                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), obj.getInt("emergency") == 1 ? R.drawable.exclamationsmall : obj.getInt("raining") == 1 ? R.drawable.thunderstormsmall : obj.getInt("jacket") == 1 ? R.drawable.coldsmall : R.drawable.sunnysmall))));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else { // we are querying WU
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = null;
                        Log.v("WEATHER", result + "");
                        try {
                            json = new JSONObject(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView temp = findViewById(R.id.temperature);
                        try {
                            String text = temp.getText() + (json.getJSONObject("current_observation").getDouble("temp_f") + "°F");
                            temp.setText(text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TextView cond = findViewById(R.id.condition);
                        try {
                            String text = cond.getText() + json.getJSONObject("current_observation").getString("weather");
                            cond.setText(text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        }
    }
}