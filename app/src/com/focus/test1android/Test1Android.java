package com.focus.test1android;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class Test1Android extends Application {

  static final String TAG = "MyApp";

  @Override
  public void onCreate() {
    super.onCreate();

      FacebookSdk.sdkInitialize(getApplicationContext());

      Parse.enableLocalDatastore(this);
      Parse.initialize(this,
              "frqDKhkEV7Tv7k69KeI6r03yDsZYZkiDDj73Qam4",
              "5nLPzUTiSUJsJv7Mull7jGT0UGjdR3BSJLMdSA4M"
      );

      ParseFacebookUtils.initialize(this);
  }
}
