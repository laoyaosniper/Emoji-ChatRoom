package com.emojichatroom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
  
  private LinearLayout layout_point;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);
    
    /**
     * Trying to copy the example, but still ongoing. Not well tested!
     */
    this.layout_point = (LinearLayout) findViewById(R.id.iv_image);
    
    ImageView imageView;  
    for (int i = 0; i < 7; i++) {  
        imageView = new ImageView(this);  
        imageView.setBackgroundResource(R.drawable.greendot);  
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  
                new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,  
                        LayoutParams.WRAP_CONTENT));  
        layoutParams.leftMargin = 10;  
        layoutParams.rightMargin = 10;  
        layoutParams.width = 8;  
        layoutParams.height = 8;  
        layout_point.addView(imageView, layoutParams);  
        if (i == 0 || i == 7 - 1) {  
            imageView.setVisibility(View.GONE);  
        }  
        if (i == 1) {  
            imageView.setBackgroundResource(R.drawable.reddot);  
        }  

    } 
    
    /**
     * The following are tested
     */

    this.table = (TableLayout) findViewById(R.id.table);
    this.emojiPanel = (RelativeLayout) findViewById(R.id.rl_emoji);
    /**
     * More and less button in emoji selector
     */
    this.more = (Button) findViewById(R.id.more);
    more.setOnClickListener( new OnClickListener() {

      @Override
      public void onClick(View v) {
        table.setVisibility(View.VISIBLE);
        emojiPanel.setVisibility(View.GONE);
      }
      
    });
    
    this.back = (Button) findViewById(R.id.back);
    back.setOnClickListener( new OnClickListener() {

      @Override
      public void onClick(View v) {
        table.setVisibility(View.GONE);
        emojiPanel.setVisibility(View.VISIBLE);
      }
      
    });
    /**
     * Emoji selector: initial value can be set again
     */
    this.layout = (LinearLayout) findViewById(R.id.layout);
    for (int i = 0; i < 18; i++) {
      Button textButton = (Button)getLayoutInflater().inflate(R.layout.emoji_selector, null);
      String text = "Yes";
      textButton.setText(text);
      textButton.setMinimumWidth(150);

      textButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if ( v instanceof Button ) {
            textField.append(((Button)v).getText());
          }
          else {
            Logger.e("Error when press emoji button: not instance of button");
          }
        }
      });
      
      layout.addView(textButton);
    }
    
    /**
     * Put chatting message to the UI
     */
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
    
    /**
     * Make networkConsole another thread in order to avoid Android's constraint
     * that UI thread cannot handle network activity.
     */
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
      Pattern pattern = Pattern.compile("(.)\\1+");
      
      @Override
      public void afterTextChanged(Editable s) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      /**
       * Do emoji prediction
       */
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
//        sendButton.setText("Len:" + s.length());
        long ts = System.currentTimeMillis();

        String text = s.toString();
        final String[] tokens = text.split(" ");
        for ( String token : tokens ) {
          // remove repeated characters and tense suffix
          Matcher matcher = pattern.matcher(token);
          token = matcher.replaceAll("$1");
          if (token.endsWith("ies") || token.endsWith("ied")) {
            token = token.substring(0, token.length() - 3);
          }
          else if (token.endsWith("es") || token.endsWith("ed")) {
            token = token.substring(0, token.length() - 2);
          }
          else if (token.endsWith("s") || token.endsWith("d") || token.endsWith("y")) {
            token = token.substring(0, token.length() - 1);
          }
          Logger.e(token);
        }
        
        int totalNum = dbHelper.priorMap.size();
        int emojiNum = dbHelper.emojiMap.size();
        int wordNum = dbHelper.word2IdMap.size();
        Logger.e("emoji number = " + emojiNum + ", wordNum = " + wordNum);

        HashSet<Integer> wordList = new HashSet<Integer>();
        for ( String token : tokens ) {
          if (dbHelper.word2IdMap.containsKey(token)) {
            int wid = dbHelper.word2IdMap.get(token);
            wordList.add(wid);
          }
          else {
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
        Logger.e("Delay: " + (System.currentTimeMillis() - ts));
//        sendButton.setText("Delay: " + (System.currentTimeMillis() - ts));
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
      }
      
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.chat_room, menu);
    return true;
  }

}
