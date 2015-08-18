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
public class DayBlock extends TimeBlock{
    protected int day; // 1~?
    protected MonthBlock prev; // the month this day belongs to
    protected static JSONArray[] dayStatistics = new JSONArray[31]; // sumTime of the specific day
    protected static boolean[] statisticsSorted = new boolean[31];
    static {
        for(int i = 0; i < 31; i++){
            dayStatistics[i] = new JSONArray();
            statisticsSorted[i] = true;
        }
    }

    public DayBlock() {
        period = "day";
        long temp = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;

        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (temp / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH));

        startTime = rightNow.getTimeInMillis();
        day = rightNow.get(rightNow.DAY_OF_MONTH);
    }
    public DayBlock(long date, MonthBlock _prev) {
        period = "day";
        prev = _prev;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (date / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH));

        startTime = rightNow.getTimeInMillis();
        day = rightNow.get(rightNow.DAY_OF_MONTH);

        for(int i = 0; i < 30; i++){
            long tempDate = startTime;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
            cal.set(rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH), i + 1, 0);

            subBlocks.add(new HourBlock(cal.getTimeInMillis(), this));
        }

    }
    public int getDay(){
        return day;
    }
    public void setDay(int _day) { day = _day; }
    public MonthBlock getPrev(){
        return prev;
    }

    public void addApps(JSONObject apps, int _hour) throws JSONException {
        subBlocks.get(_hour).addApps(apps);

        Iterator<String> keys= apps.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            long value = apps.getLong(key);
            addAppsLength( key, value);
            addStatisticLength( key, value);
        }
    }

    public static long getStatisticsLength(String appName, int _day) throws JSONException {
        for(int i = 0; i < dayStatistics[_day].length(); i++){
            if(appName.contentEquals(dayStatistics[_day].getJSONObject(i).getString("appName"))){
                return dayStatistics[_day].getJSONObject(i).getLong("sumTime");
            }
        }
        return 0;
    }
    public static JSONArray getStatistics(int _day) throws JSONException {
        if(!statisticsSorted[_day]) sortStatistics(_day);
        return dayStatistics[_day];
    }
    public void addStatisticLength(String appName, long length) throws JSONException {
        for(int i = 0; i < dayStatistics[day].length(); i++){
            if(appName.contentEquals(dayStatistics[day].getJSONObject(i).getString("appName"))){
                dayStatistics[day].getJSONObject(i).put(appName, length + dayStatistics[day].getJSONObject(i).getLong("sumTime"));
                statisticsSorted[day] = false;
                return;
            }
        }
    }
    public static void sortStatistics(int _day) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < dayStatistics[_day].length(); i++) {
            JSONObject clone = new JSONObject(dayStatistics[_day].getJSONObject(i).toString());
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
        int len = dayStatistics[_day].length();
        dayStatistics[_day] = new JSONArray();
        for (int i = 0; i < len; i++) {
            dayStatistics[_day].put(jsonValues.get(i));
        }
        statisticsSorted[_day] = true;
    }

}
