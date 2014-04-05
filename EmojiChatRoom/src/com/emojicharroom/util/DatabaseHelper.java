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
  public HashMap<Integer, String> wordMap;
  public HashMap<String, Integer> word2IdMap;
  
//  public HashMap<LikelihoodEntry, Double> likelihoodMap;
  public HashMap<Integer, Double> likelihoodMap;
  
  public HashMap<Integer, Double> priorMap;
  public HashMap<Integer, Double> precomputeMap; 
  public HashMap<Integer, ArrayList<Integer>> zeroMap;
  public double minValue;
  /**
   * Constructor
   * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
   * @param context
   */
  public DatabaseHelper(Context context) {

    super(context, DB_NAME, null, 1);
    this.myContext = context;
    DB_PATH="/data/data/"+context.getPackageName()+"/"+"databases/";
    this.emojiMap = new HashMap<Integer, String>();
    
    this.wordMap = new HashMap<Integer, String>();
    this.word2IdMap = new HashMap<String, Integer>();
    
//    this.likelihoodMap = new HashMap<LikelihoodEntry, Double>();
    this.likelihoodMap = new HashMap<Integer, Double>();
    
    this.priorMap = new HashMap<Integer, Double>();
    this.precomputeMap = new HashMap<Integer, Double>();
    this.zeroMap = new HashMap<Integer, ArrayList<Integer>>();
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
  //return cursor
  public Cursor query(String table,String[] columns, String selection,String[] selectionArgs,String groupBy,String having,String orderBy){
    return myDataBase.query("EMP_TABLE", null, null, null, null, null, null);


  }

//  public HashMap<String, String> getAll() {
//    Logger.e(this.getReadableDatabase().getPath());
//    Cursor cursor = myDataBase.query("emoji_text_mapping", null, null, null, null, null, null);
//    int count = 0;
//    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
//      int index1 = cursor.getColumnIndex("eid");
//      int index2 = cursor.getColumnIndex("emoji");
//      if ( index1 != -1 && index2 != -1 ) {
//        String eid = cursor.getInteger(index1);
//        String emoji = cursor.getString(index2);
//        emojiMap.put(eid, emoji);
//      }
//      else {
//        Logger.e("Not found");
//      }
//      
////      if ( count > 5 ) {
////        break;
////      }
////      int eid = -1, wid = -1;
////      double freq = 0.0;
////      int index = cursor.getColumnIndex("eid");
////      if ( index != -1 ) {
////        eid = cursor.getInt(index);
////      }
////      index = cursor.getColumnIndex("wid");
////      if ( index != -1 ) {
////        wid = cursor.getInt(index);
////      }
////      index = cursor.getColumnIndex("p");
////      if ( index != -1 ) {
////        freq = cursor.getDouble(index);
////      }
////      Logger.e("eid " + eid + " wid " + wid + "freq " + freq);
//    }
//    return emojiMap;
//  }


  public HashMap<Integer, String> getEmojiMap() {
    Logger.e(this.getReadableDatabase().getPath());
    Cursor cursor = myDataBase.query("emoji_text_mapping", null, null, null, null, null, null);
    int count = 0;
    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
      int index1 = cursor.getColumnIndex("eid");
      int index2 = cursor.getColumnIndex("emoji");
      if ( index1 != -1 && index2 != -1 ) {
        Integer eid = cursor.getInt(index1);
        String emoji = cursor.getString(index2);
        emojiMap.put(eid, emoji);
      }
      else {
        Logger.e("Not found");
      }
    }
    return emojiMap;
  }
  
  public HashMap<Integer, String> getWordMap() {
    Logger.e(this.getReadableDatabase().getPath());
    Cursor cursor = myDataBase.query("word", null, null, null, null, null, null);
    int count = 0;
    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
      int index1 = cursor.getColumnIndex("wid");
      int index2 = cursor.getColumnIndex("word");
      if ( index1 != -1 && index2 != -1 ) {
        Integer wid = cursor.getInt(index1);
        String word = cursor.getString(index2);
        wordMap.put(wid, word);
        word2IdMap.put(word, wid);
      }
      else {
        Logger.e("Not found");
      }
    }
    return wordMap;
  }

//  public HashMap<LikelihoodEntry, Double> getLikelihoodMap() {
  public HashMap<Integer, Double> getLikelihoodMap() {
    Logger.e(this.getReadableDatabase().getPath());
    Cursor cursor = myDataBase.query("naive", null, null, null, null, null, null);
    int count = 0;
    minValue = 10;
    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
      int index1 = cursor.getColumnIndex("eid");
      int index2 = cursor.getColumnIndex("wid");
      int index3 = cursor.getColumnIndex("p");
      
      if ( index1 != -1 && index2 != -1 && index3 != -1 ) {
        Integer eid = cursor.getInt(index1);
        Integer wid = cursor.getInt(index2);
        Double p = cursor.getDouble(index3);
        likelihoodMap.put(eid*65536+wid, p);
        if (p < minValue) {
          minValue = p;
        }

        if ( Math.abs(p - 1.0) > 1e-7 ) {
          double prob = precomputeMap.get(eid);
          precomputeMap.put(eid, prob * (1 - p));
        }
        else {
          // p is 1, 1 - p is 0, put into zero map
          if (!zeroMap.containsKey(eid)) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            zeroMap.put(eid, list);
          }
          zeroMap.get(eid).add(wid);
        }
      }
      else {
        Logger.e("Not found");
      }
    }
    minValue /= 10;
//    Logger.e("min p = " + minValue);
//    // smooth the likelihoodMap
//    for (Map.Entry<Integer, Double> entry : likelihoodMap.entrySet()) {
//      if (Math.abs(entry.getValue() - 0.0) < 1e-7) {
//        likelihoodMap.put(entry.getKey(), minValue/10);
//        int mix = entry.getKey();
//        int eid = mix / 65536;
//        int wid = mix % 65536;
//        Logger.e("<" + eid + "," + wid + "> = " + likelihoodMap.get(entry.getKey()));
//      }
//    }
//    // Validate the likelihoodMap
//    for (Map.Entry<Integer, Double> entry : likelihoodMap.entrySet()) {
//      if (entry.getValue() == 0) {
//        int mix = entry.getKey();
//        int eid = mix / 65536;
//        int wid = mix % 65536;
//        Logger.e("<" + eid + "," + wid + "> = 0");
//      }
//    }
    
    return likelihoodMap;
  }
  
  public HashMap<Integer, Double> getPriorMap() {
    Logger.e(this.getReadableDatabase().getPath());
    Cursor cursor = myDataBase.query("emoji_prob", null, null, null, null, null, null);
    int count = 0;
    for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
      int index1 = cursor.getColumnIndex("eid");
      int index2 = cursor.getColumnIndex("p");
      
      if ( index1 != -1 && index2 != -1 ) {
        Integer eid = cursor.getInt(index1);
        Double p = cursor.getDouble(index2);
        priorMap.put(eid, p);
        precomputeMap.put(eid, p);
      }
      else {
        Logger.e("Not found");
      }
    }
    return priorMap;
  }
  
  public HashMap<Integer, Double> getPrecomputeMap() {
    Logger.e(this.getReadableDatabase().getPath());
    return precomputeMap;
  }
}
