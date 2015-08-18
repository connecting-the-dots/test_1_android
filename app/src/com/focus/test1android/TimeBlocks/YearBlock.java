package com.focus.test1android.TimeBlocks;

import com.focus.test1android.TimeBlock;
import com.focus.test1android.TrackAccessibilityService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Harvey Yang on 2015/8/17.
 */
public class YearBlock extends TimeBlock {
    protected int year;
    public YearBlock() {
        period = "year";
        long temp = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (temp / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), 0, 1);

        startTime = rightNow.getTimeInMillis();
        year = rightNow.get(rightNow.YEAR);
    }
    public YearBlock(long date) {
        period = "year";
        long temp = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(1000 * (temp / 1000));
        rightNow.set(rightNow.get(rightNow.YEAR), 0, 1);

        startTime = rightNow.getTimeInMillis();
        year = rightNow.get(rightNow.YEAR);

        for(int i = 0; i < 12; i++){
            long tempDate = startTime;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
            cal.set(rightNow.get(rightNow.YEAR), i, 1);

            subBlocks.add(new MonthBlock(cal.getTimeInMillis(), this));
        }

    }
    public int getYear(){
        return year;
    }
    public void setYear(int _year) {
        year = _year;
    }

    public void addApps(JSONObject apps, int _month, int _day, int _hour) throws JSONException {
        subBlocks.get(_month).addApps(apps, _day, _hour);

        Iterator<String> keys= apps.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            long value = apps.getLong(key);
            addAppsLength( key, value);
        }
    }
}
