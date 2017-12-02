package edu.unc.jeffrey.crowdsourcedweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;


/**
 * Created by Jeffrey on 12/2/2017.
 */

public class WeatherReport extends AppCompatActivity {

    private RadioGroup rainGroup;
    private RadioGroup jacketGroup;
    private RadioGroup emergencyGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_report);
        rainGroup = (RadioGroup) findViewById(R.id.raining);
        jacketGroup = (RadioGroup) findViewById(R.id.jacket);
        emergencyGroup = (RadioGroup) findViewById(R.id.emergency);
    }

    public void submitReport(View view) {
        new HttpAsyncTask().execute("https://weatherclone.hopto.org/addWeather.php");
        Intent newIntent = new Intent(this, WeatherMap.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Toast.makeText(this, "Successfully submitted report", Toast.LENGTH_SHORT).show();
        startActivity(newIntent);
    }

    public void checkToEnable(View view) {
        if (rainGroup.getCheckedRadioButtonId() != -1 && jacketGroup.getCheckedRadioButtonId() != -1 && emergencyGroup.getCheckedRadioButtonId() != -1) {
            Button submit = findViewById(R.id.submit);
            submit.setEnabled(true);
        }
    }

    private static String post(String url, boolean isRaining, boolean needJacket, boolean isEmergency){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url + "");

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("lat", WeatherMap._location.getLatitude() + "");
            jsonObject.accumulate("long", WeatherMap._location.getLongitude() + "");
            jsonObject.accumulate("raining", isRaining ? 1 : 0);
            jsonObject.accumulate("jacket", needJacket ? 1 : 0);
            jsonObject.accumulate("emergency", isEmergency ? 1 : 0);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();
            Log.v("JSON", json + "");

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream == null) {
                result = "Did not work!";
            }
            Log.v("Result", convertInputStreamToString(inputStream) + "");

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage() + "");
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return post(urls[0], rainGroup.getCheckedRadioButtonId() == R.id.raining_yes ? true : false, jacketGroup.getCheckedRadioButtonId() == R.id.jacket_yes ? true : false, emergencyGroup.getCheckedRadioButtonId() == R.id.emergency_yes ? true : false);

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }
}
