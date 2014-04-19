package com.survey;
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


import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Hongyi Yao (hyyao@umich.edu)
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper{

  //The Android's default system path of your application database.
  String DB_PATH =null;

//  private static String DB_NAME = "instagram.sqlite";
  private static String DB_NAME = "instagram_sentence_mid_new.sqlite";

  private SQLiteDatabase myDataBase; 

  private final Context myContext;


  public HashMap<Integer, String> emojiMap;
  public HashMap<String, Integer> revEmojiMap;
  /**
   * Constructor
   * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
   * @param context
   */
  public DatabaseHelper(Context context) {

    super(context, DB_NAME, null, 1);
    this.myContext = context;
    DB_PATH="/data/data/"+context.getPackageName()+"/"+"databases/";
    Logger.e(DB_PATH);
    this.emojiMap = new HashMap<Integer, String>();
    this.revEmojiMap = new HashMap<String, Integer>();
  }   

  /**
   * Creates a empty database on the system and rewrites it with your own database.
   * @throws Exception 
   * */
  public void createDataBase() throws Exception{

    //        boolean dbExist = checkDataBase();
    boolean dbExist = false;
    if(dbExist){
      //do nothing - database already exist
    }else{

      //By calling this method and empty database will be created into the default system path
      //of your application so we are gonna be able to overwrite that database with our database.
      this.getReadableDatabase();

      try { 
        copyDataBase();

      } catch (IOException e) {

        throw new Exception("Error copying database" + e.getMessage());

      }
    }

  }

  /**
   * Check if the database already exist to avoid re-copying the file each time you open the application.
   * @return true if it exists, false if it doesn't
   */
  private boolean checkDataBase(){

    SQLiteDatabase checkDB = null;

    try{
      String myPath = DB_PATH + DB_NAME;
      checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }catch(SQLiteException e){

      //database does't exist yet.

    }

    if(checkDB != null){

      checkDB.close();

    }

    return checkDB != null ? true : false;
  }

  /**
   * Copies your database from your local assets-folder to the just created empty database in the
   * system folder, from where it can be accessed and handled.
   * This is done by transfering bytestream.
   * */
  private void copyDataBase() throws IOException{

    //Open your local db as the input stream
    InputStream myInput = myContext.getAssets().open(DB_NAME);

    // Path to the just created empty db
    String outFileName = DB_PATH + DB_NAME;

    //Open the empty db as the output stream
    OutputStream myOutput = new FileOutputStream(outFileName);

    //transfer bytes from the inputfile to the outputfile
    byte[] buffer = new byte[1024];
    int length;
    while ((length = myInput.read(buffer))>0){
      myOutput.write(buffer, 0, length);
    }

    //Close the streams
    myOutput.flush();
    myOutput.close();
    myInput.close();

  }

  public void openDataBase() throws SQLException{

    //Open the database
    String myPath = DB_PATH + DB_NAME;
    myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

  }

  @Override
  public synchronized void close() {

    if(myDataBase != null)
      myDataBase.close();

    super.close();

  }



  @Override
  public void onCreate(SQLiteDatabase db) {

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }


  public HashMap<Integer, String> getEmojiMap() {
    Logger.e(this.getReadableDatabase().getPath());
    Cursor cursor = myDataBase.query("emoji", null, null, null, null, null, null);
    int count = 0;
    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
//      int index1 = cursor.getColumnIndex("rowid");
//      int index2 = cursor.getColumnIndex("emoji");
//      if ( index1 != -1 && index2 != -1 ) {
//        Integer eid = cursor.getInt(index1);
//        String emoji = cursor.getString(index2);
//        emojiMap.put(eid, emoji);
//      }
//      else {
//        Logger.e("Not found");
//      }
      int index2 = cursor.getColumnIndex("emoji");
      if ( index2 != -1 ) {
        count++;
        Integer eid = count;
        String emoji = cursor.getString(index2);
        emojiMap.put(eid, emoji);
        revEmojiMap.put(emoji, eid);
      }
      else {
        Logger.e("Not found");
      }
    }
    return emojiMap;
  }

  public HashMap<String, Integer> getRevEmojiMap() {
    Logger.e(this.getReadableDatabase().getPath());
    
    return revEmojiMap;
  }
  
}
