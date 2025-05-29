package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectPC2 extends AppCompatActivity {
    private Spinner pcSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_pc2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the lab name and PC count from intent
        String labName = getIntent().getStringExtra("LAB_NAME");
        int pcCount = getIntent().getIntExtra("PC_COUNT", 40); // Default to 40 if not specified

        // Update the PC text to show which lab was selected
        TextView pcText = findViewById(R.id.pcText);
        pcText.setText(labName);

        // Create list of PCs with random status
        List<PCSpinnerAdapter.PCItem> pcList = new ArrayList<>();

        // Add default "Select PC" item
        pcList.add(new PCSpinnerAdapter.PCItem("Select PC", ""));

        Random random = new Random();
        String[] statuses = {"Available", "Occupied", "Unavailable"};

        for (int i = 1; i <= pcCount; i++) {
            String status = statuses[random.nextInt(statuses.length)];
            pcList.add(new PCSpinnerAdapter.PCItem("PC " + i, status));
        }

        pcSpinner = findViewById(R.id.pcSpinner);

        // Create and set the custom adapter
        PCSpinnerAdapter adapter = new PCSpinnerAdapter(this, pcList);
        pcSpinner.setAdapter(adapter);

        // Set the item selection listener
        pcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PCSpinnerAdapter.PCItem selectedItem = (PCSpinnerAdapter.PCItem) parent.getItemAtPosition(position);
                if (position == 0) {
                    // Do nothing for the default "Select PC" item
                    return;
                }
                
                if (selectedItem.getStatus().equals("Available")) {
                    // Create intent to start LoggedInActivity
                    Intent intent = new Intent(SelectPC2.this, Loggedin.class);
                    // Pass the selected PC number
                    intent.putExtra("PC_NUMBER", selectedItem.getPcNumber());
                    startActivity(intent);
                } else {
                    Toast.makeText(SelectPC2.this,
                            "This PC is " + selectedItem.getStatus(),
                            Toast.LENGTH_SHORT).show();
                    // Reset selection to "Select PC"
                    pcSpinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView issueIcon = findViewById(R.id.issueIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectPC2.this, HomePage.class);
                startActivity(intent);
                finish(); // Close this activity when returning to home
            }
        });

        issueIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectPC2.this, IssueAct.class);
                startActivity(intent);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectPC2.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}