package com.finalproject.brickbreaker.services;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.finalproject.brickbreaker.R;

public class CustomImageAdapter extends BaseAdapter {
    private Context mContext;

    public CustomImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 10*12;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes

            imageView = new ImageView(mContext);
            AbsListView.LayoutParams vp = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, 55);
            imageView.setLayoutParams(vp);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.brick0);
        return imageView;
    }


}

