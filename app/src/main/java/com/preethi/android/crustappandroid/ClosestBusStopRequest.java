package com.preethi.android.crustappandroid;


public class ClosestBusStopRequest extends APIRequest {
    private String mBeaconId;
    private APIResponseHandler mResponseHandler;

    public ClosestBusStopRequest(String beaconId, APIResponseHandler apiResponseHandler) {
        mBeaconId = beaconId;
        mResponseHandler = apiResponseHandler;
    }

    @Override
    protected String getUrl() {
         return "http://ec2-54-84-146-121.compute-1.amazonaws.com:8181/stops/station/" + mBeaconId;

    }

    @Override
    protected APIResponseHandler getResponseHandler() {
        return mResponseHandler;
    }
}
