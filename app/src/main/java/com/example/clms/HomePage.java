package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

        // Setup lab buttons
        setupLabButtons();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup login history button
        setupLoginHistoryButton();
    }

    private void setupLabButtons() {
        LinearLayout labAButton = findViewById(R.id.labAButton);
        LinearLayout labBButton = findViewById(R.id.labBButton);
        LinearLayout labCButton = findViewById(R.id.labCButton);
        LinearLayout loginHistoryButton = findViewById(R.id.loginHistoryButton);

        labAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory A");
                startActivity(intent);
            }
        });

        labBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory B");
                startActivity(intent);
            }
        });

        labCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory C");
                startActivity(intent);
            }
        });
    }

    private void setupLoginHistoryButton() {
        LinearLayout loginHistoryButton = findViewById(R.id.loginHistoryButton);
        loginHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, LoginHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigation() {
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView issueIcon = findViewById(R.id.issueIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        // Home icon is current page, no action needed
        homeIcon.setOnClickListener(null);

        issueIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, IssueAct.class);
                startActivity(intent);
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