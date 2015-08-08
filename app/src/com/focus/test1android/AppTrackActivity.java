package com.focus.test1android;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.CountDownTimer;

import com.parse.ParseObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.Text;
import com.parse.ParseUser;
import com.parse.ParseRelation;

import java.util.Date;

/**
 * Created by XNS on 2015/7/22.
 */
public class AppTrackActivity extends Activity{

    private Button start_button;
    private Button stop_button;
    public static JSONArray mySortedArray = new JSONArray();
    public static final String TAG = "AppTrackActivity";
    private int FM_NOTIFICATION_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apptrack);
        start_button = (Button) findViewById(R.id.start_button);
        stop_button = (Button) findViewById(R.id.stop_button);

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        registerReceiver(br, new IntentFilter(TrackAccessibilityService.COUNTDOWN_BR));
    }

    public void onStartClick(View view) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(Test1Android.TAG, "error");
        }
        start_button.setClickable(false);
        stop_button.setClickable(true);
        final TextView service_state = (TextView) findViewById(R.id.service_state);
//        service_state.setText("Service On");
        startCountDown(service_state);
        service_state.setBackgroundColor(getResources().getColor(R.color.on_background));

    }

    public void startCountDown(final TextView service_state) {
        final long startTime = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;
        final Date date = new Date(startTime);
        TrackAccessibilityService.blockStart = startTime;
        TrackAccessibilityService.startHour = 3600000 * (startTime / 3600000);
        try {
            TrackAccessibilityService.stopActivity();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
        while(TrackAccessibilityService.outerArray.length() != 0){
            TrackAccessibilityService.outerArray.remove(0);
        }
        while(mySortedArray.length() != 0){
            mySortedArray.remove(0);
        }
        */
        TrackAccessibilityService.outerArray = new JSONArray();
        mySortedArray = new JSONArray();

        CountDownTimer myTimer =  new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                service_state.setText("remaining: " + millisUntilFinished / 1000 + "sec");
            }
            public void onFinish() {
                service_state.setText("done!");
                try {
                    TrackAccessibilityService.stopActivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    sortJSONArray();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ParseObject myHourBlock = new ParseObject("AppHourBlock");


                myHourBlock.put("date", new Date(TrackAccessibilityService.startHour));
                myHourBlock.put("start", new Date(TrackAccessibilityService.blockStart));
                myHourBlock.put("user", ParseUser.getCurrentUser());
                //myHourBlock.put("date", date);


                int len = mySortedArray.length();
                for(int i = len - 1; i >= 0; i--) {
                    try {
                        Log.d(TAG, mySortedArray.get(i).toString());

                        ParseObject myAppActivity = new ParseObject("AppActivity");
                        myAppActivity.put("packageName", mySortedArray.getJSONObject(i).getString("packageName"));
                        myAppActivity.put("appName", mySortedArray.getJSONObject(i).getString("appName"));
                        myAppActivity.put("activities", mySortedArray.getJSONObject(i).getJSONArray("activities"));
                        myAppActivity.put("sumTime", mySortedArray.getJSONObject(i).getLong("sumTime"));
//                        ParseRelation<ParseObject> timeRelation = myAppActivity.getRelation("bbb");
//                        timeRelation.add(myAppActivity);
                        myAppActivity.saveInBackground();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject hourBlock = new JSONObject();
                try {
                    hourBlock.put("startHour", TrackAccessibilityService.startHour);
                    hourBlock.put("outerArray", mySortedArray);

                    long id = Test1Android.itemDAO.insert(hourBlock);

                    Test1Android.hourBlockId.put(date.toString(), id);
                    /*
                    ParseObject currentBlock = new ParseObject("currentBlock");
                    currentBlock.put("jsonObject", hourBlock);
                    currentBlock.put("startHour", hourBlock.getLong("startHour"));
                    currentBlock.put("outerArray", hourBlock.getJSONArray("outerArray"));

                    currentBlock.saveInBackground();
                    */
                } catch (JSONException e) {
                    e.printStackTrace();
                }



                try {
                    /*
                    JSONArray hourBlocks = Test1Android.itemDAO.getAll();

                    myHourBlock.put("blocks", hourBlocks.length());
                    for(int i = 0; i < hourBlocks.length(); i++){
                        ParseObject tempHourBlock = new ParseObject("databaseTest");
                        tempHourBlock.put("jsonObject", hourBlocks.getJSONObject(i));
                        tempHourBlock.saveInBackground();
                    }
                    */

                    List<String> hourBlocks = Test1Android.itemDAO.getAll();
                    for(int i = 0; i < hourBlocks.size(); i++){
                        ParseObject tempHourBlock = new ParseObject("databaseBlocks");
                        JSONObject jsonObjectTest = new JSONObject(hourBlocks.get(i));

                        tempHourBlock.put("jsonObject", jsonObjectTest);
                        tempHourBlock.put("outerArray", jsonObjectTest.getJSONArray("outerArray"));
                        tempHourBlock.put("user", ParseUser.getCurrentUser());
                        long temp = (long)jsonObjectTest.getLong("startHour");
                        tempHourBlock.put("startHourDate", new Date(temp));

                        tempHourBlock.saveInBackground();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                ParseRelation<ParseObject> relation = myHourBlock.getRelation("aaa");
//                relation.add(user);
                myHourBlock.saveInBackground();

            }
        }.start();
    }

    public void sortJSONArray() throws JSONException {

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < TrackAccessibilityService.outerArray.length(); i++) {
            JSONObject clone = new JSONObject(TrackAccessibilityService.outerArray.getJSONObject(i).toString());
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

                if (valA > valB) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        int len = TrackAccessibilityService.outerArray.length();
        for (int i = 0; i < len; i++) {
            mySortedArray.put(jsonValues.get(i));
//            TrackAccessibilityService.outerArray.remove(len - 1 - i);
        }
    }
    public void onReportClick(View view) {
        Intent it = new Intent(this, ReportActivity.class);
        startActivity(it);
    }
    public void onFriendClick(View view) {
        Intent it = new Intent(this, AddFriendActivity.class);
        startActivity(it);
    }
    public void onPushClick(View view) {
        Intent it = new Intent(this, PushActivity.class);
        startActivity(it);
    }
    public void onStopClick(View view) {
        Intent it = new Intent(this, TrackAccessibilityService.class);
        stopService(it);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(Test1Android.TAG, "error");
        }
        start_button.setClickable(true);
        stop_button.setClickable(false);
        TextView service_state = (TextView) findViewById(R.id.service_state);
        service_state.setText("Service Off");
        service_state.setBackgroundColor(getResources().getColor(R.color.off_background));
    }
    @Override
    public void onResume() {
        super.onResume();
        unregisterReceiver(br);
        Log.i(TAG, "Unregistered broadcast receiver");
    }

    @Override
    public void onPause() {
        super.onPause();
        registerReceiver(br, new IntentFilter(TrackAccessibilityService.COUNTDOWN_BR));
        Log.i(TAG, "Registered broadcast receiver");
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendKickNotification(intent); // or whatever method used to update your GUI fields
        }
    };
    private void sendKickNotification(Intent intent) {
//        if (intent.getExtras() != null) {
//            long millisUntilFinished = intent.getLongExtra("countdown", 0);
//            Log.v(TAG, "Countdown seconds remaining: " +  millisUntilFinished);
//
//        } else {
        Log.v(TAG, "KICK!");
        addNotification();
    }
    private void addNotification() {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notifications Example")
                        .setContentText("You receive Kick :-)");

        Intent notificationIntent = new Intent(this, AppTrackActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(FM_NOTIFICATION_ID, builder.build());
        FM_NOTIFICATION_ID++;
    }
}
