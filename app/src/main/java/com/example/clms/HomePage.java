package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends BaseActivity {

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

        LinearLayout loginHistoryBtn = (LinearLayout) findViewById(R.id.loginHistoryButton);

        // Get username from intent
        String username = getIntent().getStringExtra("USERNAME");

        loginHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, LoginHistoryActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }

    private void setupLabButtons() {
        LinearLayout labAButton = findViewById(R.id.labAButton);
        LinearLayout labBButton = findViewById(R.id.labBButton);
        LinearLayout labCButton = findViewById(R.id.labCButton);
        LinearLayout loginHistoryButton = findViewById(R.id.loginHistoryButton);

        // Get username from intent
        String username = getIntent().getStringExtra("USERNAME");

        labAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory A");
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        labBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory B");
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        labCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, SelectPC2.class);
                intent.putExtra("LAB_NAME", "Computer Laboratory C");
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        
        // Additional HomePage-specific navigation setup
        ImageView homeIcon = findViewById(R.id.homeIcon);
        if (homeIcon != null) {
            // Home icon is current page, no action needed
            homeIcon.setOnClickListener(null);
        }
    }
}