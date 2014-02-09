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
package com.emojicharroom.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Hongyi Yao (hyyao@umich.edu)
 *
 */
public class MySocket {
  public Socket socket;
  public DataInputStream reader;
  public DataOutputStream writer;
  
  public void init(Socket socket) throws IOException {
    this.socket = socket;
    this.reader = new DataInputStream(socket.getInputStream());
    this.writer = new DataOutputStream(socket.getOutputStream());
  }
}
