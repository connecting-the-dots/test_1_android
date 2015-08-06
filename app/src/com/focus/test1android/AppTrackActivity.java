package com.focus.test1android;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
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
public class AppTrackActivity extends Activity {

    private Button start_button;
    private Button stop_button;
    public static JSONArray mySortedArray = new JSONArray();
    public static final String TAG = "AppTrackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apptrack);
        start_button = (Button) findViewById(R.id.start_button);
        stop_button = (Button) findViewById(R.id.stop_button);

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
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

                    ParseObject currentBlock = new ParseObject("currentBlock");
                    currentBlock.put("jsonObject", hourBlock);
                    currentBlock.put("startHour", hourBlock.getLong("startHour"));
                    currentBlock.put("outerArray", hourBlock.getJSONArray("outerArray"));

                    currentBlock.saveInBackground();

                    String jsonInString = hourBlock.toString();
                    JSONObject transBlock = new JSONObject(jsonInString);

                    ParseObject TransBlock = new ParseObject("transBlock");
                    TransBlock.put("jsonObject", transBlock);
                    TransBlock.put("startHour", transBlock.getLong("startHour"));
                    TransBlock.put("outerArray", transBlock.getJSONArray("outerArray"));

                    TransBlock.saveInBackground();
                } catch (JSONException e) {
                    e.printStackTrace();
                }



                try {
                    JSONArray hourBlocks = Test1Android.itemDAO.getAll();

                    myHourBlock.put("blocks", hourBlocks.length());
                    for(int i = 0; i < hourBlocks.length(); i++){
                        ParseObject tempHourBlock = new ParseObject("databaseTest");
                        tempHourBlock.put("jsonObject", hourBlocks.getJSONObject(i));
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
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "sumTime";

            @Override
            public int compare(JSONObject a, JSONObject b) {

                long valA = -1;
                long valB = -1;
                try {
                    valA =  a.getLong(KEY_NAME);
                    valB =  b.getLong(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                if(valA > valB) {
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
}
