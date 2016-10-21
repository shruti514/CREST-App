package com.preethi.android.crustappandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;


public class BusInformation extends AppCompatActivity implements BeaconConsumer, RangeNotifier{

    private Integer mClosestBeacon = 0;
    private Double mClosestBeaconDistance = Double.MAX_VALUE;
    private BeaconManager mBeaconManager;
    TTSManager ttsManager = null;
    private Integer mCurrentDestinationBusStop = 0;
    private double pitch=1.3f;
    private double speed=0.8f;
    private int cIndex = 0;
    private int userDestinationBus;
    private APIRequest.APIResponseHandler mClosestBusStopResponseHandler;
    private APIRequest.APIResponseHandler mPathResponseHandler;
    private APIRequest.APIResponseHandler mCrowdEstimatorHandler;
    public ArrayList list = new ArrayList();
    private Handler mHandler = new Handler();
    Boolean noBeaconDetected = false;

    private static TextView stepsText;
    private static TextView analyticsText;


    private static class ClosestBusStopResponseHandler implements APIRequest.APIResponseHandler {
        private WeakReference<BusInformation> mBusInformationRef;

        public ClosestBusStopResponseHandler(BusInformation activity) {
            mBusInformationRef = new WeakReference<>(activity);
        }

        @Override
        public void handleResponse(String response) {
            try{
            if (mBusInformationRef.get() == null) {
                return;
            }
            if (response.equals(APIRequest.ERROR)) {
                Toast.makeText(mBusInformationRef.get(), "Some error occurred", Toast.LENGTH_SHORT).show();
                return;
            } else if (mBusInformationRef.get().mCurrentDestinationBusStop == 0) {
                Toast.makeText(mBusInformationRef.get(), "Destination Bus Stop not set", Toast.LENGTH_SHORT).show();
                return;
            }
            String sourceBusStop = response.replace("\n", "");
            JSONObject jsonObject = new JSONObject(response);
            new PathRequest(jsonObject.getString("busNumber"),
                    mBusInformationRef.get().mCurrentDestinationBusStop.toString(),
                    mBusInformationRef.get().mPathResponseHandler).execute();

        }catch (Exception ex){
                Log.e("app", ex.getMessage());
            }
        }

    }
    static String rtinfo  = null;
    static String crowdinfo  = null;

    private static class PathResponseHandler implements APIRequest.APIResponseHandler {
        private WeakReference<BusInformation> mBusInformationRef;


        public PathResponseHandler(BusInformation activity) {
            mBusInformationRef = new WeakReference<>(activity);
        }

        @Override
        public void handleResponse(String response) {
            try {
                if (mBusInformationRef.get() == null) {
                    return;
                }
                JSONObject jsonObject = new JSONObject(response);

                Log.d("app-resp",response.toString());
                Log.d("app-mbus",mBusInformationRef.get().toString());
                Log.d("app-json",jsonObject.toString());

                stepsText.setText(jsonObject.getString("routeDescription"));
                rtinfo = jsonObject.getString("routeDescription");


               // Toast.makeText(mBusInformationRef.get(), jsonObject.getString("routeDescription"), Toast.LENGTH_SHORT).show();
                //   mBusInformationRef.get().speakBusInformation(response);
            }catch (Exception ex){
                Log.e("app",ex.getMessage());
            }
        }
    }

    private static class CrowdEstimatorResponseHandler implements  APIRequest.APIResponseHandler{
        private WeakReference<BusInformation> mBusInformationRef;

        public CrowdEstimatorResponseHandler(BusInformation activity){
            mBusInformationRef= new WeakReference<>(activity);

        }

        @Override
        public void handleResponse(String response) {
            try {
                if (mBusInformationRef.get() == null) {
                    return;
                }
                JSONObject jsonObject = new JSONObject(response);
                analyticsText.setText(jsonObject.getString("predictedValue"));
                crowdinfo = jsonObject.getString("predictedValue");
             //   Toast.makeText(mBusInformationRef.get(), response, Toast.LENGTH_LONG).show();

            }
            catch(Exception e){

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ttsManager = new TTSManager();
        ttsManager.init(this);

        stepsText = (TextView) findViewById(R.id.steps);
        analyticsText = (TextView) findViewById(R.id.analytics);

        Intent intent = getIntent();
        userDestinationBus = intent.getIntExtra("busno", 0); // here 0 is the default value

        TextView busNumber = (TextView) findViewById(R.id.busNo_textview);

        busNumber.setText("Route for Bus Number" + userDestinationBus);
        verifyLocationsPermissions(this);

        mClosestBusStopResponseHandler = new ClosestBusStopResponseHandler(this);
        mPathResponseHandler = new PathResponseHandler(this);
        mCrowdEstimatorHandler = new CrowdEstimatorResponseHandler(this);

        if (savedInstanceState != null) {
            mClosestBeacon = savedInstanceState.getInt("closest_beacon");
            mClosestBeaconDistance = savedInstanceState.getDouble("closest_beacon_distance");
            mCurrentDestinationBusStop = savedInstanceState.getInt("current_destination");
        }



    }

    public static void verifyLocationsPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
        }
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
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    public void onStart(){
        super.onStart();
        if(noBeaconDetected){
            stepsText.setText(" No Beacon Detected");

        }
        if (rtinfo !=null) {
            ttsManager.initQueue(rtinfo);
        }
        if (crowdinfo !=null) {
            ttsManager.addQueue(crowdinfo);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        mBeaconManager.bind(this);
        Log.d("In on Resume", "Speech");
    }

    @Override
    protected void onPause(){
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("closest_beacon", mClosestBeacon);
        outState.putDouble("closest_beacon_distance", mClosestBeaconDistance);
        outState.putInt("current_destination", mCurrentDestinationBusStop);
    }

    public void bus1Action() {
        if (mClosestBeacon == 0) {
            stepsText.setText("No navigation Available for the bus stop");
          //    speakBusInformation("Cannot Detect Any Beacon Close to you");
//            Toast.makeText(mBusInformationRef.get(), "Some error occurred", Toast.LENGTH_SHORT).show();
//            Toast.makeText("Alert","No Beacons detected ", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentDestinationBusStop = userDestinationBus;
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();


        list=datePicker();
        String month =String.valueOf(list.get(0));
        String weekday= String.valueOf(list.get(1));
        String hour=String.valueOf(list.get(2));
        String busNumber=String.valueOf(userDestinationBus);
        String tripRoute="";
        if(userDestinationBus == 55) {
            tripRoute = "busStop-55";
        }else if(userDestinationBus == 60){
            tripRoute = "busStop-60";
        }else if(userDestinationBus == 181){
            tripRoute= "busStop-181";
        }
        new CrowdEstimatorRequest(mCrowdEstimatorHandler,month,weekday,hour,tripRoute, busNumber).execute();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("Route for Bus Number" + userDestinationBus);

            }
        }, 1000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if(rtinfo != null && crowdinfo != null) {
                    String read = rtinfo + crowdinfo;
                    ttsManager.initQueue(read);
                }
            }
        }, 5000);

    }

    public void bus2Action() {
        if (mClosestBeacon == 0) {
            return;
        }

        mCurrentDestinationBusStop = userDestinationBus;
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();

        list=datePicker();
        String month =String.valueOf(list.get(0));
        String weekday= String.valueOf(list.get(1));
        String hour=String.valueOf(list.get(2));
        String busNumber=String.valueOf(userDestinationBus);
        String tripRoute="";
        if(userDestinationBus == 55) {
            tripRoute = "busStop-55";
        }else if(userDestinationBus == 60){
            tripRoute ="busStop-60";
        }else if(userDestinationBus == 181){
            tripRoute="busStop-181";
        }
        new CrowdEstimatorRequest(mCrowdEstimatorHandler,month,weekday,hour,tripRoute, busNumber).execute();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                    ttsManager.initQueue("Route for Bus Number" + userDestinationBus);

            }
        }, 1000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if(rtinfo != null && crowdinfo != null) {
                    String read = rtinfo + crowdinfo;
                    ttsManager.initQueue(read);
                }
            }
        }, 5000);
    }


    @Override
    public String toString() {
        return "BusInformation{" +
                "mClosestBeacon=" + mClosestBeacon +
                ", mClosestBeaconDistance=" + mClosestBeaconDistance +
                ", mBeaconManager=" + mBeaconManager +
                ", mCurrentDestinationBusStop=" + mCurrentDestinationBusStop +
                ", pitch=" + pitch +
                ", speed=" + speed +
                ", cIndex=" + cIndex +
                ", userDestinationBus=" + userDestinationBus +
                ", mClosestBusStopResponseHandler=" + mClosestBusStopResponseHandler +
                ", mPathResponseHandler=" + mPathResponseHandler +
                '}';
    }

    public void bus3Action() {
        if (mClosestBeacon == 0) {
            stepsText.setText("No navigation Available for the bus stop");
            return;
        }
        mCurrentDestinationBusStop = userDestinationBus;
        new ClosestBusStopRequest(mClosestBeacon.toString(), mClosestBusStopResponseHandler).execute();
        list=datePicker();
        String month =String.valueOf(list.get(0));
        String weekday= String.valueOf(list.get(1));
        String hour=String.valueOf(list.get(2));
        String busNumber=String.valueOf(userDestinationBus);
        String tripRoute="";
        if(userDestinationBus == 55) {
            tripRoute = "busStop-55";
        }else if(userDestinationBus == 60){
            tripRoute ="busStop-60";
        }else if(userDestinationBus == 181){
            tripRoute="busStop-181";
        }
        new CrowdEstimatorRequest(mCrowdEstimatorHandler,month,weekday,hour,tripRoute, busNumber).execute();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("Route for Bus Number" + userDestinationBus);

            }
        }, 1000);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if(rtinfo != null && crowdinfo != null) {
                    String read = rtinfo + crowdinfo;
                    ttsManager.initQueue(read);
                }
            }
        }, 5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }

    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d("RangingActivity", "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");
                String instanceHexValue = instanceId.toHexString();
                if (beacon.getDistance() < mClosestBeaconDistance) {
                    mClosestBeacon = Integer.parseInt(instanceHexValue.substring(instanceHexValue.length() - 4, instanceHexValue.length()), 16);
                    mClosestBeaconDistance = beacon.getDistance();
                    mBeaconManager.setRangeNotifier(null);
                }
            }
        }

        System.out.println("mClosest Beacon : " + mClosestBeacon);
        if (mClosestBeacon == 13){
            bus2Action();
        }
        else if (mClosestBeacon == 81){
            bus1Action();
        }
        else if(mClosestBeacon == 101){
            bus3Action();
        }
        else{
            System.out.println("something is wrong");
            noBeaconDetected = true;
            //stepsText.setText(" No Beacon Detected");
            //ttsManager.initQueue("No Beacons Detected");
        }

    }

    public ArrayList datePicker() {
        ArrayList<Integer> dateList = new ArrayList();
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int month = cal.get(Calendar.MONTH);
        int weekDay= cal.get(Calendar.DAY_OF_WEEK);
        int time=cal.get(Calendar.HOUR_OF_DAY);
        dateList.add(0,month);
        dateList.add(1,weekDay);
        dateList.add(2,time);
        return dateList;
    }
}

