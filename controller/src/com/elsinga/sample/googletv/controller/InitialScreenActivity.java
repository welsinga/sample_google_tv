package com.elsinga.sample.googletv.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class InitialScreenActivity extends Activity
{
  private static final String TAG = "QuizClientActivity";

  private EditText            _clientIdEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.initialscreen);

    Log.i(TAG, "Configuring client...");

    // Find views
    _clientIdEditText = (EditText) findViewById(R.id.client_id);

    // Load prefs
    SharedPreferences prefs = getSharedPreferences("default", MODE_WORLD_READABLE);
    int clientId = prefs.getInt(Prefs.CLIENT_ID, -1);
    if (clientId != -1)
    {
      _clientIdEditText.setText(Integer.toString(clientId));
    }
  }

  public void savePressed(View v)
  {
    // Load prefs
    SharedPreferences prefs = getSharedPreferences("default", MODE_WORLD_READABLE);

    Editor e = prefs.edit();

    try
    {
      int newId = Integer.parseInt(_clientIdEditText.getText().toString());
      e.putInt(Prefs.CLIENT_ID, newId);
    }
    catch (Exception e1)
    {
      Log.e(TAG, "Coulndn't parse client id");
    }

    e.commit();

    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }

}
