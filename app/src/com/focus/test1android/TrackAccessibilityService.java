package com.focus.test1android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.facebook.AccessToken;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by XNS on 2015/7/23.
 */
public class TrackAccessibilityService extends AccessibilityService {

    public static JSONArray outerArray = new JSONArray();

    public static String currentPackageName = "";
    public static String tempPackageName = "";
    public static long blockStart = 0;
    public static long startHour = 0;
    public static long startTime = 0;
    public static long endTime = 0;
    public static long duration = 0;
    public static boolean ignoring = false;
    public static long deltaTime = 8 * 3600 * 1000;
    public static long anHour = 60000;

    public static final String TAG = "MyService";

    public static final String COUNTDOWN_BR = "com.focus.test1android.countdown_br";
    public static Intent bi = new Intent(COUNTDOWN_BR);

    public static CountDownTimer cdt = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.v(TAG, "***** onAccessibilityEvent");

            tempPackageName = event.getPackageName().toString();
            // if typeOne is enabled
            String target = "com.facebook.katana";
            kickDetectTypeOne(target);
            // kickDetectTypeTwo(tempPackageName);
            // kickDetectTypeThree(tempPackageName);

            Thread thread = new Thread() {
                @Override
                public void run() {
                    checkWindowState();
                }
            };

            thread.start();
        }
    }

    public static void checkWindowState() {
        if (currentPackageName.contentEquals("")) {
            currentPackageName = tempPackageName;
            startTime = System.currentTimeMillis() + deltaTime;

            startHour = anHour * (startTime / anHour);

            return;
        }
        if ( System.currentTimeMillis() + deltaTime > startHour + anHour ) {

            final long currentTime = System.currentTimeMillis() + deltaTime;
            endTime = startHour + anHour;


            while(endTime <= currentTime){
                duration = endTime - startTime;
                try {
                    if (!ignoring) storeAppInfo();
                    sortJSONArray();
                    storeInDatabase();
                    Log.v(TAG, "updating");
                    update();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startTime = endTime;
                startHour = anHour * (startTime / anHour);
                endTime += anHour;
                outerArray = new JSONArray();
            }
            startHour = anHour * ( currentTime / anHour );
            endTime = currentTime;
            startTime = startHour;
        }

        if ( !tempPackageName.contentEquals(currentPackageName) ){
            if (ignoring){
                if (!tempPackageName.contentEquals("com.android.systemui") &&
                            !tempPackageName.contentEquals("com.asus.launcher")) {
                    startTime = System.currentTimeMillis() + deltaTime;
                    endTime = startTime;
                    currentPackageName = tempPackageName;
                    ignoring = false;
                }
            } else {
                endTime = System.currentTimeMillis() + deltaTime;
                duration = endTime - startTime;
                try {
                    storeAppInfo();
                    Log.v(TAG, "PackageName: " + currentPackageName + ", duration: " + (duration / 1000));
                    startTime = endTime;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (tempPackageName.contentEquals("com.android.systemui") ||
                            tempPackageName.contentEquals("com.asus.launcher")){
                    ignoring = true;
                } else  currentPackageName = tempPackageName;
            }
        }
        /*
        if (tempPackageName.contentEquals("com.android.systemui") ||
                    tempPackageName.contentEquals("com.asus.launcher")) {

            if (ignoring) {
                return;
            }
            endTime = System.currentTimeMillis() + deltaTime;
            ignoring = true;
        } else {
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
        */
    }

    public void kickDetectTypeOne(String target) {

        if (tempPackageName.contentEquals(target)) {
            Log.v(TAG, "Starting timer...");
            cdt = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    Log.v(TAG, "Countdown seconds remaining: " + millisUntilFinished);
                    bi.putExtra("countdown", millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    Log.v(TAG, "Timer finished");
                    sendBroadcast(bi);
                }
            }.start();
        } else {

            if (cdt != null) // prevent first time exception
                cdt.cancel();
        }
    }

    public static void stopCheckingWindowState() throws JSONException {
        if (!ignoring) {
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

        if (outerArray.length() == 0) {
            JSONObject myObject = new JSONObject();

            myObject.put("appName", getAppName());
            myObject.put("packageName", currentPackageName);
            storeAppActivity(myObject, true);
            outerArray.put(myObject);
        } else {
            boolean is_new = true;
            int index = 0;
            for (int i = 0; i < outerArray.length(); i++) {

                JSONObject currentJSONObject = outerArray.getJSONObject(i);
                if (currentJSONObject.getString("packageName").contentEquals(currentPackageName)) {
                    is_new = false;
                    index = i;
                }
            }
            if (is_new) {
                JSONObject myObject = new JSONObject();

                myObject.put("appName", currentPackageName);
                myObject.put("packageName", currentPackageName);
                storeAppActivity(myObject, true);
                outerArray.put(myObject);
            } else {
                storeAppActivity(outerArray.getJSONObject(index), false);
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

        if (is_new) {
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
    public void onInterrupt() {
        Log.v(TAG, "***** onInterrupt");
    }

    @Override
    public void onServiceConnected() {
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
    public static void storeInDatabase(){
        JSONObject hourBlock = new JSONObject();
        try {

            hourBlock.put("startHour", startHour);
            hourBlock.put("outerArray", outerArray);
            Log.v(TAG, "Byte: " + hourBlock.toString().getBytes().length);
            long id = Test1Android.itemDAO.insert(hourBlock);
            Test1Android.hourBlockId.put((new Date(startHour)).toString(), id);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public static void sortJSONArray() throws JSONException {

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < outerArray.length(); i++) {
            JSONObject clone =
                    new JSONObject(outerArray.getJSONObject(i).toString());
            jsonValues.add(clone);
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {

            private static final String KEY_NAME = "sumTime";

            @Override
            public int compare(JSONObject a, JSONObject b) {

                long valA = -1;
                long valB = -1;
                try {
                    valA = a.getLong(KEY_NAME);
                    valB = b.getLong(KEY_NAME);
                } catch (JSONException e) {
                    //do something
                }

                if (valA > valB)    return 1;
                else  return -1;
            }
        });
        int len = outerArray.length();
        outerArray = new JSONArray();
        for (int i = 0; i < len; i++) {
            outerArray.put(jsonValues.get(i));
            // TrackAccessibilityService.outerArray.remove(len - 1 - i);
        }
    }

    public static void update(){
        try {
            List<String> blocks = Test1Android.itemDAO.getAll();

            String hourInString = Test1Android.itemDAO.get(Test1Android.hourBlockId.getLong((new Date(startHour).toString())));
            ParseObject tempHourBlock = new ParseObject("ForTest");
            JSONObject jsonObjectHour = new JSONObject(hourInString);

            tempHourBlock.put("AppHourBlock", jsonObjectHour.getJSONArray("outerArray"));
            tempHourBlock.put("User", ParseUser.getCurrentUser());

            long temp = (long) jsonObjectHour.getLong("startHour");
            tempHourBlock.put("StartHourDate", new Date(temp));

            tempHourBlock.saveEventually();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void checkOverTime(){
        long sumOfTime = 0;
        for(int i = 0; i < outerArray.length(); i++) {
            try {
                sumOfTime += outerArray.getJSONObject(i).getLong("sumTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(sumOfTime >= 20*60*1000){ // 20 min in an hour
            Log.v(TAG, "Using Apps OverTime.");
        }
    }
}

