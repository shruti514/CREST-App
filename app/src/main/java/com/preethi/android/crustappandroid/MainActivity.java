package com.preethi.android.crustappandroid;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech engine;
    private double pitch=1.3f;
    private double speed=0.8f;
    int cIndex = 0;
    String text = "Welcome to V T A";
    String[] mainScreenText = {"Welcome to V T A"," Select the Bus No","Bus Number 60","Bus Number 181","Bus Number 66"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        engine = new TextToSpeech(this, this);

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
    public void onResume(){
        super.onResume();
        Log.d("In on Resume","Speech");
       // speakBusInformation();
    }

    @Override
    protected void onPause(){

        if(engine != null)
        {
            engine.stop();
            engine.shutdown();
        }

        super.onPause();
    }
    @Override
    public void onInit(int status) {
        Log.d("Speech", "OnInit - Status ["+status+"]");

        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            engine.setLanguage(Locale.US);
            engine.setPitch((float) pitch);
            engine.setSpeechRate((float) speed);
            speech();

        }
    }

    private void speakBusInformation(String speakNow) {

           // String speakNow = mainScreenText[cIndex];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(speakNow);
            } else {
                ttsUnder20(speakNow);
            }

        }


    public void bus1Action(View view){
        speakBusInformation("Bus number 60 San Jose");
    }
    public void bus2Action(View view){
        speakBusInformation("Bus number 181 Fremont");
    }
    public void bus3Action(View view){
        speakBusInformation("Bus number 66 Great Mall");
    }

    private void speech() {
        engine.setPitch((float) pitch);
        engine.setSpeechRate((float) speed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21("Welcome to V T A Select the Bus Number");
        } else {
            ttsUnder20("Welcome to V T A Select the Bus Number");
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onDestroy() {
        if (engine != null) {
            engine.stop();
            engine.shutdown();
        }
        super.onDestroy();
    }
}
