package com.comp90018.assignment2.modules.categories.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comp90018.assignment2.R;
import com.comp90018.assignment2.dto.CategoryDTO;

import java.util.ArrayList;

public class CategoryLeftAdapter extends BaseAdapter {
    private int mSelect = 0;
    private Context mContext;
    private ArrayList<CategoryDTO> categories;

    public CategoryLeftAdapter(Context mContext,  ArrayList<CategoryDTO> categories) {
        this.mContext = mContext;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_categories_item, null);
            holder = new ViewHolder();
            holder.ct_title = convertView.findViewById(R.id.ct_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ct_title.setText(categories.get(i).getName());
        if (mSelect == i) {
//            convertView.setBackgroundResource(R.drawable.item_category_item_);  //选中项背景
            holder.ct_title.setTextColor(Color.parseColor("#fd3f3f"));
        } else {
//            convertView.setBackgroundResource(R.drawable.bg2);  //其他项背景
            holder.ct_title.setTextColor(Color.parseColor("#323437"));
        }
        return convertView;

    }

    public void changeSelected(int i) {
        if (i != mSelect) {
            mSelect = i;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        private TextView ct_title;
    }
}


