package com.elsinga.sample.googletv.controller.obj;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.elsinga.sample.googletv.controller.parser.ParserConstants;

public class Player implements Serializable
{

  private static final String TAG              = "Player";

  /**
   * 
   */
  private static final long   serialVersionUID = 1195114127607762436L;

  private String              _name;
  private int                 _totalScore;

  public Player(JSONObject obj)
  {
    try
    {
      _name = obj.getString(ParserConstants.NAME_PLAYER);
      _totalScore = obj.getInt(ParserConstants.SCORE);
    }
    catch (JSONException e)
    {
      Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  public void setName(String name)
  {
    _name = name;
  }

  public String getName()
  {
    return _name;
  }

  public void setTotalScore(int totalScore)
  {
    _totalScore = totalScore;
  }

  public int getTotalScore()
  {
    return _totalScore;
  }

}
