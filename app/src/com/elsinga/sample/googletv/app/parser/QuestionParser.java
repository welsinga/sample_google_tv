package com.elsinga.sample.googletv.app.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.elsinga.sample.googletv.app.R;
import com.elsinga.sample.googletv.app.obj.Question;

public class QuestionParser
{
  private static final String QUESTION_TAG = "question";
  private static final String TITLE_TAG    = "title";
  private static final String CORRECT_TAG  = "correct";
  private static final String WRONG_TAG    = "wrong";
  private static final String IMAGE_TAG    = "image";

  public static ArrayList<Question> load(Context context) throws XmlPullParserException, IOException
  {
    XmlResourceParser xpp = context.getResources().getXml(R.xml.quiz_data);
    xpp.next();
    int eventType = xpp.getEventType();

    ArrayList<Question> questions = new ArrayList<Question>(9);

    Question currentQuestion = null;
    String currentTag = "";

    while (eventType != XmlPullParser.END_DOCUMENT)
    {
      if (eventType == XmlPullParser.START_DOCUMENT)
      {
        // Don't need to do anything here
      }
      else if (eventType == XmlPullParser.START_TAG)
      {
        if (xpp.getName().equals(QUESTION_TAG))
        {
          // Starting to parse new question
          currentQuestion = new Question();
        }
        else
        {
          // Otherwise, save off current tag for code below
          currentTag = xpp.getName();
        }
      }
      else if (eventType == XmlPullParser.END_TAG)
      {
        if (xpp.getName().equals(QUESTION_TAG))
        {
          // We've finished parsing a question, so add it to ArrayList
          questions.add(currentQuestion);
        }
      }
      else if (eventType == XmlPullParser.TEXT)
      {
        // We will go through all the properties - set them on the current question
        if (TITLE_TAG.equals(currentTag))
        {
          currentQuestion.setTitle(xpp.getText());
        }
        else if (CORRECT_TAG.equals(currentTag))
        {
          currentQuestion.setCorrectAnswer(xpp.getText());
          currentQuestion.getAnswers().add(xpp.getText());
        }
        else if (WRONG_TAG.equals(currentTag))
        {
          currentQuestion.getAnswers().add(xpp.getText());
        }
        else if (IMAGE_TAG.equals(currentTag))
        {
          currentQuestion.image = xpp.getText();
        }
      }

      // Move onto next event
      eventType = xpp.next();
    }

    // Finally return all the questions we've built up
    return questions;
  }

}
