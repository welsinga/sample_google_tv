package com.elsinga.sample.googletv.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.elsinga.sample.googletv.controller.obj.Player;
import com.elsinga.sample.googletv.controller.obj.Question;
import com.elsinga.sample.googletv.controller.parser.ParserConstants;
import com.elsinga.sample.googletv.controller.parser.QuestionMessages;

public class QuestionQuizClientActivity extends Activity
{
  private static final String TAG         = "QuestionQuizClientActivity";

  private int                 _clientId   = 1;

  private boolean             connected   = false;

  private static final String SERVER_IP   = "192.168.1.109";
  private static final int    SERVER_PORT = 13337;

  private PrintWriter         _outWriter;
  private BufferedReader      _inputReader;

  private LinearLayout        _connectLayout;
  private TextView            _questionTextView;
  private RadioButton         _answer1Button;
  private RadioButton         _answer2Button;
  private RadioButton         _answer3Button;
  private RadioButton         _answer4Button;

  private Question            _currentQuestion;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.quiz);

    // Get network config from prefs
    SharedPreferences prefs = getSharedPreferences("default", MODE_WORLD_READABLE);

    _clientId = prefs.getInt(Prefs.CLIENT_ID, -1);

    Log.i(TAG, "Started client " + _clientId);

    _connectLayout = (LinearLayout) findViewById(R.id.connect_container);
    _questionTextView = (TextView) findViewById(R.id.Question);
    _answer1Button = (RadioButton) findViewById(R.id.Answer1);
    _answer2Button = (RadioButton) findViewById(R.id.Answer2);
    _answer3Button = (RadioButton) findViewById(R.id.Answer3);
    _answer4Button = (RadioButton) findViewById(R.id.Answer4);
  }

  public void connectPressed(View view)
  {
    if (!connected)
    {
      Thread cThread = new Thread(new ClientThread(_clientId));
      cThread.start();
    }
  }

  public void settingsPressed(View view)
  {
    Intent intent = new Intent(this, InitialScreenActivity.class);
    startActivity(intent);
    finish();
  }

  public void setConnected(final boolean connected)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        if (connected)
        {
          _connectLayout.setVisibility(View.GONE);
        }
        else
        {
          finish();
          _connectLayout.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  private void updateWithQuestion(Question question)
  {
    _currentQuestion = question;
    _questionTextView.setText(question.getTitle());

    List<String> answers = question.getAnswers();

    _answer1Button.setText(answers.get(0));
    _answer2Button.setText(answers.get(1));
    _answer3Button.setText(answers.get(2));
    _answer4Button.setText(answers.get(3));

    uncheckAll();
  }

  private void uncheckAll()
  {
    uncheckAllExcept(null);
  }

  private void uncheckAllExcept(RadioButton r)
  {
    RadioButton[] buttons = new RadioButton[4];
    buttons[0] = _answer1Button;
    buttons[1] = _answer2Button;
    buttons[2] = _answer3Button;
    buttons[3] = _answer4Button;
    for (int i = 0; i < buttons.length; i++)
    {
      if (r == buttons[i])
      {
        r.setChecked(true);
      }
      else
      {
        buttons[i].setChecked(false);
      }
    }
  }

  public void answerPressed(View v)
  {
    String chosenAnswer = (String) ((RadioButton) v).getText();
    _currentQuestion.setCorrectAnswer(chosenAnswer);
    uncheckAllExcept((RadioButton) v);
    Log.d(TAG, "Selected = " + ((Button) v).isSelected());
    sendMessageToServer(_clientId, QuestionMessages.postAnswer(_currentQuestion));
  }

  // Use methods here to send/receive message to/from server
  public void sendMessageToServer(int client, JSONObject message)
  {
    if (_outWriter != null)
    {
      try
      {
        message.put(ParserConstants.CLIENT, client);
      }
      catch (JSONException e)
      {
        Log.e(TAG, Log.getStackTraceString(e));
      }
      String stringified = message.toString();
      Log.d(TAG, "Sending to server:" + stringified);
      _outWriter.println(stringified);
    }
  }

  public void receivedMessageFromServer(int client, JSONObject message)
  {
    Log.d(TAG, "Received message from server: " + message.toString());

    try
    {
      String msgType = message.getString(ParserConstants.MSG_TYPE);

      if (ParserConstants.NEW_QUESTION.equalsIgnoreCase(msgType))
      {
        Log.d(TAG, "Got new question");
        Question q = new Question(message.getJSONObject(ParserConstants.MSG_VALUE));
        updateWithQuestion(q);
      }
      else if (ParserConstants.WINNER.equalsIgnoreCase(msgType))
      {
        Log.d(TAG, "Got a winner");
        Player p = new Player(message.getJSONObject(ParserConstants.MSG_VALUE));
        stopGame(p);

      }
      else
      {
        Log.w(TAG, "Received unknown message type from server: " + msgType);
      }

    }
    catch (JSONException e)
    {
      Log.e(TAG, Log.getStackTraceString(e));
    }

  }

  private void stopGame(Player p)
  {
    setConnected(false);

    finish();

    Intent endIntent = new Intent(getBaseContext(), WinnerActivity.class);
    endIntent.putExtra("winner", p);

    startActivity(endIntent);

  }

  public class ClientThread implements Runnable
  {
    private final int mClient;

    public ClientThread(int client)
    {
      mClient = client;
    }

    @Override
    public void run()
    {
      Socket socket = null;
      try
      {
        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
        Log.d(TAG, "Connecting client " + mClient + "...");
        socket = new Socket(serverAddr, SERVER_PORT + mClient);
        Log.i(TAG, "Connected to server on port: " + (SERVER_PORT + mClient));

        // Hide connect button
        connected = true;
        setConnected(connected);

        _outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        _inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (connected)
        {
          try
          {
            String st = _inputReader.readLine();
            if (st != null)
            {
              final JSONObject json = new JSONObject(st);
              runOnUiThread(new Runnable()
              {
                @Override
                public void run()
                {
                  receivedMessageFromServer(mClient, json);
                }
              });
            }
          }
          catch (JSONException e)
          {
            Log.e(TAG, Log.getStackTraceString(e));
          }
        }
      }
      catch (Exception e)
      {
        Log.d(TAG, "Error with socket on client " + mClient);
        Log.e(TAG, Log.getStackTraceString(e));
        connected = false;
        setConnected(connected);
      }
      finally
      {
        if (socket != null && !socket.isClosed())
        {
          try
          {
            socket.close();
          }
          catch (IOException e)
          {
            Log.e(TAG, Log.getStackTraceString(e));
          }
          Log.d(TAG, "Closed client " + mClient + " socket");
        }
      }
    }
  }
}