package edu.umich.cse.yctung.surveyhalf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.survey.MainActivity;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

//==========================================================================
// 2014/04/17: this is a controller of our survey
//==========================================================================

public class SurveyController {
	final static String LOG_TAG = "EMOJI";
	final static int TOATL_QUESTION_CNT = 30;
	// UI related varialbes
	TextView mTextQuestion;
	TextView mTextInfo;
	TextView mTextGuide;
	RatingBar mSatisfactionBar;
	
	
	String demoQuestions[] = {"HAHAHHAHA", "WTFFFF!!!", "BOROROROR","XOF","kkkk","wwww"};
	
	SurveyQuestions mSurveyQuestions;
	
	// variable of a round a question/answer
	String mQuestionNow;
	int mUserId = 1; // *** this should be a uniqu random number in future ***
	int mQuestinCntNow;
	int mQIdxNow;
	int mSelectedEidNow;
	float mRatingNow;
	long mTimeQuestionStart;
	long mTimeQuestionAnswered;
	long mDurationQuestionNow;
	
	private MainActivity context; 
	
	private File survey;

	public SurveyController(TextView textQuestionIn, RatingBar satisfactionBarIn, TextView textInfoIn, TextView textGuideIn, Context context) {
		 mTextQuestion = textQuestionIn;
		 mTextGuide = textGuideIn;
		 mTextInfo = textInfoIn;
		 mSatisfactionBar = satisfactionBarIn;
		 
		 mSurveyQuestions = new SurveyQuestions(context);
		 this.context = (MainActivity) context;
//		 makeNewSurvey();
	}
	
	// initializaer for new survey
	public void makeNewSurvey(){
		mQuestinCntNow = 0;
		mTextQuestion.setVisibility(View.INVISIBLE);
//		mSatisfactionBar.setVisibility(View.INVISIBLE);
        mSatisfactionBar.setVisibility(View.GONE);
		showNextQuestion();
	}
	
	public void start(){
		
	}
	
	public static Boolean comprobarSDCard(Context mContext) {
	    String auxSDCardStatus = Environment.getExternalStorageState();

	    if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED))
	        return true;
	    else if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
	        Toast.makeText(
	                mContext,
	                "Warning, the SDCard it's only in read mode.\nthis does not result in malfunction"
	                        + " of the read aplication", Toast.LENGTH_LONG)
	                .show();
	        return true;
	    } else if (auxSDCardStatus.equals(Environment.MEDIA_NOFS)) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard can be used, it has not a corret format or "
	                        + "is not formated.", Toast.LENGTH_LONG)
	                .show();
	        return false;
	    } else if (auxSDCardStatus.equals(Environment.MEDIA_REMOVED)) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard is not found, to use the reader you need "
	                        + "insert a SDCard on the device.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    } else if (auxSDCardStatus.equals(Environment.MEDIA_SHARED)) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard is not mounted beacuse is using "
	                        + "connected by USB. Plug out and try again.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    } else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCard cant be mounted.\nThe may be happend when the SDCard is corrupted "
	                        + "or crashed.", Toast.LENGTH_LONG).show();
	        return false;
	    } else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
	        Toast.makeText(
	                mContext,
	                "Error, the SDCArd is on the device but is not mounted."
	                        + "Mount it before use the app.",
	                Toast.LENGTH_LONG).show();
	        return false;
	    }

	    return true;
	}
	
	private void showNextQuestion(){
		if(mQuestinCntNow == 0 || mQuestinCntNow == 31){ // start of a new survey
		  mQuestinCntNow = 0;
		  if (comprobarSDCard(context)) {
              com.survey.Logger.i("Create new file...");
              File sdcard = Environment.getExternalStorageDirectory();              
              File root = new File(sdcard.getPath() + "/545Survey");
              survey = new File(root.getPath() + "/survey_" + android.os.Process.myPid() + "_" + ((int)(Math.random()*1000)) + ".txt");
              
              if (!root.exists()) {
                com.survey.Logger.e("folder not exist!");
                if (!root.mkdirs()) {
                  com.survey.Logger.e("folder still not exist!");
                }
              }
              else {
                com.survey.Logger.i("folder OK");
              }
              
              if (!survey.exists()) {
                try {
                  survey.createNewFile();
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                if (!survey.exists()) {
                  com.survey.Logger.e("survey still not exist!");
                }
              }
		  }
		} else { // restore answer before make the new question
			// TODO: save answers
		  StringBuilder sb = new StringBuilder();
		  sb.append(mQuestinCntNow + " ");
		  sb.append(mQIdxNow + " ");		    
		  sb.append(mSelectedEidNow + " ");
          sb.append(mRatingNow + " ");
          sb.append(mDurationQuestionNow + " ");
          sb.append("\n");
          if (comprobarSDCard(context)) {
		    try {
		      String result = sb.toString();
//		      com.survey.Logger.i("Saving result to file...");
//		      File sdcard = Environment.getExternalStorageDirectory();
//		      if (sdcard != null) {
//		        com.survey.Logger.i("sdcard OK, path is " + sdcard.getPath());
//		      }
//		      else {
//		        com.survey.Logger.e("sdcard not exist!");
//		      }
//		      
//		      File root = new File(sdcard.getPath() + "/545Survey");
//		      File survey = new File(root.getPath() + "/survey_" + android.os.Process.myPid() + "_" + ((int)(Math.random()*1000)) + ".txt");
//		      com.survey.Logger.i("folder path is " + root.getPath());
//		      com.survey.Logger.i("file path is " + survey.getPath());
//		      
//		      if (!root.exists()) {
//		        com.survey.Logger.e("folder not exist!");
//		        if (!root.mkdirs()) {
//		          com.survey.Logger.e("folder still not exist!");
//		        }
//		      }
//		      else {
//		        com.survey.Logger.i("folder OK");
//		      }
//		      
//		      if (!survey.exists()) {
//		        survey.createNewFile();
//                if (!survey.exists()) {
//                  com.survey.Logger.e("survey still not exist!");
//                }
//		      }
//		      File survey = new File(root, "survey_" + android.os.Process.myPid() + "_" + (int)(Math.random()*100));
//              File survey = new File(root, "survey_" + android.os.Process.myPid() + ".txt");
//              File survey = new File(sdcard, "survey_" + android.os.Process.myPid() + ".txt");
              com.survey.Logger.i("file is " + survey.getName());
              FileWriter writer = new FileWriter(survey, true);
              writer.append(result);
              writer.flush();
              writer.close();
//		      BufferedOutputStream writer =
//		          new BufferedOutputStream(context.openFileOutput("results",
//		              Context.MODE_PRIVATE | Context.MODE_APPEND));
//		      result += "\n";
//		      writer.write(result.getBytes());
//		      writer.close();
		    } catch (FileNotFoundException e) {
		      com.survey.Logger.e("", e);
		    } catch (IOException e) {
		      com.survey.Logger.e("", e);
		    }
          }
		}
		mQuestinCntNow += 1;
		// TODO: randomly select the answer
		mQIdxNow = mQuestinCntNow;
        // even then use our prediction
        if ( mQuestinCntNow % 2 != 0 ) {
          Integer[] eidList = mSurveyQuestions.eidsList.get(mQIdxNow);

          context.Init_viewPager(eidList);
          context.Init_Point();
          context.Init_Data();
        }
        else {
          Integer[] eidList = context.peopleList;
          context.Init_viewPager(eidList);
          context.Init_Point();
          context.Init_Data();
        }
		
		// 1. record the time now
		//mQuestionNow = demoQuestions[mQuestinCntNow-1];
		mQuestionNow = mSurveyQuestions.qs[mQIdxNow];
		mTextQuestion.setText(mQuestionNow);
		mTextQuestion.setVisibility(View.VISIBLE);
		mTextGuide.setText("Please select the emoji that match the following sentence most");
		mTextInfo.setText(String.format("%d/%d", mQuestinCntNow, TOATL_QUESTION_CNT));
		
		mTimeQuestionStart = System.currentTimeMillis();
	}

	public void questionIsAnswered(int selectEid){
		// 1. update selected answer
		mSelectedEidNow = selectEid;
		Log.d(LOG_TAG, "selectEid = "+mSelectedEidNow);
		
		// 2. estimate the answer time
		mTimeQuestionAnswered = System.currentTimeMillis();
		mDurationQuestionNow = mTimeQuestionAnswered - mTimeQuestionStart;
		
		
		// 3. show satisfaction question
//		mTextQuestion.setVisibility(View.INVISIBLE);
        mTextQuestion.setVisibility(View.GONE);
		mSatisfactionBar.setVisibility(View.VISIBLE);
		this.mTextGuide.setText("Thanks! Please rate your selection");
	}
	
	
	public void showSatisfaction(){
		// 1. make up the satisfaction UI for selection
		
	}
	
	public void satisfactionIsAnswered(){
		// 1. record the answer of satisfaction
		mRatingNow = mSatisfactionBar.getRating();
		Log.d(LOG_TAG, "mRatingNow = "+mRatingNow);
		
		// 2. make up the next question
		mSatisfactionBar.setRating(0);
//		mSatisfactionBar.setVisibility(View.INVISIBLE);
        mSatisfactionBar.setVisibility(View.GONE);
		showNextQuestion();
	}
}
