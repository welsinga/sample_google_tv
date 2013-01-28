package com.elsinga.sample.googletv.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elsinga.sample.googletv.app.obj.Player;
import com.elsinga.sample.googletv.app.obj.Question;
import com.elsinga.sample.googletv.app.parser.ParserConstants;
import com.elsinga.sample.googletv.app.parser.QuestionMessages;
import com.elsinga.sample.googletv.app.parser.QuestionParser;

public class QuestionQuizActivity extends Activity
{
  private static final String   TAG            = "QuestionQuizActivity";

  private TextView              _questionTextView;
  private List<Button>          _optionButtons;
  private List<ParticipantView> _participantViews;
  private List<Player>          _players;
  private List<PlayerView>      _playerViews;
  private List<Question>        _questions;
  private TextView              _countdownTextView;
  private LinearLayout          _homeScreen;
  private ImageView             _imageView;

  private int                   _currentQuestion;
  private long                  _questionStartTime;

  private List<ServerThread>    _serverThreads;
  private Handler               _handler       = new Handler();

  private final static int      QUESTION_TIMER = 15;                      // 15 seconds between questions

  private static final String   SERVER_IP      = "192.168.1.109";
  public static final int       SERVER_PORT    = 13337;
  private static final String[] PLAYER_NAMES   = {"Player 1", "Player 2"};

  private static final int      MAX_CLIENTS    = 2;

  private final Runnable        _gameTick      = new Runnable()
                                               {
                                                 @Override
                                                 public void run()
                                                 {
                                                   int timeLeft = QUESTION_TIMER
                                                                  - (int) Math
                                                                      .floor((System.currentTimeMillis() - _questionStartTime) / 1000.0);
                                                   _countdownTextView.setText(String.format("0:%02d", timeLeft));
                                                   if (timeLeft <= 0)
                                                   {
                                                     _countdownTextView.setText("0:00");

                                                     for (ParticipantView p : _participantViews)
                                                     {
                                                       p.showChoice();
                                                     }

                                                     for (Button b : _optionButtons)
                                                     {
                                                       if (b.getText() == _questions.get(_currentQuestion).getCorrectAnswer())
                                                       {
                                                         b.setSelected(true);
                                                       }
                                                       else
                                                       {
                                                         b.setEnabled(false);
                                                       }
                                                     }
                                                     if (_questions.size() > _currentQuestion + 1)
                                                     {
                                                       _handler.postDelayed(_nextQuestion, 5000);
                                                     }
                                                     else
                                                     {
                                                       stopGame();
                                                     }
                                                   }
                                                   else
                                                   {
                                                     _handler.postDelayed(_gameTick, 1000);
                                                   }
                                                 }
                                               };

  private final Runnable        _nextQuestion  = new Runnable()
                                               {
                                                 @Override
                                                 public void run()
                                                 {
                                                   _currentQuestion = (_currentQuestion + 1) % _questions.size();
                                                   Log.d(TAG, "Next question: " + _currentQuestion);
                                                   sendToAllClients(QuestionMessages.newQuestion(_questions.get(_currentQuestion)));
                                                   _questionStartTime = System.currentTimeMillis();

                                                   runOnUiThread(new Runnable()
                                                   {
                                                     @Override
                                                     public void run()
                                                     {
                                                       displayQuestion(_questions.get(_currentQuestion));
                                                     }
                                                   });
                                                   _handler.postDelayed(_gameTick, 1000);
                                                 }
                                               };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.quiz);

    Log.d(TAG, "Started questionquiz");
    findUIElements();

    _players = new ArrayList<Player>(2);
    _questions = loadQuestions();
    if (_questions == null || _questions.isEmpty())
    {
      Log.e(TAG, "Could not load questions");
      finish();
      return;
    }

    // Show first question
    displayQuestion(_questions.get(_currentQuestion));

    // Create 2 threads for 2 clients
    _serverThreads = new ArrayList<QuestionQuizActivity.ServerThread>(MAX_CLIENTS);
    for (int i = 0; i < MAX_CLIENTS; i++)
    {
      ServerThread st = new ServerThread(i);
      Thread t = new Thread(st);
      t.start();
      _serverThreads.add(st);
    }

  }

  private void findUIElements()
  {
    _questionTextView = (TextView) findViewById(R.id.question_tv);
    _optionButtons = new ArrayList<Button>(4);
    _participantViews = new ArrayList<ParticipantView>(4);
    _playerViews = new ArrayList<PlayerView>(MAX_CLIENTS);
    _optionButtons.add((Button) findViewById(R.id.option_1));
    _optionButtons.add((Button) findViewById(R.id.option_2));
    _optionButtons.add((Button) findViewById(R.id.option_3));
    _optionButtons.add((Button) findViewById(R.id.option_4));
    _participantViews.add((ParticipantView) findViewById(R.id.participant_1));
    _participantViews.add((ParticipantView) findViewById(R.id.participant_2));
    _playerViews.add((PlayerView) findViewById(R.id.player_1));
    _playerViews.add((PlayerView) findViewById(R.id.player_2));

    _countdownTextView = (TextView) findViewById(R.id.countdown_tv);
    _imageView = (ImageView) findViewById(R.id.image);
    _homeScreen = (LinearLayout) findViewById(R.id.home_screen);
  }

  public void playerConnected(final int position)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        Player player = new Player();
        player.setName(PLAYER_NAMES[position]);
        player.setPosition(position);
        _players.add(player);
        _playerViews.get(position).setPlayer(player);
        _participantViews.get(position).setPlayer(player);
      }
    });
  }

  public void playPressed(View view)
  {
    if (_players != null && _players.size() >= 1)
    {
      _homeScreen.setVisibility(View.GONE);
      startGame();
    }
    else
    {
      Toast.makeText(getBaseContext(), "No players connected", Toast.LENGTH_SHORT).show();
    }
  }

  private void displayQuestion(final Question q)
  {
    _questionTextView.setText(q.getTitle());
    for (int i = 0; i < 4; i++)
    {
      Animation anim = AnimationUtils.loadAnimation(this, R.anim.option_out);
      anim.setStartOffset(i * 100);
      _optionButtons.get(i).startAnimation(anim);

      final int finalI = i;

      anim.setAnimationListener(new AnimationListener()
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
          _optionButtons.get(finalI).setText(q.getAnswers().get(finalI));

          Animation anim = AnimationUtils.loadAnimation(QuestionQuizActivity.this, R.anim.option_in);
          _optionButtons.get(finalI).setAnimation(anim);

          _optionButtons.get(finalI).setEnabled(true);
          _optionButtons.get(finalI).setSelected(false);
        }
      });
    }

    int imageResId = getResources().getIdentifier(q.image, "drawable", getPackageName());
    if (imageResId == 0)
    {
      Log.w(TAG, "No image found for id: " + q.image);
    }
    else
    {
      _imageView.setImageResource(imageResId);
    }

  }

  private List<Question> loadQuestions()
  {
    try
    {
      return QuestionParser.load(this);
    }
    catch (XmlPullParserException e)
    {
      Log.e(TAG, "Couldn't parse xml");
      Log.e(TAG, Log.getStackTraceString(e));
    }
    catch (IOException e)
    {
      Log.e(TAG, "IO error while parsing xml");
      Log.e(TAG, Log.getStackTraceString(e));
    }

    return null;
  }

  private void stopGame()
  {
    Player winner = andTheWinnerIs();
    sendToAllClients(QuestionMessages.resultFinished(winner));

    onStop();
    _handler = null;
    finish();

    Intent endIntent = new Intent(getBaseContext(), WinnerActivity.class);

    endIntent.putExtra("winner", winner);

    startActivity(endIntent);

  }

  private Player andTheWinnerIs()
  {

    Player winner = _players.get(0);

    for (Player player : _players)
    {
      if (winner.getTotalScore() < player.getTotalScore())
      {
        winner = player;
      }
    }
    return winner;

  }

  private void startGame()
  {
    _questionStartTime = System.currentTimeMillis();
    _handler.post(_gameTick);
  }

  public void sendToClient(int client, JSONObject object)
  {
    sendMessageToClient(client, object);
  }

  public void sendToAllClients(JSONObject object)
  {
    for (int c = 0; c < MAX_CLIENTS; c++)
    {
      sendMessageToClient(c, object);
    }
  }

  public void sendMessageToClient(int client, JSONObject message)
  {
    if (client >= MAX_CLIENTS)
    {
      Log.e(TAG, "Client id out of range: " + client);
      return;
    }

    _serverThreads.get(client).sendMessageToClient(message);
  }

  public void receivedMessageFromClient(final int client, JSONObject message)
  {
    Log.d(TAG, "Received message from client " + client + ": " + message.toString());

    try
    {
      String msgType = message.getString(ParserConstants.MSG_TYPE);

      if (ParserConstants.POST_ANSWER.equalsIgnoreCase(msgType))
      {
        Log.d(TAG, "Got answer to question");
        Question q = new Question(message.getJSONObject(ParserConstants.MSG_VALUE));
        Question displayedQuestion = _questions.get(_currentQuestion);
        if (q.getTitle().equalsIgnoreCase(displayedQuestion.getTitle()))
        {
          Player player = null;
          for (Player p : _players)
          {
            if (p.getPosition() == client) player = p;
          }

          if (player == null)
          {
            Log.e(TAG, "Received message from unregistered player");
            return;
          }

          if (q.getCorrectAnswer().equalsIgnoreCase(displayedQuestion.getCorrectAnswer()))
          {
            Log.d(TAG, "Answer is correct");
            int timeLeft = QUESTION_TIMER - (int) Math.floor((System.currentTimeMillis() - _questionStartTime) / 1000.0);
            player.setQuestionScore(calculateScore(timeLeft));
          }
          else
          {
            Log.d(TAG, "Answer is wrong");
            player.setQuestionScore(0);
          }

          player.setCurrentChoice(displayedQuestion.indexForAnswer(q.getCorrectAnswer()));
        }
        else
        {
          Log.d(TAG, "Answer is to old question, ignoring...");
        }
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

  private int calculateScore(int timeRemaining)
  {
    return timeRemaining + 3;
  }

  // Server code
  public class ServerThread implements Runnable
  {
    private final int      _client;

    private Handler        _serverHandler;
    private ServerSocket   _serverSocket;
    private PrintWriter    _outWriter;
    private BufferedReader _inputReader;

    public ServerThread(int client)
    {
      _client = client;
    }

    public void sendMessageToClient(JSONObject message)
    {
      if (_outWriter != null)
      {
        try
        {
          message.put(ParserConstants.CLIENT, _client);
        }
        catch (JSONException e)
        {
          Log.e(TAG, Log.getStackTraceString(e));
        }

        final String stringified = message.toString();
        Log.d(TAG, "Sending to client " + _client + ": " + stringified);
        _handler.post(new Runnable()
        {
          @Override
          public void run()
          {
            new SendToClientTask().execute(stringified);
          }
        });
      }
    }

    @Override
    public void run()
    {
      try
      {
        if (SERVER_IP != null)
        {
          _handler.post(new Runnable()
          {
            @Override
            public void run()
            {
              Log.d(TAG, "Listening on IP: " + SERVER_IP);
            }
          });
          _serverSocket = new ServerSocket(SERVER_PORT + _client);
          Log.i(TAG, "Started server on port: " + (SERVER_PORT + _client));
          while (true)
          {
            Socket client = _serverSocket.accept();
            _outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            sendMessageToClient(QuestionMessages.resultOK());

            playerConnected(_client);
            sendToClient(_client, QuestionMessages.newQuestion(_questions.get(_currentQuestion)));
            _handler.post(new Runnable()
            {
              @Override
              public void run()
              {
                Log.d(TAG, "Client " + _client + "  connected.");
              }
            });

            try
            {
              _inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
              _outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

              String line = null;
              while ((line = _inputReader.readLine()) != null)
              {
                JSONObject json = new JSONObject(line);
                receivedMessageFromClient(_client, json);
                sendMessageToClient(QuestionMessages.resultOK());
              }
              break;
            }
            catch (Exception e)
            {
              _handler.post(new Runnable()
              {
                @Override
                public void run()
                {
                  Log.d(TAG, "Oops. Connection interrupted. Please reconnect your devices.");
                }
              });
              e.printStackTrace();
            }
          }
        }
        else
        {
          _handler.post(new Runnable()
          {
            @Override
            public void run()
            {
              Log.d(TAG, "Couldn't detect internet connection.");
            }
          });
        }
      }
      catch (Exception e)
      {
        _handler.post(new Runnable()
        {
          @Override
          public void run()
          {
            Log.d(TAG, "Error");
          }
        });
        e.printStackTrace();
      }
    }

    class SendToClientTask extends AsyncTask<String, Void, Integer>
    {
      @Override
      protected Integer doInBackground(String... msgs)
      {
        String msg = msgs[0];
        _outWriter.println(msg);
        return 0;
      }
    }
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    for (ServerThread st : _serverThreads)
    {
      try
      {
        // Close the socket upon exiting
        st._serverSocket.close();
      }
      catch (IOException e)
      {
        Log.e(TAG, Log.getStackTraceString(e));
      }
    }
  }

}