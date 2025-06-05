package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        LinearLayout labAButton = findViewById(R.id.labAButton);
        LinearLayout labBButton = findViewById(R.id.labBButton);
        LinearLayout labCButton = findViewById(R.id.labCButton);
        LinearLayout loginHistoryButton = findViewById(R.id.loginHistoryButton);

        // Set click listeners for laboratory buttons
        labAButton.setOnClickListener(v -> openLabSelection("Computer Laboratory A"));
        labBButton.setOnClickListener(v -> openLabSelection("Computer Laboratory B"));
        labCButton.setOnClickListener(v -> openLabSelection("Computer Laboratory C"));

        // Login History button click listener
        loginHistoryButton.setOnClickListener(v -> {
            // Handle login history click
            // TODO: Implement login history functionality
        });

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void openLabSelection(String labName) {
        Intent intent = new Intent(HomePage.this, SelectPCActivity.class);
        intent.putExtra("LAB_NAME", labName);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView issueIcon = findViewById(R.id.issueIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        // Home icon is already active in this activity
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already in home, no action needed
            }
        });

        issueIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(HomePage.this, IssueAct.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HomePage.this, "Error opening Issue page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}