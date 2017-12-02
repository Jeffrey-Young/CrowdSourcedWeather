package edu.unc.jeffrey.crowdsourcedweather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    public static String user;
    public static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view) {
        TextView tv = findViewById(R.id.username);
        user = tv.getText().toString();

        TextView tv2 = findViewById(R.id.password);
        password = tv2.getText().toString();
        new HttpAsyncTask().execute("https://weatherclone.hopto.org/login.php");
    }

    private void authenticate(JSONObject auth) {
        try {
            if (auth.getInt("isAuthenticated") == 1) {
                Intent newIntent = new Intent(this, WeatherMap.class);
                startActivity(newIntent);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Invalid username or password. Try again or create account.", Toast.LENGTH_LONG).show();
                        TextView tv = findViewById(R.id.username);
                        tv.setText(new char[0], 0, 0);
                        TextView tv2 = findViewById(R.id.password);
                        tv2.setText(new char[0], 0, 0);
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void createAccount(View view) {
        sendNotification();
        TextView tv = findViewById(R.id.username);
        user = tv.getText().toString();

        TextView tv2 = findViewById(R.id.password);
        password = tv2.getText().toString();
        new HttpAsyncTask().execute("https://weatherclone.hopto.org/register.php");
    }

    private void createAccountResult(JSONObject received) {
        try {
            if (received.getInt("received") == 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_SHORT).show();
                    }

                });
                Intent newIntent = new Intent(this, WeatherMap.class);
                startActivity(newIntent);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Account already exists! Try again.", Toast.LENGTH_SHORT).show();
                        TextView tv = findViewById(R.id.username);
                        tv.setText(new char[0], 0, 0);
                        TextView tv2 = findViewById(R.id.password);
                        tv2.setText(new char[0], 0, 0);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.thunderstormicon)
                        .setContentTitle("Weather Survey")
                        .setContentText("Fill out a weather Survey!");

        Intent resultIntent = new Intent(this, WeatherReport.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private static String post(String url) {
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
            jsonObject.accumulate("username", user + "");
            jsonObject.accumulate("password", password + "");

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
            if (inputStream == null) {
                result = "Did not work!";
            }
            result = convertInputStreamToString(inputStream);

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage() + "");
        }

        // 11. return result
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
            String result = post(urls[0]);
            if (urls[0].contains("login")) {
                try {
                    Log.v("AUTH", result + "");
                    authenticate(new JSONObject(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Log.v("CREATE", result + "");
                    createAccountResult(new JSONObject(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                }            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }
}
