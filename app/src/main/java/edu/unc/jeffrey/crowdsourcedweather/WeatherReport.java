package edu.unc.jeffrey.crowdsourcedweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

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
        Intent newIntent = new Intent(this, WeatherMap.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(newIntent);
    }

    public void checkToEnable(View view) {
        if (rainGroup.getCheckedRadioButtonId() != -1 && jacketGroup.getCheckedRadioButtonId() != -1 && emergencyGroup.getCheckedRadioButtonId() != -1) {
            Button submit = findViewById(R.id.submit);
            submit.setEnabled(true);
        }
    }
}
