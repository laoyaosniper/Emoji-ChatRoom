/* Copyright 2014 RobustNet Lab, University of Michigan. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emojichatroom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.emojicharroom.util.Config;
import com.emojicharroom.util.DatabaseHelper;
import com.emojicharroom.util.Logger;
import com.emojicharroom.util.MySocket;

/**
 * @author Hongyi Yao (hyyao@umich.edu)
 *
 */
public abstract class NetworkConsole implements Runnable {
  private static final int BUFFER_SIZE = 10000;
  
  private ChatRoom parent;
  private MySocket mySocket = null;
  private boolean isEstablished = false;
  private byte[] buffer;
  
  public HashMap<Integer, String> emojiMap = null;  
  public HashMap<Integer, String> wordMap = null;
  
//  public HashMap<LikelihoodEntry, Double> likelihoodMap = null;
  public HashMap<Integer, Double> likelihoodMap = null;
  
  public HashMap<Integer, Double> priorMap = null;
  
  public abstract void handleText(String text);
  
  public NetworkConsole(ChatRoom parent) {
    this.parent = parent;
    this.buffer = new byte[BUFFER_SIZE];
    this.mySocket = new MySocket();
    this.isEstablished = false;
  }
  
  private void init() {
    while ( true ) {
      try {
        mySocket.init(new Socket(InetAddress.getByName(Config.SERVER_ADDR), Config.SERVER_PORT));
        isEstablished = true;
        Logger.d("Connection established! " + InetAddress.getByName(Config.SERVER_ADDR)
          + ":" + Config.SERVER_PORT);
        break;
      } catch (UnknownHostException e) {
        Logger.e(e.toString());
        isEstablished = false;
      } catch (IOException e) {
        Logger.e("IOException: " + e.toString());
        isEstablished = false;
      }
      
      if ( !isEstablished ) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          Logger.e(e.toString() + e.getMessage());
        }
      }
    }
  }
  
  public void sendText(String text) throws IOException {
    if ( isEstablished ) {
      if ( text != null ) {
        Logger.e("Network send:" + text);
        try {
          mySocket.writer.write(text.getBytes());
          //      writer.writeUTF(text);
        } catch (IOException e) {
          Logger.e("Send failed: " + e.getMessage());
        }
      }
      else {
        Logger.e("text is null");
      }
    }
    else {
      Logger.e("Connection not established, please check network condition");
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Logger.i("Network Console Receive Thread Start!");
    
    Logger.i("Try to get emoji list and put it on screen");

    parent.dbHelper = new DatabaseHelper(parent);
    try {
      parent.dbHelper.createDataBase();
    } catch (Exception e) {
      Logger.e(e.getMessage());
    }
    parent.dbHelper.openDataBase();
    
    try {
//      emojiMap = parent.dbHelper.getAll();
      emojiMap = parent.dbHelper.getEmojiMap();
      wordMap = parent.dbHelper.getWordMap();
      priorMap = parent.dbHelper.getPriorMap();
      likelihoodMap = parent.dbHelper.getLikelihoodMap();
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      Logger.e("", e);
    }
    
//    if ( emojiMap != null ) {
//      Logger.d("Got " + emojiMap.size() + " emoji");
//      StringBuilder strBld = new StringBuilder();
//      int count = 0;
//      for ( Map.Entry<Integer, String> entry : emojiMap.entrySet() ) {
//        strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//        count++;
//        if ( count == 5 ) {
//          handleText(strBld.toString());
//          strBld = new StringBuilder();
//          count = 0;
//          break;
//        }
//      }
//    }
//    else {
//      Logger.e("EmojiMap is null");
//    }
//    
//    if ( wordMap != null ) {
//      Logger.d("Got " + wordMap.size() + " emoji");
//      StringBuilder strBld = new StringBuilder();
//      int count = 0;
//      for ( Map.Entry<Integer, String> entry : wordMap.entrySet() ) {
//        strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//        count++;
//        if ( count == 5 ) {
//          handleText(strBld.toString());
//          strBld = new StringBuilder();
//          count = 0;
//          break;
//        }
//      }
//    }
//    else {
//      Logger.e("WordMap is null");
//    }
//        
//    if ( priorMap != null ) {
//      Logger.d("Got " + priorMap.size() + " emoji");
//      StringBuilder strBld = new StringBuilder();
//      int count = 0;
//      for ( Map.Entry<Integer, Double> entry : priorMap.entrySet() ) {
//        strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//        count++;
//        if ( count == 5 ) {
//          handleText(strBld.toString());
//          strBld = new StringBuilder();
//          count = 0;
//          break;
//        }
//      }
//    }
//    else {
//      Logger.e("PriorMap is null");
//    }
    
//    if ( emojiMap != null ) {
//      Logger.d("Got " + emojiMap.size() + " emoji");
//      StringBuilder strBld = new StringBuilder();
//      int count = 0;
//      for ( Map.Entry<String, String> entry : emojiMap.entrySet() ) {
//        strBld.append(entry.getKey() + " " + entry.getValue() + " ");
//        count++;
//        if ( count == 5 ) {
//          handleText(strBld.toString());
//          strBld = new StringBuilder();
//          count = 0;
//          break;
//        }
//      }
//    }
    parent.dbHelper.close();
    
    while ( true ) {
      this.init();
      handleText("Connection Established! Let's talk");

      while ( isEstablished ) {
        try {
          int length = mySocket.reader.read(buffer);
          if ( length == -1 ) {
            throw new IOException("Connection closed");
          }
          else {
            String text = new String(buffer, 0, length);
            Logger.e("Network recv:" + text);
            handleText(text);
          }
//          String text = reader.readUTF();
        } catch (IOException e) {
          Logger.e("Receive failed: " + e.getMessage());
          isEstablished = false;
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            Logger.e(e1.toString() + e1.getMessage());
          }
        }
      }
    }

  }

}
