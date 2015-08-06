package com.focus.test1android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.facebook.AccessToken;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


/**
 * Created by XNS on 2015/7/23.
 */
public class TrackAccessibilityService extends AccessibilityService {

    public static JSONArray outerArray = new JSONArray();

    public static String currentPackageName = "";
    public static long blockStart = 0;
    public static long startHour = 0;
    public static long startTime = 0;
    public static long endTime = 0;
    public static long duration = 0;
    public static boolean ignoring = false;
    public static long deltaTime = 8*3600*1000;
    public static final String TAG = "TrackAccessibilityService";



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.v(TAG, "***** onAccessibilityEvent");

            String tempPackageName = event.getPackageName().toString();

            if(currentPackageName.contentEquals("")) { // first time
                currentPackageName = tempPackageName;
                startTime = System.currentTimeMillis() + deltaTime;

                return;
            }
            if (tempPackageName.contentEquals("com.android.systemui") ||
                    tempPackageName.contentEquals("com.asus.launcher")) {

                if (ignoring) {
                    return;
                }
                endTime = System.currentTimeMillis() + deltaTime;
                ignoring = true;
            }
            else {
                if (ignoring) {
                    ignoring = false;
                } else {
                    if (tempPackageName.contentEquals(currentPackageName)) {
                        return;
                    }
                    endTime = System.currentTimeMillis() + deltaTime;
                }

                duration = endTime - startTime;
                try {
                    storeAppInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startTime = System.currentTimeMillis() + deltaTime;
                Log.v(TAG, "PackageName: " + currentPackageName + ", duration: " + (duration / 1000));
                currentPackageName = event.getPackageName().toString();
            }
        }
    }
    public static void stopActivity() throws JSONException {
        if(!ignoring){
            endTime = System.currentTimeMillis() + deltaTime;
            duration = endTime - startTime;
            try {
                storeAppInfo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startTime = System.currentTimeMillis() + deltaTime;
        }

    }

    static void storeAppInfo() throws JSONException {

        if(outerArray.length() == 0)
        {
            JSONObject myObject = new JSONObject();

            myObject.put("appName", getAppName());
            myObject.put("packageName", currentPackageName);
            storeAppActivity( myObject , true );
            outerArray.put(myObject);
        } else {
            boolean is_new = true;
            int index = 0;
            for(int i = 0; i < outerArray.length(); i++) {

                JSONObject currentJSONObject = outerArray.getJSONObject(i);
                if(currentJSONObject.getString("packageName").contentEquals(currentPackageName)) {
                    is_new = false;
                    index = i;
                }
            }
            if(is_new) {
                JSONObject myObject = new JSONObject();

                myObject.put("appName", currentPackageName);
                myObject.put("packageName", currentPackageName);
                storeAppActivity( myObject , true );
                outerArray.put(myObject);
            } else {
                storeAppActivity(outerArray.getJSONObject(index) , false);
            }
        }
    }
    static String getAppName() {

        return currentPackageName;
    }
    static void storeAppActivity(JSONObject myObject, boolean is_new) throws JSONException {

        JSONArray innerArray = new JSONArray();
        JSONObject innerObject = new JSONObject();

        innerObject.put("startTime", startTime);
        innerObject.put("duration", duration);

        if(is_new) {
            try {
                myObject.put("activities", innerArray);
                myObject.put("sumTime", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            myObject.getJSONArray("activities").put(innerObject);
            long tmp = myObject.getLong("sumTime");
            myObject.put("sumTime", tmp + duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInterrupt()
    {
        Log.v(TAG, "***** onInterrupt");
    }

    @Override
    public void onServiceConnected()
    {
        Log.v(TAG, "***** onServiceConnected");

        super.onServiceConnected();
        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }
}
