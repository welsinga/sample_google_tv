package com.elsinga.sample.googletv.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elsinga.sample.googletv.app.obj.Player;

public class ParticipantView extends LinearLayout
{
  private int             _score;

  private final TextView  _scoreTextView;
  private final TextView  _nameTextView;
  private final ImageView _choiceImageView;
  private Player          _player;

  public ParticipantView(Context context)
  {
    this(context, null);
  }

  public ParticipantView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public ParticipantView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);

    View.inflate(context, R.layout.participant, this);

    _scoreTextView = (TextView) findViewById(R.id.score_tv);
    _nameTextView = (TextView) findViewById(R.id.name_tv);
    _choiceImageView = (ImageView) findViewById(R.id.choice_iv);

    setScore(0);

  }

  void addToScore(int score)
  {
    _scoreTextView.setText("+" + Integer.toString(score));
    _choiceImageView.setVisibility(View.GONE);
    _scoreTextView.setVisibility(View.VISIBLE);

    _player.setTotalScore(_player.getTotalScore() + score);
    _player.setQuestionScore(0);

    Animation fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
    fade.setAnimationListener(new AnimationListener()
    {
      @Override
      public void onAnimationStart(Animation animation)
      {
      }

      @Override
      public void onAnimationRepeat(Animation animation)
      {
      }

      @Override
      public void onAnimationEnd(Animation animation)
      {
        setScore(_player.getTotalScore());
      }
    });

    _scoreTextView.startAnimation(fade);
  }

  void setScore(int score)
  {
    _score = score;
    _scoreTextView.setText(Integer.toString(score));
    _choiceImageView.setVisibility(View.GONE);
    _scoreTextView.setVisibility(View.VISIBLE);
  }

  public void setPlayer(Player player)
  {
    _player = player;
    _nameTextView.setText(_player.getName() == null ? "" : _player.getName());
    setScore(_player.getTotalScore());
  }

  void showChoice()
  {
    if (_player == null || _player.getCurrentChoice() == -1)
    {
      return;
    }

    switch (_player.getCurrentChoice())
    {
      case 0 :
        _choiceImageView.setImageResource(R.drawable.circle_a);
        break;
      case 1 :
        _choiceImageView.setImageResource(R.drawable.circle_b);
        break;
      case 2 :
        _choiceImageView.setImageResource(R.drawable.circle_c);
        break;
      case 3 :
        _choiceImageView.setImageResource(R.drawable.circle_d);
        break;
      default :
        break;
    }

    _choiceImageView.setVisibility(View.VISIBLE);
    _scoreTextView.setVisibility(View.GONE);
    _player.setCurrentChoice(-1);

    Animation fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
    fade.setAnimationListener(new AnimationListener()
    {
      @Override
      public void onAnimationStart(Animation animation)
      {
      }

      @Override
      public void onAnimationRepeat(Animation animation)
      {
      }

      @Override
      public void onAnimationEnd(Animation animation)
      {
        addToScore(_player.getQuestionScore());
      }
    });

    _choiceImageView.startAnimation(fade);
  }

}
