package com.focus.test1android;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Test1Android extends Application {

  static final String TAG = "MyApp";
  public static ItemDAO itemDAO;
  public static JSONObject hourBlockId = new JSONObject();
  public static JSONObject yetUpdate = new JSONObject();
  public static List<TimeBlock> years = new ArrayList<TimeBlock>();

  @Override
  public void onCreate() {
    super.onCreate();
    itemDAO = new ItemDAO(getApplicationContext());
    FacebookSdk.sdkInitialize(getApplicationContext());
    /*
    long time = System.currentTimeMillis() + TrackAccessibilityService.deltaTime;

    Date date = new Date(time);
    Calendar rightNow = Calendar.getInstance();
    rightNow.setTimeInMillis(1000 * (time / 1000));

    Log.v(TAG, "Date: " + date);
    Log.v(TAG, "Year: " + rightNow.get(rightNow.YEAR));
    Log.v(TAG, "month: " + rightNow.get(rightNow.MONTH));
    Log.v(TAG, "week: " + rightNow.get(rightNow.DAY_OF_WEEK));
    Log.v(TAG, "day: " + rightNow.get(rightNow.DAY_OF_MONTH));
    Log.v(TAG, "hour: " + rightNow.get(rightNow.HOUR_OF_DAY));

    Log.v(TAG, "StartMonth");
    rightNow.set(rightNow.get(rightNow.YEAR), 0, 1, 0, 0, 0);
    for(int i = 0; i < 12; i++) {
      Log.v(TAG, "Time: " + i);
      rightNow.set(rightNow.get(rightNow.YEAR), i, 1);
      Log.v(TAG, "Date: " + rightNow.getTime());
      Log.v(TAG, "Long: " + rightNow.getTimeInMillis());
      Log.v(TAG, "Year: " + rightNow.get(rightNow.YEAR));
      Log.v(TAG, "month: " + rightNow.get(rightNow.MONTH));
      Log.v(TAG, "week: " + rightNow.get(rightNow.DAY_OF_WEEK));
      Log.v(TAG, "day: " + rightNow.get(rightNow.DAY_OF_MONTH));
      Log.v(TAG, "hour: " + rightNow.get(rightNow.HOUR_OF_DAY));
      Log.v(TAG, "min: " + rightNow.get(rightNow.MINUTE));
      Log.v(TAG, "sec: " + rightNow.get(rightNow.SECOND));
    }
    */
    Parse.enableLocalDatastore(this);
    Parse.initialize(this,
                            "frqDKhkEV7Tv7k69KeI6r03yDsZYZkiDDj73Qam4",
                            "5nLPzUTiSUJsJv7Mull7jGT0UGjdR3BSJLMdSA4M"
    );

    ParseFacebookUtils.initialize(this);
  }

}
