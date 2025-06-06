package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity extends AppCompatActivity {
    protected DatabaseHelper dbHelper;
    protected String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void setupBottomNavigation() {
        if (username != null) {
            int accountId = dbHelper.getAccountId(username);
            String[] activeSession = dbHelper.getActiveSession(accountId);

            ImageView homeIcon = findViewById(R.id.homeIcon);
            ImageView issueIcon = findViewById(R.id.issueIcon);
            ImageView profileIcon = findViewById(R.id.profileIcon);
            ImageView returnToPCIcon = findViewById(R.id.returnToPCIcon);

            if (homeIcon != null) {
                homeIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(this, HomePage.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    if (!(this instanceof HomePage)) {
                        finish();
                    }
                });
            }

            if (issueIcon != null) {
                issueIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(this, IssueAct.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    if (this instanceof HomePage) {
                        finish();
                    }
                });
            }

            if (profileIcon != null) {
                profileIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    if (this instanceof HomePage) {
                        finish();
                    }
                });
            }

            // Show return to PC button only if there's an active session
            if (returnToPCIcon != null) {
                if (activeSession != null && !(this instanceof LoggedIn)) {
                    returnToPCIcon.setVisibility(View.VISIBLE);
                    returnToPCIcon.setOnClickListener(v -> {
                        Intent intent = new Intent(this, LoggedIn.class);
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("LAB_NAME", activeSession[0]);
                        intent.putExtra("PC_NAME", activeSession[1]);
                        intent.putExtra("ACCOUNT_ID", accountId);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    returnToPCIcon.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 