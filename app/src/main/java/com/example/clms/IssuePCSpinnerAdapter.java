package com.example.clms;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class IssuePCSpinnerAdapter extends ArrayAdapter<String> {
    public IssuePCSpinnerAdapter(Context context, List<String> items) {
        super(context, R.layout.spinner_item, items);
        setDropDownViewResource(R.layout.spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        return view;
    }
} 