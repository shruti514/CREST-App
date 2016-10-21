package com.preethi.android.crustappandroid;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/////
/////hey this is final

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    protected static final int REQUEST_OK = 1;
    TTSManager ttsManager = null;
    SensorManager mySensorManager;
    Sensor myProximitySensor;
    int busNumber1 = 55, busNumber2 = 60, busNumber3 = 181;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Toolbar for the app
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ttsManager = new TTSManager();
        ttsManager.init(this);
        final Context context = this;

        //Set Proximity Sensors
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //Check for proximity sensor in phone
        if (myProximitySensor == null) {
            Toast.makeText(this, "No Proximity Sensor.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Speak to choose Buses", Toast.LENGTH_SHORT).show();
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue(" Welcome to V T A Select the Bus Number");
            }
        }, 1000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("Bus number 55 De Anza College");
            }
        }, 5000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("Bus number 60 Mission College");
            }
        }, 9000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("Bus number 181 Fremont ");
            }
        }, 13000);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        mySensorManager.registerListener(this, myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("In on Resume", "Speech");
        // speakBusInformation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(this, myProximitySensor);
    }


    public void bus1Action(View view) {
        ttsManager.initQueue("Bus number 55 De Anza College Selected");
        getBusInformation(busNumber1);
    }

    public void bus2Action(View view) {
        ttsManager.initQueue("Bus number 60 Mission College Selected");
        getBusInformation(busNumber2);
    }

    public void bus3Action(View view) {
        ttsManager.initQueue("Bus number 181 Fremont Selected");
        getBusInformation(busNumber3);
    }

    //Sensor APIs
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    //Check for user Proximity and enable Speech to text conversion
    public void onSensorChanged(SensorEvent event) {
        //  ((TextView) findViewById(R.id.text2)).setText("Got a sensor event: " + event.values[0] + " centimeters");
        if (event.values[0] == 0.0) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(i, REQUEST_OK);
            } catch (Exception e) {
                Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Convert for Speech to text and check if the corresponding bus numbers are present and navigate to the next screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String inputText = thingsYouSaid.get(0);
            boolean b = inputText.contains("55");
            boolean c = inputText.contains("60");
            boolean d = inputText.contains("181");
            if (b == true) {
                //  Toast.makeText(this, "Bus 60", Toast.LENGTH_SHORT).show();
                getBusInformation(busNumber1);
            } else if (c == true) {
                //Toast.makeText(this, "Click bus 181", Toast.LENGTH_SHORT).show();
                getBusInformation(busNumber2);
            } else if (d == true) {
                //Toast.makeText(this, "Click bus 66", Toast.LENGTH_SHORT).show();
                getBusInformation(busNumber3);
            } else {
                Toast.makeText(this, "No buses", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void getBusInformation(int getBusNumber) {
        Log.d("Bus No is ", ":" + getBusNumber);
        Intent intent = new Intent(this, BusInformation.class);
        intent.putExtra("busno", getBusNumber);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }

}
