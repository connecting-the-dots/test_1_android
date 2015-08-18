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
public class MonthBlock extends TimeBlock {
    protected int month; // 0~11
    protected YearBlock prev; // the year this month belongs to
    protected static JSONArray[] monthStatistics = new JSONArray[12]; // sumTime of the specific day
    protected static boolean[] statisticsSorted = new boolean[12];
    static {
        for(int i = 0; i < 12; i++){
            monthStatistics[i] = new JSONArray();
            statisticsSorted[i] = true;
        }
    }

    public MonthBlock() {
        period = "year";
        long temp = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;

        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (temp / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), 1);

        startTime = rightNow.getTimeInMillis();
        month = rightNow.get(rightNow.MONTH);
    }
    public MonthBlock(long date, YearBlock _prev) {
        period = "year";
        prev = _prev;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (date / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), 1);

        startTime = rightNow.getTimeInMillis();
        month = rightNow.get(rightNow.MONTH);

        for(int i = 0; i < TimeBlock.daysInMonth(month, rightNow.get(rightNow.YEAR)); i++){
            long tempDate = startTime;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
            cal.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), i + 1);

            subBlocks.add(new DayBlock(cal.getTimeInMillis(), this));
        }

    }
    public int getMonth(){
        return month;
    }
    public void setMonth(int _month) {
        month = _month;
    }
    public YearBlock getPrev(){
        return prev;
    }

    public void addApps(JSONObject apps, int _day, int _hour) throws JSONException {
        subBlocks.get(_day).addApps(apps, _hour);

        Iterator<String> keys= apps.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            long value = apps.getLong(key);
            addAppsLength( key, value);
            addStatisticLength( key, value);
        }
    }
    public static long getStatisticsLength(String appName, int _month) throws JSONException {
        for(int i = 0; i < monthStatistics[_month].length(); i++){
            if(appName.contentEquals(monthStatistics[_month].getJSONObject(i).getString("appName"))){
                return monthStatistics[_month].getJSONObject(i).getLong("sumTime");
            }
        }
        return 0;
    }
    public static JSONArray getStatistics(int _month) throws JSONException {
        if(!statisticsSorted[_month]) sortStatistics(_month);
        return monthStatistics[_month];
    }
    public void addStatisticLength(String appName, long length) throws JSONException {
        for(int i = 0; i < monthStatistics[month].length(); i++){
            if(appName.contentEquals(monthStatistics[month].getJSONObject(i).getString("appName"))){
                monthStatistics[month].getJSONObject(i).put(appName, length + monthStatistics[month].getJSONObject(i).getLong("sumTime"));
                statisticsSorted[month] = false;
                return;
            }
        }
    }
    public static void sortStatistics(int _month) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < monthStatistics[_month].length(); i++) {
            JSONObject clone = new JSONObject(monthStatistics[_month].getJSONObject(i).toString());
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
        int len = monthStatistics[_month].length();
        monthStatistics[_month] = new JSONArray();
        for (int i = 0; i < len; i++) {
            monthStatistics[_month].put(jsonValues.get(i));
        }
        statisticsSorted[_month] = true;
    }
}
