package com.focus.test1android;

/**
 * Created by AdrianHsu on 15/8/3.
 */
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AddFriendActivity extends Activity {

    final String TAG = "AddFriendActivity";
    private ProfilePictureView userProfilePictureView;
    private TextView userNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend);

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.v(TAG, response.toString());
                        JSONObject json = response.getJSONObject();
                        try {
                            JSONArray jarray = json.getJSONArray("data");
                            Log.v(TAG, jarray.toString());
                            userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
                            userNameView = (TextView) findViewById(R.id.userName);

                            userProfilePictureView.setProfileId(jarray.getJSONObject(0).getString("id"));
                            userNameView.setText(jarray.getJSONObject(0).getString("name"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
        ).executeAsync();
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/me/friendlists",
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//                        /* handle the result */
//                        Log.v(TAG, response.toString());
//                        JSONObject json = response.getJSONObject();
//                        try {
//                            JSONArray jarray = json.getJSONArray("data");
//                            Log.v(TAG, jarray.toString());
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }
//        ).executeAsync();
    }
}