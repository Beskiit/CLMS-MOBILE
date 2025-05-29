package com.example.clms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class PCSpinnerAdapter extends ArrayAdapter<PCSpinnerAdapter.PCItem> {
    private final LayoutInflater inflater;

    public PCSpinnerAdapter(Context context, List<PCItem> items) {
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
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        PCItem item = getItem(position);
        if (item != null) {
            TextView pcNumber = view.findViewById(R.id.pcNumber);
            TextView pcStatus = view.findViewById(R.id.pcStatus);

            pcNumber.setText(item.getPcNumber());
            
            // Only show status for non-default items
            if (position == 0) {
                pcStatus.setVisibility(View.GONE);
            } else {
                pcStatus.setVisibility(View.VISIBLE);
                pcStatus.setText(item.getStatus());

                // Set status text color based on status
                int color;
                switch (item.getStatus().toLowerCase()) {
                    case "available":
                        color = 0xFF008000; // Green
                        break;
                    case "occupied":
                        color = 0xFFFF0000; // Red
                        break;
                    default:
                        color = 0xFF808080; // Gray for unavailable
                        break;
                }
                pcStatus.setTextColor(color);
            }
        }

        return view;
    }

    public static class PCItem {
        private final String pcNumber;
        private final String status;

        public PCItem(String pcNumber, String status) {
            this.pcNumber = pcNumber;
            this.status = status;
        }

        public String getPcNumber() {
            return pcNumber;
        }

        public String getStatus() {
            return status;
        }
    }
} 