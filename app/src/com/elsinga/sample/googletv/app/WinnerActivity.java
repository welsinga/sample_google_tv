package com.elsinga.sample.googletv.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.elsinga.sample.googletv.app.obj.Player;

public class WinnerActivity extends Activity
{

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.winner);

    // Find views
    TextView winnerTextView = (TextView) findViewById(R.id.winner);
    TextView scoreTextView = (TextView) findViewById(R.id.score);

    Player winner = (Player) getIntent().getSerializableExtra("winner");

    winnerTextView.setText(winner.getName());
    scoreTextView.setText("With a total score of " + winner.getTotalScore());
  }

  public void restartPressed(View view)
  {
    startActivity(new Intent(getBaseContext(), QuestionQuizActivity.class));
    finish();
  }

}
