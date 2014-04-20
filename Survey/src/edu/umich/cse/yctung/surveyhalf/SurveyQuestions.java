package edu.umich.cse.yctung.surveyhalf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;

import android.content.Context;
//===========================================================================
// 2014/04/17: yctung: this is a function to read and mange survey questions
//===========================================================================
import android.content.res.AssetManager;
import android.util.Log;

public class SurveyQuestions {
	final static String LOG_TAG = "EMOJI";
	final static int QUESTIONS_CNT = 2000;
	final static int EMOJI_CNT = 100;
	
	int[] mids = new int[QUESTIONS_CNT];
	int[] as = new int[QUESTIONS_CNT];
	public String[] qs = new String[QUESTIONS_CNT];
	public ArrayList<Integer[]> eidsList = new ArrayList();
	
	public SurveyQuestions(Context context) {
		// 1. read data from asset
		AssetManager am = context.getAssets();
		try {
			InputStreamReader is = new InputStreamReader(am.open("survey_data_predict_reconstruct.txt")); 
			Log.d(LOG_TAG,"read asset data successfully");
			
			
			BufferedReader in = new BufferedReader(is);
			String line = null;

			StringBuilder responseData = new StringBuilder();
			int qIdx = 0;
			
			while((line = in.readLine()) != null) {
			//for(qIdx = 0; qIdx < QUESTIONS_CNT; qIdx++){	
				String []parts = line.split("\\|");
				mids[qIdx] =  Integer.parseInt(parts[0]);
				Log.d(LOG_TAG, "read qIdx = "+qIdx+", mid = "+mids[qIdx]);
				as[qIdx] = Integer.parseInt(parts[1]);
				qs[qIdx] = parts[2];
				
				String temp = parts[3].replaceAll(" ","").replaceAll("\r","").replaceAll("\n","");
				String [] eidStrings = temp.split(","); 
				
				Integer [] eids = new Integer[EMOJI_CNT];
				for(int i=0;i<EMOJI_CNT;i++){
					eids[i] = Integer.parseInt(eidStrings[i]);
				}
				
				eidsList.add(eids);
				
				qIdx ++;
				if(qIdx >= QUESTIONS_CNT){
					Log.d(LOG_TAG, "read enough questions");
					break;
				}
			}
			
			Log.d(LOG_TAG, "qs length = "+qs.length);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


