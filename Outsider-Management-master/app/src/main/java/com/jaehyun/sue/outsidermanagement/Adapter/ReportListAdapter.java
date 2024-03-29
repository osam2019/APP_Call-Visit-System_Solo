package com.jaehyun.sue.outsidermanagement.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaehyun.sue.outsidermanagement.Activity.MainActivity;
import com.jaehyun.sue.outsidermanagement.R;
import com.jaehyun.sue.outsidermanagement.Utility.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportListAdapter extends BaseAdapter
{
    TextView classTextView, idTextView, nameTextView, reportDateTextView, reportContentTextView;
    LinearLayout reportItemLinearLayout, subtitleLinearLayout;

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<HashMap<String, Object>> list;
    private int layout;

    public ReportListAdapter(Context context, int layout, ArrayList<HashMap<String, Object>> list)
    {
        this.context = context;
        this.layout = layout;
        this.list = list;
    }

    @Override
    public int getCount()
    {
        return this.list.size();
    }

    @Override
    public Object getItem(int i)
    {
        return this.list.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        if( view == null )
        {
            this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = this.inflater.inflate(this.layout, viewGroup, false);
        }

        // Bind views
        this.reportItemLinearLayout = ViewHolder.get(view, R.id.report_item_linear_layout);
        this.subtitleLinearLayout = ViewHolder.get(view, R.id.subtitle_linear_layout);
        this.classTextView = ViewHolder.get(view, R.id.class_textView);
        this.idTextView = ViewHolder.get(view, R.id.id_textView);
        this.nameTextView = ViewHolder.get(view, R.id.name_textView);
        this.reportDateTextView = ViewHolder.get(view, R.id.report_date_textView);
        this.reportContentTextView = ViewHolder.get(view, R.id.report_content_textView);

        // Set attributes
        if( (boolean) MainActivity.myInfoMap.get("officer") )
        {
            if ("관심".equals(this.list.get(i).get("outsiderType").toString().trim()))
                this.reportItemLinearLayout.setBackgroundColor(Color.parseColor("#980000"));
            else if ("배려".equals(this.list.get(i).get("outsiderType").toString().trim()))
                this.reportItemLinearLayout.setBackgroundColor(Color.parseColor("#CEFBC9"));
            else if ("일반".equals(this.list.get(i).get("outsiderType").toString().trim()))
                this.reportItemLinearLayout.setBackgroundColor(Color.parseColor("#FAF4C0"));
        }

        this.reportDateTextView.setText(this.list.get(i).get("reportDate").toString().trim());
        this.reportContentTextView.setText(this.list.get(i).get("reportContent").toString().trim());

        // 로그인 아이디로 확인해야됨
        if( !(boolean) MainActivity.myInfoMap.get("officer") )        // 병이면
            this.subtitleLinearLayout.setVisibility(LinearLayout.GONE);
        else
        {
            this.subtitleLinearLayout.setVisibility(LinearLayout.VISIBLE);
            this.classTextView.setText(this.list.get(i).get("class").toString().trim());
            this.idTextView.setText(this.list.get(i).get("memberId").toString().trim());
            this.nameTextView.setText(this.list.get(i).get("name").toString().trim());
        }

        // Add Events

        return view;
    }
}
