package com.elsinga.sample.googletv.app.obj;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.elsinga.sample.googletv.app.parser.ParserConstants;

public class Player implements Serializable
{

  private static final String TAG              = "Player";

  /**
   * 
   */
  private static final long   serialVersionUID = 1195114127607762436L;

  private int                 _position;
  private String              _name;
  private int                 _currentChoice;
  private int                 _questionScore;
  private int                 _totalScore;

  public Player()
  {
    _currentChoice = -1;
  }

  public JSONObject toJSON()
  {
    JSONObject obj = new JSONObject();
    try
    {
      obj.put(ParserConstants.NAME_PLAYER, _name);
      obj.put(ParserConstants.SCORE, _totalScore);
    }
    catch (JSONException e)
    {
      Log.e(TAG, Log.getStackTraceString(e));
    }

    return obj;
  }

  public void setName(String name)
  {
    _name = name;
  }

  public String getName()
  {
    return _name;
  }

  public void setCurrentChoice(int currentChoice)
  {
    _currentChoice = currentChoice;
  }

  public int getCurrentChoice()
  {
    return _currentChoice;
  }

  public void setQuestionScore(int questionScore)
  {
    _questionScore = questionScore;
  }

  public int getQuestionScore()
  {
    return _questionScore;
  }

  public void setTotalScore(int totalScore)
  {
    _totalScore = totalScore;
  }

  public int getTotalScore()
  {
    return _totalScore;
  }

  public void setPosition(int position)
  {
    _position = position;
  }

  public int getPosition()
  {
    return _position;
  }

  @Override
  public int hashCode()
  {
    return _position;
  }

  @Override
  public boolean equals(Object o)
  {
    return o.getClass() == Player.class && ((Player) o)._position == _position;
  }

}
