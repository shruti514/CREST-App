package com.preethi.android.crustappandroid;

/**
 * Created by Arun on 10/14/16.
 */

public class CrowdEstimatorRequest extends APIRequest {
    private APIResponseHandler mResponseHandler;
    private String busNumber;
    private String tripMonth;
    private String tripService;
    private String timeBucket;
    private String tripRoute;

    public CrowdEstimatorRequest(APIResponseHandler mResponseHandler, String tripMonth, String tripService, String timeBucket, String tripRoute, String busNumber) {
        this.mResponseHandler = mResponseHandler;
        this.busNumber = busNumber;
        this.tripMonth = tripMonth;
        this.tripService = tripService;
        this.timeBucket = timeBucket;
        this.tripRoute = tripRoute;
    }

    @Override
    protected String getUrl() {

        System.out.println("http://ec2-54-84-146-121.compute-1.amazonaws.com:8181/stops/crowdEstimator/"+tripMonth+"/"+tripService+"/"+timeBucket+"/"+tripRoute+"/"+busNumber );
        return "http://ec2-54-84-146-121.compute-1.amazonaws.com:8181/stops/crowdEstimator/"+tripMonth+"/"+tripService+"/"+timeBucket+"/"+tripRoute+"/"+busNumber ;
    }

    @Override
    protected APIResponseHandler getResponseHandler() {
        return mResponseHandler;
    }
}
