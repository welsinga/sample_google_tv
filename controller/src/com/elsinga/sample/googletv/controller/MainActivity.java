package com.elsinga.sample.googletv.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    getWindow().getAttributes().format = android.graphics.PixelFormat.RGBA_8888;

    SharedPreferences prefs = getSharedPreferences("default", MODE_WORLD_READABLE);

    int clientId = prefs.getInt(Prefs.CLIENT_ID, -1);

    Intent intent;

    if (clientId == -1)
    {
      intent = new Intent(this, InitialScreenActivity.class);
    }
    else
    {
      intent = new Intent(this, QuestionQuizClientActivity.class);
    }

    startActivity(intent);
    finish();
  }
}