package com.example.clms;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class IssuePCSpinnerAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;

    public IssuePCSpinnerAdapter(Context context, List<String> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            // Don't show the "Select PC" option in the dropdown
            return new View(getContext());
        }
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        String item = getItem(position);
        if (item != null) {
            TextView pcNumber = view.findViewById(R.id.pcNumber);
            TextView pcStatus = view.findViewById(R.id.pcStatus);

            pcNumber.setText(item);
            
            // Only show status for non-default items
            if (position == 0) {
                pcStatus.setVisibility(View.GONE);
            } else {
                pcStatus.setVisibility(View.GONE); // Hide status in Issue activity
            }
        }

        return view;
    }
} 