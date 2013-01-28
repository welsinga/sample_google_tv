package com.elsinga.sample.googletv.app.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.elsinga.sample.googletv.app.obj.Player;
import com.elsinga.sample.googletv.app.obj.Question;

public class QuestionMessages
{
  public static JSONObject resultOK()
  {
    JSONObject json = new JSONObject();
    try
    {
      json.put(ParserConstants.MSG_TYPE, ParserConstants.STATUS);
      json.put(ParserConstants.MSG_VALUE, ParserConstants.STATUS_OK);
    }
    catch (JSONException e)
    {
    }

    return json;
  }

  public static JSONObject resultFinished(Player p)
  {
    JSONObject json = new JSONObject();
    try
    {
      json.put(ParserConstants.MSG_TYPE, ParserConstants.WINNER);
      json.put(ParserConstants.MSG_VALUE, p.toJSON());
    }
    catch (JSONException e)
    {
    }

    return json;
  }

  public static JSONObject newQuestion(Question q)
  {
    JSONObject json = new JSONObject();
    try
    {
      json.put(ParserConstants.MSG_TYPE, ParserConstants.NEW_QUESTION);
      json.put(ParserConstants.MSG_VALUE, q.toJSON());
    }
    catch (JSONException e)
    {
    }

    return json;
  }

  public static JSONObject postAnswer(Question q)
  {
    JSONObject json = new JSONObject();
    try
    {
      json.put(ParserConstants.MSG_TYPE, ParserConstants.POST_ANSWER);
      json.put(ParserConstants.MSG_VALUE, q.toJSON());
    }
    catch (JSONException e)
    {
    }

    return json;
  }
}
