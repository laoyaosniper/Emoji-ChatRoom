package com.emojichatroom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.emojicharroom.util.Config;
import com.emojicharroom.util.DatabaseHelper;
import com.emojicharroom.util.Logger;
import android.os.Process;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class ChatRoom extends Activity {
//  private static final int NUM_EMOJI_HINT = 3;
  private Handler mHandler;
  
  private Button sendButton;
//  private Button[] emoji = new Button[NUM_EMOJI_HINT];
  private EditText textField;
  
  private ListView messageView;
  private ArrayAdapter<String> messageAdapter;
  private NetworkConsole networkConsole;
  
  private LinearLayout layout;
  
//  private ViewSwitcher switcher;
  private Button more;
  private Button back;
  
  private TableLayout table;
  private RelativeLayout emojiPanel;
  public DatabaseHelper dbHelper;

  public class EmojiRank {
    public String emoji;
    public int eid;
    public Double p;
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);
    
//    this.switcher = (ViewSwitcher) findViewById(R.id.switcher);
//    switcher.setDisplayedChild(0);
    this.table = (TableLayout) findViewById(R.id.table);
    this.emojiPanel = (RelativeLayout) findViewById(R.id.rl_emoji);
    this.more = (Button) findViewById(R.id.more);
    more.setOnClickListener( new OnClickListener() {

      @Override
      public void onClick(View v) {
        table.setVisibility(View.VISIBLE);
        emojiPanel.setVisibility(View.GONE);
//        switcher.showNext();
      }
      
    });
    
    this.back = (Button) findViewById(R.id.back);
    back.setOnClickListener( new OnClickListener() {

      @Override
      public void onClick(View v) {
        table.setVisibility(View.GONE);
        emojiPanel.setVisibility(View.VISIBLE);
//        switcher.showPrevious();
      }
      
    });
    this.layout = (LinearLayout) findViewById(R.id.layout);
    for (int i = 0; i < 18; i++) {
//      Button textButton = new Button();
      Button textButton = (Button)getLayoutInflater().inflate(R.layout.emoji_selector, null);
      String text = "Yes";
//      for ( int j = 0; j < i; j++) {
//        text = text + "s";
//      }
      textButton.setText(text);
      textButton.setMinimumWidth(150);
      layout.addView(textButton);
    }
    
    this.mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        String text;
        switch(msg.what) {
          case Config.MSG_TEXT:
            text = (String)msg.obj;
            Logger.d("Handler get text:" + text);
            messageAdapter.add(text);
            runOnUiThread(new Runnable() {
              public void run() { messageAdapter.notifyDataSetChanged(); }
            });
            break;
          default:
            Logger.e("Other message");
            break;
        }
      }

    };
    
    this.messageView = (ListView) findViewById(R.id.chat_listview);
    this.messageAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
    this.messageView.setAdapter(messageAdapter);
    this.networkConsole = new NetworkConsole(this) {

      @Override
      public void handleText(String text) {        
        if ( text != null ) {
          Logger.d("Send text to UI:" + text);
          mHandler.obtainMessage(Config.MSG_TEXT, text).sendToTarget();
        }        
      }
      
    };
    
    new Thread(networkConsole).start();
    
    this.sendButton = (Button) findViewById(R.id.chat_send);
//    emoji[0] = (Button) findViewById(R.id.emoji0);
//    emoji[0].setText("0:\uD83D\uDE01");
//    emoji[1] = (Button) findViewById(R.id.emoji1);
//    emoji[1].setText("1:\uD83D\uDE28");
//    emoji[2] = (Button) findViewById(R.id.emoji2);
//    emoji[2].setText("2:\uD83D\uDE1E");
    
    this.textField = (EditText) findViewById(R.id.chat_editmessage);
    this.textField.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (v != null) {
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        
      }
    });
    this.textField.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
//        sendButton.setText("Len:" + s.length());
        long ts = System.currentTimeMillis();

        String text = s.toString();
        final String[] tokens = text.split(" ");
        for ( String token : tokens ) {
          Logger.e(token);
        }
        
        int totalNum = dbHelper.priorMap.size();
        int emojiNum = dbHelper.emojiMap.size();
        int wordNum = dbHelper.word2IdMap.size();
        Logger.e("emoji number = " + emojiNum + ", wordNum = " + wordNum);

        HashSet<Integer> wordList = new HashSet<Integer>();
        for ( String token : tokens ) {
          if (dbHelper.word2IdMap.containsKey(token)) {
//            networkConsole.handleText(token + " is there");
            int wid = dbHelper.word2IdMap.get(token);
//            flags[wid - 1] = true;
            wordList.add(wid);
          }
          else {
//            networkConsole.handleText(token + " not exist");
          } 
        }
        

        EmojiRank[] emojiQueue = new EmojiRank[emojiNum];
        // Initialize with prior probability
        int emojiCount = 0;
        for (int c = 1; c <= totalNum; c++) {
          String emoji = dbHelper.emojiMap.get(c);
          if (emoji != null) {
            EmojiRank rank = new EmojiRank();
            rank.emoji = emoji;
            rank.eid = c;
            rank.p = dbHelper.precomputeMap.get(c);
            emojiQueue[emojiCount] = rank;
            emojiCount++;
          }
        }
        
        // predict
        for (int i = 0; i < emojiNum; i++) {
          EmojiRank rank = emojiQueue[i];
          ArrayList<Integer> zeroList = dbHelper.zeroMap.get(rank.eid);
          if (zeroList != null) {
            int zeroLen = zeroList.size();
            for (int wid : wordList) {
              if (zeroList.contains(wid)) {
                zeroLen--;
                continue;
              }
              if (dbHelper.likelihoodMap.containsKey(rank.eid*65536+wid)) {
                double pp = dbHelper.likelihoodMap.get(rank.eid*65536+wid);
                rank.p = rank.p / (1 - pp) * pp;
              }
              else {
//                rank.p = 0.0;
                rank.p *= dbHelper.minValue;
              }
            }
            
            if (zeroLen != 0) {
              rank.p = 0.0;
            }
          }
          else {
            for (int wid : wordList) {
              if (dbHelper.likelihoodMap.containsKey(rank.eid*65536+wid)) {
                double pp = dbHelper.likelihoodMap.get(rank.eid*65536+wid);
                rank.p = rank.p / (1 - pp) * pp;
              }
              else {
//                rank.p = 0.0;
                rank.p *= dbHelper.minValue;
              }
            }
          }
        }
        Arrays.sort(emojiQueue, new Comparator<EmojiRank>() {
          @Override
          public int compare(EmojiRank lhs, EmojiRank rhs) {
            return -1 * lhs.p.compareTo(rhs.p);
          }
        });
        sendButton.setText("Delay: " + (System.currentTimeMillis() - ts));
        if (emojiQueue != null) {
          for (int i = 0; i < 18; i++) {
            Button emojiButton = (Button) layout.getChildAt(i);
            if ( emojiButton != null ) {
              EmojiRank rank = emojiQueue[i];
              emojiButton.setText(rank.emoji);
              
            }
            else {
              Logger.e("Button " + i + " cannot be fetched!");
            }
          }
          
        }
      }
      
    });
    
//    for ( Button b : emoji ) {
//      b.setOnClickListener(new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//          if ( v instanceof Button ) {
//            textField.append(((Button)v).getText());
//          }
//          else {
//            Logger.e("Error when press emoji button: not instance of button");
//          }
//        }
//      });
//    }

    sendButton.setOnClickListener( new OnClickListener() {

      @Override
      public void onClick(View v) {
        String text = textField.getText().toString();
        final String[] tokens = text.split(" ");
        for ( String token : tokens ) {
          Logger.e(token);
        }
        text = "AppID " + Process.myPid() + ":" + text;
        try {
          networkConsole.sendText(text);
        } catch (IOException e) {
          Logger.e(e.toString());
        }
        textField.getText().clear();
        textField.clearFocus();
        // start predict thread
        new Thread(new Runnable() {

          @Override
          public void run() {
            long ts = System.currentTimeMillis();
            
            int totalNum = dbHelper.priorMap.size();
            int emojiNum = dbHelper.emojiMap.size();
            int wordNum = dbHelper.word2IdMap.size();
            Logger.e("emoji number = " + emojiNum + ", wordNum = " + wordNum);

//            EmojiRank[] emojiQueue = new EmojiRank[emojiNum];
////            boolean[] flags = new boolean[wordNum];
//            // Initialize with prior probability
//            int emojiCount = 0;
//            for (int c = 1; c <= totalNum; c++) {
//              String emoji = dbHelper.emojiMap.get(c);
//              if (emoji != null) {
//                EmojiRank rank = new EmojiRank();
//                rank.emoji = emoji;
//                rank.eid = c;
//                rank.p = dbHelper.priorMap.get(c);
////                rank.p = dbHelper.precomputeMap.get(c);
//                emojiQueue[emojiCount] = rank;
//                emojiCount++;
//              }
//            }
//            networkConsole.handleText("EmojiNum " + emojiNum + " emojiCount " + emojiCount);
//            // decide using p or 1 - p
//            for (int i = 0; i < wordNum; i++) {
//              flags[i] = false;
//            }
            HashSet<Integer> wordList = new HashSet<Integer>();
            for ( String token : tokens ) {
              if (dbHelper.word2IdMap.containsKey(token)) {
                networkConsole.handleText(token + " is there");
                int wid = dbHelper.word2IdMap.get(token);
//                flags[wid - 1] = true;
                wordList.add(wid);
              }
              else {
                networkConsole.handleText(token + " not exist");
              } 
            }
            
//            // predict
//            for (int i = 0; i < emojiNum; i++) {
//              EmojiRank rank = emojiQueue[i];
////              Logger.e("c = " + rank.eid);
////              for (int wid : wordList) {
////                LikelihoodEntry entry = new LikelihoodEntry(rank.eid, wid);
////                if (dbHelper.likelihoodMap.containsKey(entry)) {
////                  double pp = dbHelper.likelihoodMap.get(entry);
////                  rank.p = rank.p / (1 - pp) * pp;
////                }
////                else {
////                  rank.p = 0.0;
////                }
////              }
//                         
//              for (int w = 0; w < wordNum; w++) {
//                if (dbHelper.likelihoodMap.containsKey(rank.eid*65536+w+1)) {
//                  double pp = dbHelper.likelihoodMap.get(rank.eid*65536+w+1);
//                  if (flags[w] == true) {
//                    Logger.e("c=" + rank.eid + " wid " + (w + 1) + " word " + dbHelper.wordMap.get(w+1));
//                    rank.p *= pp;
//                  }
//                  else {
//                    rank.p *= (1 - pp);
//                  }
//                }
//                // not exist, true -> 1.0, false -> 0.0
//                else {
//                  if (flags[w] == true) {
////                    Logger.e("c=" + rank.eid + " wid " + (w + 1) + "not exist");
////                    rank.p = 0.0;
//                    rank.p *= dbHelper.minValue;
//                  }
//                }
//              }
//            }
//            Arrays.sort(emojiQueue, new Comparator<EmojiRank>() {
//              @Override
//              public int compare(EmojiRank lhs, EmojiRank rhs) {
//                return -1 * lhs.p.compareTo(rhs.p);
//              }
//            });
//
//            if (emojiQueue != null) {
//              for (int i = 0; i < 5; i++) {
//                EmojiRank rank = emojiQueue[i];
////                if ( rank.p != 0 )
//                networkConsole.handleText(rank.eid + " " + rank.emoji + " " + rank.p);
//              }
//            }
//
//            networkConsole.handleText("\n\n\n");
            

            EmojiRank[] emojiQueue = new EmojiRank[emojiNum];
            // Initialize with prior probability
            int emojiCount = 0;
            for (int c = 1; c <= totalNum; c++) {
              String emoji = dbHelper.emojiMap.get(c);
              if (emoji != null) {
                EmojiRank rank = new EmojiRank();
                rank.emoji = emoji;
                rank.eid = c;
                rank.p = dbHelper.precomputeMap.get(c);
                emojiQueue[emojiCount] = rank;
                emojiCount++;
              }
            }
//            networkConsole.handleText("EmojiNum " + emojiNum + " emojiCount " + emojiCount);
            
            // predict
            for (int i = 0; i < emojiNum; i++) {
              EmojiRank rank = emojiQueue[i];
              ArrayList<Integer> zeroList = dbHelper.zeroMap.get(rank.eid);
              if (zeroList != null) {
                int zeroLen = zeroList.size();
                for (int wid : wordList) {
                  if (zeroList.contains(wid)) {
                    zeroLen--;
                    continue;
                  }
                  if (dbHelper.likelihoodMap.containsKey(rank.eid*65536+wid)) {
                    double pp = dbHelper.likelihoodMap.get(rank.eid*65536+wid);
                    rank.p = rank.p / (1 - pp) * pp;
                  }
                  else {
//                    rank.p = 0.0;
                    rank.p *= dbHelper.minValue;
                  }
                }
                
                if (zeroLen != 0) {
                  rank.p = 0.0;
                }
              }
              else {
                for (int wid : wordList) {
                  if (dbHelper.likelihoodMap.containsKey(rank.eid*65536+wid)) {
                    double pp = dbHelper.likelihoodMap.get(rank.eid*65536+wid);
                    rank.p = rank.p / (1 - pp) * pp;
                  }
                  else {
//                    rank.p = 0.0;
                    rank.p *= dbHelper.minValue;
                  }
                }
              }
            }
            Arrays.sort(emojiQueue, new Comparator<EmojiRank>() {
              @Override
              public int compare(EmojiRank lhs, EmojiRank rhs) {
                return -1 * lhs.p.compareTo(rhs.p);
              }
            });
            networkConsole.handleText("Delay: " + (System.currentTimeMillis() - ts));
            if (emojiQueue != null) {
              for (int i = 0; i < 5; i++) {
                EmojiRank rank = emojiQueue[i];
                networkConsole.handleText(rank.eid + " " + rank.emoji + " " + rank.p);
              }
            }
            
            
            
            
            
            
//            if ( networkConsole.emojiMap != null ) {
//              Logger.d("Got " + networkConsole.emojiMap.size() + " emoji");
//              StringBuilder strBld = new StringBuilder();
//              int count = 0;
//              for ( Map.Entry<Integer, String> entry : networkConsole.emojiMap.entrySet() ) {
//                strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//                count++;
//                if ( count == 5 ) {
//                  networkConsole.handleText(strBld.toString());
//                  strBld = new StringBuilder();
//                  count = 0;
//                  break;
//                }
//              }
//            }
//            if ( networkConsole.wordMap != null ) {
//              Logger.d("Got " + networkConsole.wordMap.size() + " emoji");
//              StringBuilder strBld = new StringBuilder();
//              int count = 0;
//              for ( Map.Entry<Integer, String> entry : networkConsole.wordMap.entrySet() ) {
//                strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//                count++;
//                if ( count == 5 ) {
//                  networkConsole.handleText(strBld.toString());
//                  strBld = new StringBuilder();
//                  count = 0;
//                  break;
//                }
//              }
//            }
//            if ( networkConsole.likelihoodMap != null ) {
//              Logger.d("Got " + networkConsole.likelihoodMap.size() + " emoji");
//              StringBuilder strBld = new StringBuilder();
//              int count = 0;
//              for ( Map.Entry<LikelihoodEntry, Double> entry : networkConsole.likelihoodMap.entrySet() ) {
//                strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//                count++;
//                if ( count == 5 ) {
//                  networkConsole.handleText(strBld.toString());
//                  strBld = new StringBuilder();
//                  count = 0;
//                  break;
//                }
//              }
//            }
//            if ( networkConsole.priorMap != null ) {
//              Logger.d("Got " + networkConsole.priorMap.size() + " emoji");
//              StringBuilder strBld = new StringBuilder();
//              int count = 0;
//              for ( Map.Entry<Integer, Double> entry : networkConsole.priorMap.entrySet() ) {
//                strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//                count++;
//                if ( count == 5 ) {
//                  networkConsole.handleText(strBld.toString());
//                  strBld = new StringBuilder();
//                  count = 0;
//                  break;
//                }
//              }
//            }
          }
          
        }).start();
      }
      
    });
    
//    for ( int i = 0; i < 20; i++ ) {
//      mHandler.obtainMessage(Config.MSG_TEXT, "HABhsf" + i).sendToTarget();
//    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.chat_room, menu);
    return true;
  }

}
