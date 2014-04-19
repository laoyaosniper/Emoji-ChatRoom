package com.survey;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {

  private Context context;
  private String[] texts = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "eee", "hhh", "iii"};

  public MyAdapter(Context context) {
      this.context = context;
  }

  public MyAdapter(Context context, String[] contents) {
    this.context = context;
    this.texts = contents;
  }
  public int getCount() {
      return texts.length;
  }

  public Object getItem(int position) {
      return null;
  }

  public long getItemId(int position) {
      return 0;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv;
      if (convertView == null) {
          tv = new TextView(context);
          tv.setLayoutParams(new GridView.LayoutParams(100, 100));
      }
      else {
          tv = (TextView) convertView;
      }

      tv.setTextSize(25);
          tv.setText(texts[position]);
      return tv;
  }

}