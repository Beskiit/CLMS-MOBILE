package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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


        // Find all lab buttons
        LinearLayout labA = findViewById(R.id.labAButton);
        LinearLayout labB = findViewById(R.id.labBButton);
        LinearLayout labC = findViewById(R.id.labCButton);
        LinearLayout loginHistory = findViewById(R.id.loginHistoryButton);

        loginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, LoginHistoryActivity.class);
                startActivity(intent);
            }
        });

        // Set click listeners for each lab
        labA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectPC("LAB A", 40);
            }
        });

        labB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectPC("LAB B", 40);
            }
        });

        labC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectPC("LAB C", 50);
            }
        });

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void openSelectPC(String labName, int pcCount) {
        Intent intent = new Intent(this, SelectPC2.class);
        intent.putExtra("LAB_NAME", labName);
        intent.putExtra("PC_COUNT", pcCount);
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