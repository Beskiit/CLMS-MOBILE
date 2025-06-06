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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class SelectPC2 extends BaseActivity {
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

        // Get the lab name and username from intent
        String labName = getIntent().getStringExtra("LAB_NAME");
        username = getIntent().getStringExtra("USERNAME");

        if (labName == null || labName.isEmpty()) {
            Toast.makeText(this, "Error: Laboratory name not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: Username not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Update the PC text to show which lab was selected
        TextView pcText = findViewById(R.id.labNameTextView);
        pcText.setText(labName);

        // Get computers from database
        List<String> pcList = new ArrayList<>();
        pcList.add("Select PC"); // Add default selection
        pcList.addAll(dbHelper.getComputersByLab(labName));

        pcSpinner = findViewById(R.id.computerSpinner);

        // Create and set the custom adapter
        PCSpinnerAdapter adapter = new PCSpinnerAdapter(this, pcList);
        pcSpinner.setAdapter(adapter);

        // Handle PC selection
        pcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPC = parent.getItemAtPosition(position).toString();
                if (!selectedPC.equals("Select PC")) {
                    // Extract PC unit and status
                    String[] parts = selectedPC.split(" - ");
                    String pcUnit = parts[0];
                    
                    // Verify current PC status
                    String currentStatus = dbHelper.getComputerStatus(labName, pcUnit);
                    if (currentStatus == null) {
                        Toast.makeText(SelectPC2.this, "Error: Could not verify PC status", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("Available".equals(currentStatus)) {
                        int accountId = dbHelper.getAccountId(username);
                        if (accountId != -1) {
                            // First insert login history and update PC status
                            if (dbHelper.loginToPC(accountId, labName, pcUnit)) {
                                // If login successful, redirect to LoggedIn activity
                                Intent intent = new Intent(SelectPC2.this, LoggedIn.class);
                                intent.putExtra("USERNAME", username);
                                intent.putExtra("LAB_NAME", labName);
                                intent.putExtra("PC_NAME", pcUnit);
                                intent.putExtra("ACCOUNT_ID", accountId);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SelectPC2.this, "Failed to log in to PC. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SelectPC2.this, "Error: User account not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SelectPC2.this, "This PC is currently " + currentStatus, Toast.LENGTH_SHORT).show();
                    }
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

    @Override
    protected void setupBottomNavigation() {
        super.setupBottomNavigation();

        // Get account ID
        int accountId = dbHelper.getAccountId(username);
        
        // Check if user has an active session
        String[] activeSession = dbHelper.getActiveSession(accountId);
        
        // Find the returnToPCIcon
        ImageView returnToPCIcon = findViewById(R.id.returnToPCIcon);
        
        // Only show the returnToPCIcon if there's an active session
        if (returnToPCIcon != null) {
            if (activeSession != null && activeSession[0] != null && activeSession[1] != null) {
                returnToPCIcon.setVisibility(View.VISIBLE);
            } else {
                returnToPCIcon.setVisibility(View.GONE);
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