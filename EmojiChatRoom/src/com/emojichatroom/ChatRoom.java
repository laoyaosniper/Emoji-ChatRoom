package com.emojichatroom;

import java.io.IOException;
import com.emojicharroom.util.Config;
import com.emojicharroom.util.DatabaseHelper;
import com.emojicharroom.util.Logger;
import android.os.Process;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
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
      textButton.setMinimumWidth(300);
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
    
    for ( int i = 0; i < 20; i++ ) {
      mHandler.obtainMessage(Config.MSG_TEXT, "HABhsf" + i).sendToTarget();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.chat_room, menu);
    return true;
  }

}
