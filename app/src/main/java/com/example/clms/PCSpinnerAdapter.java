package com.example.clms;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class PCSpinnerAdapter extends ArrayAdapter<String> {
    public PCSpinnerAdapter(Context context, List<String> items) {
        super(context, R.layout.spinner_item, items);
        setDropDownViewResource(R.layout.spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        setTextColor(textView, getItem(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        setTextColor(textView, getItem(position));
        return view;
    }

    private void setTextColor(TextView textView, String item) {
        if (item == null || item.equals("Select PC") || item.equals("Select Laboratory")) {
            textView.setTextColor(Color.parseColor("#333333"));
            return;
        }

        // Extract status from the item string (format: "PCX - Status")
        String status = item.contains(" - ") ? item.split(" - ")[1] : item;

        switch (status) {
            case "Available":
                textView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "Occupied":
                textView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            case "Unavailable":
                textView.setTextColor(Color.parseColor("#9E9E9E")); // Gray
                break;
            default:
                textView.setTextColor(Color.parseColor("#333333")); // Default dark gray
                break;
        }
    }
} 