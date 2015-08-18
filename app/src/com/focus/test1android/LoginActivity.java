package com.focus.test1android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends Activity {

  private Dialog progressDialog;
  static final String TAG = "LoginActivity";

  private List<AppInfo> applicationList = new ArrayList<AppInfo>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    // Check if there is a currently logged in user
    // and if it's linked to a Facebook account.
    ParseUser currentUser = ParseUser.getCurrentUser();
    if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
      showUserDetailsActivity();
    }

    //Store applications
    applicationList.clear();

    //Query the applications
    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
    for (ResolveInfo ri : ril) {
      applicationList.add(new AppInfo(LoginActivity.this, ri));
    }
    Collections.sort(applicationList);

    for (AppInfo appInfo : applicationList) {
      //load icons before shown. so the list is smoother
      final String appName = appInfo.getName();
      final String packageName = appInfo.getPackageName();
      /*
      Drawable icon = appInfo.getIcon();
      Bitmap bitmap = drawableToBitmap(icon);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      // Compress image to lower quality scale 1 - 100
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
      byte[] image = stream.toByteArray();

      // Create the ParseFile
      final ParseFile file  = new ParseFile("picture_1.jpeg", image);
      */

      ParseQuery<ParseObject> query = ParseQuery.getQuery("AppInfo");
      query.whereEqualTo("packageName", packageName);
      query.getFirstInBackground(new GetCallback<ParseObject>() {
        public void done(ParseObject object, ParseException e) {
          if (object == null) {
            ParseObject app = new ParseObject("AppInfo");
            app.put("appName", appName);
            app.put("packageName", packageName);
            //app.put("icon", file);
            app.saveEventually();
          } else {

          }
        }
      });
    }

  }
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
  }

  public void onLoginClick(View v) {
    progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

    // "public_profile, email, user_status, user_friends, user_about_me, user_location"
    List<String> permissions = Arrays.asList("public_profile, user_friends");
    Log.d(TAG, Arrays.toString(permissions.toArray()));

    // for extended permissions, like "user_about_me", your app must be reviewed by Facebook team
    // (https://developers.facebook.com/docs/facebook-login/permissions/)
    ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException err) {
        progressDialog.dismiss();
        if (user == null) {
          Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
        } else if (user.isNew()) {
          Log.d(TAG, "User signed up and logged in through Facebook!");
          showUserDetailsActivity();
        } else {
          Log.d(TAG, "User logged in through Facebook!");
          Log.d(TAG, AccessToken.getCurrentAccessToken().getPermissions().toString());
          showUserDetailsActivity();
        }
      }
    });
  }

  private void showUserDetailsActivity() {
    Intent intent = new Intent(this, UserDetailsActivity.class);
    startActivity(intent);
  }
  public static Bitmap drawableToBitmap (Drawable drawable) {
    Bitmap bitmap = null;

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if(bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

}
