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

public class PCSpinnerAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;

    public PCSpinnerAdapter(Context context, List<String> items) {
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
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.pc_spinner_item, parent, false);
        }

        String item = getItem(position);
        if (item != null) {
            TextView pcNumber = view.findViewById(R.id.pcNumber);
            TextView statusText = view.findViewById(R.id.statusText);

            String[] parts = item.split(" - ");
            pcNumber.setText(parts[0]); // PC number
            pcNumber.setTextColor(Color.BLACK);

            if (parts.length > 1) {
                String status = parts[1];
                statusText.setText(status);
                statusText.setVisibility(View.VISIBLE);

                // Set color based on status
                switch (status.toLowerCase()) {
                    case "available":
                        statusText.setTextColor(Color.parseColor("#008000")); // Green
                        break;
                    case "occupied":
                        statusText.setTextColor(Color.parseColor("#FF0000")); // Red
                        break;
                    case "unavailable":
                        statusText.setTextColor(Color.parseColor("#808080")); // Gray
                        break;
                }
            } else {
                statusText.setVisibility(View.GONE);
            }
        }

        return view;
    }
} 