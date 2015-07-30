package com.focus.test1android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Arrays;

/**
 * Created by XNS on 2015/7/22.
 */
public class AppTrackActivity extends Activity {

    private Button start_button;
    private Button stop_button;

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
        Intent it = new Intent(this, TrackAccessibilityService.class);

        startService(it);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(Test1Android.TAG, "error");
        }
        start_button.setClickable(false);
        stop_button.setClickable(true);
        TextView service_state = (TextView) findViewById(R.id.service_state);
        service_state.setText("Service On");
        service_state.setBackgroundColor(getResources().getColor(R.color.on_background));
    }

    public void onReportClick(View view) {
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
