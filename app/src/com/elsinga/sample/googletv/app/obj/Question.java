package com.elsinga.sample.googletv.app.obj;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.elsinga.sample.googletv.app.parser.ParserConstants;

public class Question
{

  private static final String TAG = "Question";

  private String              _title;
  private String              _correctAnswer;
  private List<String>        _answers;
  public String               image;

  public Question()
  {
    _answers = new ArrayList<String>(4);
  }

  public Question(JSONObject obj)
  {
    try
    {
      _title = obj.getString(ParserConstants.QUESTION);
      _correctAnswer = obj.getString(ParserConstants.CORRECT_ANSWER);
      JSONArray array = obj.getJSONArray(ParserConstants.ANSWERS);
      _answers = new ArrayList<String>(4);
      for (int a = 0; a < 4; a++)
      {
        _answers.add(array.getString(a));
      }
    }
    catch (JSONException e)
    {
      Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  public void setTitle(String title)
  {
    _title = title;
  }

  public String getTitle()
  {
    return _title;
  }

  public void setCorrectAnswer(String correctAnswer)
  {
    _correctAnswer = correctAnswer;
  }

  public String getCorrectAnswer()
  {
    return _correctAnswer;
  }

  public void setAnswers(List<String> answers)
  {
    _answers = answers;
  }

  public List<String> getAnswers()
  {
    return _answers;
  }

  public JSONObject toJSON()
  {
    JSONObject obj = new JSONObject();
    try
    {
      obj.put(ParserConstants.QUESTION, _title);
      obj.put(ParserConstants.CORRECT_ANSWER, _correctAnswer);
      obj.put(ParserConstants.ANSWERS, new JSONArray(_answers));
    }
    catch (JSONException e)
    {
      Log.e(TAG, Log.getStackTraceString(e));
    }

    return obj;
  }

  public int indexForAnswer(String a)
  {
    int index = -1;
    for (int c = 0; c < 4; c++)
    {
      if (_answers.get(c).equalsIgnoreCase(a))
      {
        index = c;
        break;
      }
    }

    return index;
  }

}
