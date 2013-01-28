package com.elsinga.sample.googletv.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elsinga.sample.googletv.app.obj.Player;

public class PlayerView extends LinearLayout
{
  private final ProgressBar _progressBar;
  private final ImageView   _checkImage;
  private final TextView    _nameTextView;
  private Player            _player;

  public PlayerView(Context context)
  {
    this(context, null);
  }

  public PlayerView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public PlayerView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);

    View.inflate(context, R.layout.player, this);

    // Find subviews
    _progressBar = (ProgressBar) findViewById(R.id.connecting_progress);
    _checkImage = (ImageView) findViewById(R.id.connecting_check_image);
    _nameTextView = (TextView) findViewById(R.id.connecting_text);

    setConnecting(true);
  }

  public void setConnecting(boolean connecting)
  {
    if (connecting)
    {
      _progressBar.setVisibility(View.VISIBLE);
      _checkImage.setVisibility(View.GONE);
      _nameTextView.setText(R.string.connecting);
    }
    else
    {
      _progressBar.setVisibility(View.GONE);
      _checkImage.setVisibility(View.VISIBLE);
      _nameTextView.setText(_player.getName() == null ? "" : _player.getName());
    }
  }

  public void setPlayer(Player player)
  {
    _player = player;
    setConnecting(false);
  }
}