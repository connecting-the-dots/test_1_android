package com.focus.test1android.TimeBlocks;

import com.focus.test1android.TimeBlock;
import com.focus.test1android.TrackAccessibilityService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Harvey Yang on 2015/8/17.
 */
public class HourBlock extends TimeBlock {
    protected int hour; // 0~23
    protected DayBlock prev; // the day this hour belongs to
    protected static JSONArray[] hourStatistics = new JSONArray[24]; // sumTime of the specific hour
    protected static boolean[] statisticsSorted = new boolean[24];
    static {
        for(int i = 0; i < 24; i++){
            hourStatistics[i] = new JSONArray();
            statisticsSorted[i] = true;
        }
    }

    public HourBlock() {
        period = "hour";
        long temp = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;

        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (temp / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH), rightNow.get(rightNow.HOUR_OF_DAY), 0);

        startTime = rightNow.getTimeInMillis();
        hour = rightNow.get(rightNow.DAY_OF_MONTH);
    }
    public HourBlock(long date, DayBlock _prev) {
        period = "hour";
        prev = _prev;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (date / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH), rightNow.get(rightNow.HOUR_OF_DAY), 0);

        startTime = rightNow.getTimeInMillis();
        hour = rightNow.get(rightNow.DAY_OF_MONTH);

    }
    public int getHour() { return hour; }
    public void setHour(int _hour) { hour = _hour; }
    public DayBlock getPrev(){
        return prev;
    }
    public void addApps(JSONObject apps) throws JSONException {
        Iterator<String> keys= apps.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            long value = apps.getLong(key);
            addAppsLength( key, value);
            addStatisticLength( key, value);
        }
    }
    public static long getStatisticsLength(String appName, int _hour) throws JSONException {
        for(int i = 0; i < hourStatistics[_hour].length(); i++){
            if(appName.contentEquals(hourStatistics[_hour].getJSONObject(i).getString("appName"))){
                return hourStatistics[_hour].getJSONObject(i).getLong("sumTime");
            }
        }
        return 0;
    }
    public static JSONArray getStatistics(int _hour) throws JSONException {
        if(!statisticsSorted[_hour]) sortStatistics(_hour);
        return hourStatistics[_hour];
    }
    public void addStatisticLength(String appName, long length) throws JSONException {
        for(int i = 0; i < hourStatistics[hour].length(); i++){
            if(appName.contentEquals(hourStatistics[hour].getJSONObject(i).getString("appName"))){
                hourStatistics[hour].getJSONObject(i).put(appName, length + hourStatistics[hour].getJSONObject(i).getLong("sumTime"));
                statisticsSorted[hour] = false;
                return;
            }
        }

    }
    public static void sortStatistics(int _hour) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < hourStatistics[_hour].length(); i++) {
            JSONObject clone = new JSONObject(hourStatistics[_hour].getJSONObject(i).toString());
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

                if (valA > valB) return 1;
                else return -1;
            }
        });
        int len = hourStatistics[_hour].length();
        hourStatistics[_hour] = new JSONArray();
        for (int i = 0; i < len; i++) {
            hourStatistics[_hour].put(jsonValues.get(i));
        }
        statisticsSorted[_hour] = true;
    }
}
