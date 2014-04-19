package com.survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.umich.cse.yctung.surveyhalf.SurveyController;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

  private View view;
  /** 表情页的监听事件 */  
//  private OnCorpusSelectedListener mListener; 
  
  /** 显示表情页的viewpager */  
  private ViewPager vp_face;
  /** 表情页界面集合 */  
  private ArrayList<View> pageViews;  

  /** 游标显示布局 */  
  private LinearLayout layout_point;  

  /** 游标点集合 */  
  private ArrayList<ImageView> pointViews;  
  
  private int current;  

  private HashMap<Integer, String> emojiMap;
  private HashMap<String, Integer> revEmojiMap;
  
  private static final int EMOJI_PER_PAGE = 48;
  
  private Button init, people, animal, random, place, others;
  public Integer[] peopleList, animalList, randomList, placeList, othersList;

  Button mBtnMakeNewSurvey;
  Button mBtnNextQuestion;
  SurveyController mSurveyController;
  
  class MyClick implements OnClickListener{
    private Integer[] eidList;
    public MyClick(Integer[] eidList) {
      this.eidList = eidList;
    }

    @Override
    public void onClick(View v) {
      if (eidList != null) {
        Logger.e("" + eidList[0]);
        Init_viewPager(eidList);
        Init_Point();
        Init_Data();
      }
      
    }
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    /*
     * 
     */

    // connect the layout and surveyController
    TextView surveyQuestion = (TextView) findViewById(R.id.surveyQuestion);
    TextView surveyInfo = (TextView) findViewById(R.id.surveyInfo);
    TextView surveyGuide = (TextView) findViewById(R.id.surveyGuide);
    
    RatingBar satisficationBar = (RatingBar) findViewById(R.id.satisficationBar);
    Context context = this;
    mSurveyController = new SurveyController(surveyQuestion, satisficationBar, surveyInfo, surveyGuide, context);

    satisficationBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

      @Override
      public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
          mSurveyController.satisfactionIsAnswered();
        }
      }
      
    });   
    
    
    
    /**
     * 
     */
    
    this.init = (Button) findViewById(R.id.init);
    init.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Integer[] allEmoji = emojiMap.keySet().toArray(new Integer[emojiMap.keySet().size()]);
        Init_View();  
        Init_viewPager(allEmoji);  
        Init_Point();  
        Init_Data();  
        Logger.d("make new survey");
        mSurveyController.makeNewSurvey();
//        mSurveyController.showNextQuestion();
        init.setVisibility(View.GONE);
      }
      
    });
    
    this.people = (Button) findViewById(R.id.people);
    this.animal = (Button) findViewById(R.id.animal);
    this.random = (Button) findViewById(R.id.random);
    this.place = (Button) findViewById(R.id.place);
    this.others = (Button) findViewById(R.id.others);
    peopleList = new Integer[] {567,566,563,573,744,572,576,587,589,586,588,591,592,590,614,564,583,575,581,593,598,597,565,608,605,600,611,568,582,604,606,603,612,595,596,599,585,569,574,618,577,615,616,613,594,601,602,571,348,609,607,579,584,610,617,570,578,580,335,336,331,340,351,339,323,324,325,326,337,338,334,345,341,621,619,622,624,623,627,626,620,625,342,343,631,632,633,349,346,390,509,792,66,392,386,383,387,388,385,389,287,286,288,290,289,298,299,297,295,785,787,296,786,301,291,292,294,293,635,638,743,300,391,692,199,352,328,327,329,330,364,366,332,629,628,350,634,355,356,354,333,637,636,630,173,302,303,316,315,318,317,319,306,305,311,308,193,307,309,310,409,313,314,312,304,144,37,353,376,374,377,375,803,369,372,368,370,371,379,373,361,360,362,363,321,322,393,320,394};
    this.people.setOnClickListener(new MyClick(peopleList));
    animalList = new Integer[]  {277,281,272,268,280,271,279,270,263,282,278,284,269,246,276,241,275,240,247,283,262,261,259,260,258,243,236,257,250,252,251,253,235,248,249,255,254,267,274,234,227,238,223,226,228,230,232,237,239,242,244,245,224,225,273,256,233,266,265,229,231,264,285,365,75,74,83,76,78,77,84,86,85,82,81,87,73,72,70,71,68,69,79,51,65,64,61,52,53,54,55,56,57,58,59,63,62,60,48,49,50,46,47,67,816,737,772,738,766,741,795,771,35,36,43,45};
    this.animal.setOnClickListener(new MyClick(animalList));
    randomList = new Integer[] {157,378,158,162,163,159,150,151,160,161,147,344,149,148,145,155,153,154,152,156,518,169,468,469,472,412,413,410,411,408,462,739,443,444,445,446,470,471,482,481,480,492,493,447,448,727,721,726,720,491,490,487,488,489,486,382,510,479,478,484,483,485,703,702,701,699,511,513,512,680,682,384,515,514,359,358,397,401,402,404,403,400,405,463,452,450,449,784,454,453,460,456,455,457,458,459,451,442,417,416,430,423,421,422,441,424,418,419,420,414,415,781,425,427,789,788,428,429,434,436,437,438,432,433,431,439,435,494,440,516,517,461,172,176,168,171,192,185,186,189,191,190,187,188,347,178,4,184,3,182,179,203,196,769,770,194,181,204,183,777,691,690,197,202,201,195,198,205,200,167,742,136,137,143,141,142,139,140,138,135,104,103,114,106,105,112,110,119,132,118,120,108,107,109,111,133,117,116,134,113,124,129,121,123,122,146,131,125,126,127,128,130,97,98,93,94,101,90,92,102,100,91,95,99,96,115,89,88,80};
    this.random.setOnClickListener(new MyClick(randomList));
    placeList = new Integer[] {206,207,217,208,209,211,212,216,215,214,367,775,218,210,42,41,221,222,779,219,559,561,558,39,40,38,560,44,164,165,776,166,672,778,674,673,764,639,783,407,640,641,649,648,668,645,643,644,647,646,667,642,652,650,651,663,662,661,659,660,665,664,678,657,658,656,655,654,688,671,669,670,666,357,653,175,676,675,765,677,520,780,220,180,761,562,174,177,426,679};
    this.place.setOnClickListener(new MyClick(placeList));
    othersList = new Integer[] {503,506,507,812,813,811,807,504,505,508,715,714,716,717,712,713,477,732,731,532,533,718,719,711,723,722,724,725,810,809,16,473,474,475,14,18,11,12,15,467,170,20,23,25,27,26,24,33,31,32,28,22,697,695,696,698,700,686,684,8,763,683,29,30,21,728,704,706,707,705,34,821,820,10,17,13,681,502,466,685,687,689,693,694,774,793,796,798,782,794,380,19,464,465,5,6,9,7,381,762,745,746,747,748,749,750,751,752,753,754,755,756,773,519,213,406,399,398,1,2,710,819,818,501,498,497,499,500,797,817,802,799,801,800,476,545,557,534,546,535,547,536,548,537,549,538,550,539,540,541,542,543,544,551,552,553,554,555,556,791,804,805,806,757,759,758,760,395,396,790,740,496,495,808,521,522,523,734,733,736,735,729,730,530,815,814,768,767,524,525,531,526,527,528,529};
    this.others.setOnClickListener(new MyClick(othersList));
    
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    try {
      dbHelper.createDataBase();
    } catch (Exception e) {
      Logger.e(e.getMessage());
    }
    dbHelper.openDataBase();

    try {
      emojiMap = dbHelper.getEmojiMap();
      revEmojiMap = dbHelper.getRevEmojiMap();
    } catch (Exception e) {
      Logger.e("", e);
    }

  }
  /** 
   * 初始化控件 
   */  
  public void Init_View() {  
      vp_face = (ViewPager) findViewById(R.id.vp_contains);  
      layout_point = (LinearLayout) findViewById(R.id.iv_image);  
      view = findViewById(R.id.ll_facechoose);  

  }  

  /** 
   * 初始化显示表情的viewpager 
   */  
  public void Init_viewPager(Integer[] emojiList) {  
      pageViews = new ArrayList<View>();  
      // 左侧添加空页  
      View nullView1 = new View(this);  
      // 设置透明背景  
      nullView1.setBackgroundColor(Color.TRANSPARENT);  
      pageViews.add(nullView1);  

      // 中间添加表情页  

//      faceAdapters = new ArrayList<FaceAdapter>();  
//      for (int i = 0; i < emojis.size(); i++) {  
      int pageNum = emojiList.length / EMOJI_PER_PAGE + 1;
      for (int i = 0; i < pageNum; i++) {  
          GridView view = new GridView(this);
          int start = i * EMOJI_PER_PAGE;
          int end = (i+1) * EMOJI_PER_PAGE;
          if (end > emojiList.length) {
            end = emojiList.length;
          }
          String[] contents = new String[end - start];
          for (int index = 0, j = start; j < end; j++, index++) {
            contents[index] = emojiMap.get(emojiList[j]);
          }
          MyAdapter adapter = new MyAdapter(this, contents);
          view.setAdapter(adapter);
          view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
              TextView tv = (TextView) arg1;
              MainActivity.this.mSurveyController.questionIsAnswered(revEmojiMap.get(tv.getText()));
            }
          });
          
          
//          FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));  
//          view.setAdapter(adapter);  
//          faceAdapters.add(adapter);  
//          view.setOnItemClickListener(this);  
          
          view.setNumColumns(6);  
          view.setBackgroundColor(Color.TRANSPARENT);  
          view.setHorizontalSpacing(1);  
          view.setVerticalSpacing(1);  
          view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);  
          view.setCacheColorHint(0);  
          view.setPadding(5, 0, 5, 0);  
          view.setSelector(new ColorDrawable(Color.TRANSPARENT));  
          view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,  
                  LayoutParams.WRAP_CONTENT));  
          view.setGravity(Gravity.CENTER);  
          pageViews.add(view);  
      }  

      // 右侧添加空页面  
      View nullView2 = new View(this);  
      // 设置透明背景  
      nullView2.setBackgroundColor(Color.TRANSPARENT);  
      pageViews.add(nullView2);  
  }  

  /** 
   * 初始化游标 
   */  
  public void Init_Point() {  

    layout_point.removeAllViews();
      pointViews = new ArrayList<ImageView>();  
      ImageView imageView;  
      Logger.e("PageView.size=" + pageViews.size());
      for (int i = 0; i < pageViews.size(); i++) {  
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
          if (i == 0 || i == pageViews.size() - 1) {  
              imageView.setVisibility(View.GONE);  
          }  
          if (i == 1) {  
              imageView.setBackgroundResource(R.drawable.reddot);  
          }  
          pointViews.add(imageView);  

      }  
//      for (int i = 0; i < pageNum; i++) {  
//        imageView = new ImageView(this);  
//        imageView.setBackgroundResource(R.drawable.greendot);  
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  
//                new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,  
//                        LayoutParams.WRAP_CONTENT));  
//        layoutParams.leftMargin = 10;  
//        layoutParams.rightMargin = 10;  
//        layoutParams.width = 8;  
//        layoutParams.height = 8;  
//        layout_point.addView(imageView, layoutParams);  
//        if (i == 0 || i == pageNum - 1) {  
//            imageView.setVisibility(View.GONE);  
//        }  
//        if (i == 1) {  
//            imageView.setBackgroundResource(R.drawable.reddot);  
//        }  
//        pointViews.add(imageView);  
//
//    } 
  }  
  /** 
   * 绘制游标背景 
   */  
  public void draw_Point(int index) {  
      for (int i = 1; i < pointViews.size(); i++) {  
          if (index == i) {  
              pointViews.get(i).setBackgroundResource(R.drawable.reddot);  
          } else {  
              pointViews.get(i).setBackgroundResource(R.drawable.greendot);  
          }  
      }  
  }
  
  /** 
   * 填充数据 
   */  
  public void Init_Data() {  
      vp_face.setAdapter(new ViewPagerAdapter(pageViews));  

      vp_face.setCurrentItem(1);  
      current = 0;  
      vp_face.setOnPageChangeListener(new OnPageChangeListener() {  

          @Override  
          public void onPageSelected(int arg0) {  
              current = arg0 - 1;  
              // 描绘分页点  
              draw_Point(arg0);  
              // 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.  
              if (arg0 == pointViews.size() - 1 || arg0 == 0) {  
                  if (arg0 == 0) {  
                      vp_face.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.  
                      pointViews.get(1).setBackgroundResource(R.drawable.reddot);  
                  } else {  
                      vp_face.setCurrentItem(arg0 - 1);// 倒数第二屏  
                      pointViews.get(arg0 - 1).setBackgroundResource(  
                              R.drawable.reddot);  
                  }  
              }  
          }  

          @Override  
          public void onPageScrolled(int arg0, float arg1, int arg2) {  

          }  

          @Override  
          public void onPageScrollStateChanged(int arg0) {  

          }  
      });  

  }  


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}
