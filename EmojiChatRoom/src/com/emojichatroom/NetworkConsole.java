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

import com.emojicharroom.util.Config;
import com.emojicharroom.util.Logger;
import com.emojicharroom.util.MySocket;

/**
 * @author Hongyi Yao (hyyao@umich.edu)
 *
 */
public abstract class NetworkConsole implements Runnable {
  private static final int BUFFER_SIZE = 10000;
  private MySocket mySocket = null;
  private boolean isEstablished = false;
  private byte[] buffer;
  
  public abstract void handleText(String text);
  
  public NetworkConsole() {
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

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Logger.d("Network Console Receive Thread Start!");
    while ( true ) {
      this.init();

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
