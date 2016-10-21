package com.preethi.android.crustappandroid;

import android.util.Log;

/**
 * Created by preethi on 10/14/16.
 */

public class PathRequest extends APIRequest {
    private String mBusStop1;
    private String mBusStop2;
    private APIResponseHandler mHandler;

    public PathRequest(String busStop1, String busStop2, APIResponseHandler handler) {
        mBusStop1 = busStop1;
        mBusStop2 = busStop2;
        mHandler = handler;
    }

    @Override
    protected String getUrl() {
        Log.d("app",mBusStop1 +"----"+mBusStop2);
         return "http://ec2-54-84-146-121.compute-1.amazonaws.com:8181/stops/station/" + mBusStop1 + "/" + mBusStop2;
        //return "http://ec2-54-84-146-121.compute-1.amazonaws.com:8080/beacons/crust/stops/55/60";
    }

    @Override
    protected APIResponseHandler getResponseHandler() {
        return mHandler;
    }
}
