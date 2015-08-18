package com.focus.test1android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Harvey Yang on 2015/8/17.
 */
public class TimeBlock {
    protected String period;
    protected long startTime;
    protected List<TimeBlock> subBlocks = new ArrayList<TimeBlock>();
    protected JSONArray appsLength = new JSONArray(); // "appName", "sumTime"
    protected boolean sorted = false;

    public TimeBlock(){
        period = null;
        startTime = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;
    }

    public String getPeriod(){
        return period;
    }
    public long getStartTIme(){
        return startTime;
    }
    public TimeBlock getSubTimeBlock(int i) { //start from zero
        if (i < subBlocks.size())   return subBlocks.get(i);
        return null;
    }
    public long getAppsLength(String appName) throws JSONException {
        for(int i = 0; i < appsLength.length(); i++){
            if(appName.contentEquals(appsLength.getJSONObject(i).getString("appName"))){
                return appsLength.getJSONObject(i).getLong("sumTime");
            }
        }
        return 0;
    }
    public JSONArray getArray() throws JSONException {
        if(!sorted) sortApps();
        return appsLength;
    }
    public void addApps(JSONObject apps) throws JSONException {}
    public void addApps(JSONObject apps, int _hour) throws JSONException {}
    public void addApps(JSONObject apps, int _day, int _hour) throws JSONException {}
    public void addApps(JSONObject apps, int _month, int _day, int _hour) throws JSONException {}
    public void addAppsLength(String appName, long length) throws JSONException {
        for(int i = 0; i < appsLength.length(); i++){
            if(appName.contentEquals(appsLength.getJSONObject(i).getString("appName"))){
                appsLength.getJSONObject(i).put(appName, length + appsLength.getJSONObject(i).getLong("sumTime"));
                sorted = false;
                break;
            }
        }
    }

    public static int daysInMonth(int month, int year) { // 0~11
        if(month==0 || month==2 || month==4 || month==6 || month==7 || month==9 || month==11)   return 31;
        else if(month==3 || month==5 || month==8 || month==10)  return 30;
        else if(month==1){
            if( ( year % 400 ) == 0 )   return 29;
            else if( ( year % 100 ) == 0 )   return 28;
            else if( ( year % 4 ) == 0 )   return 29;
            return 28;
        }
        return 0;
    }

    public void sortApps() throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < appsLength.length(); i++) {
            JSONObject clone = new JSONObject(appsLength.getJSONObject(i).toString());
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
        int len = appsLength.length();
        appsLength = new JSONArray();
        for (int i = 0; i < len; i++) {
            appsLength.put(jsonValues.get(i));
            // TrackAccessibilityService.outerArray.remove(len - 1 - i);
        }
        sorted = true;
    }
}
