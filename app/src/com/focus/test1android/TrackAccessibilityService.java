package com.focus.test1android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.facebook.AccessToken;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by XNS on 2015/7/23.
 */
public class TrackAccessibilityService extends AccessibilityService {

//    public static JSONArray outerArray = new JSONArray();

    public static String currentPackageName = "";
    public static long startTime = 0;
    public static long endTime = 0;
    public static long duration = 0;
    public static boolean ignoring = false;
    public static final String TAG = "TrackAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.v(TAG, "***** onAccessibilityEvent");

            String tempPackageName = event.getPackageName().toString();

            if(currentPackageName.contentEquals("")) { // first time
                currentPackageName = tempPackageName;
                startTime = System.currentTimeMillis();
                return;
            }
            if (tempPackageName.contentEquals("com.android.systemui") ||
                    tempPackageName.contentEquals("com.asus.launcher")) {

                if (ignoring) {
                    return;
                }
                endTime = System.currentTimeMillis();
                ignoring = true;
            }
            else {
                if (ignoring) {
                    ignoring = false;
                } else {
                    if (tempPackageName.contentEquals(currentPackageName)) {
                        return;
                    }
                    endTime = System.currentTimeMillis();
                }

                duration = endTime - startTime;
                startTime = System.currentTimeMillis();
//                Log.v(TAG, "PackageName: " + currentPackageName + ", duration: " + (duration / 1000));
//                storeAppActivity();
                currentPackageName = event.getPackageName().toString();
            }
        }
//    }
//    void storeAppActivity() {
//        int i = 0;
//
//        if(outerArray.length() == 0) {
//            JSONObject myAppActivity = new JSONObject();
//            try {
//                myAppActivity.put("packageName", currentPackageName);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            myAppActivity.put("", );
//
//        }
//    }


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
