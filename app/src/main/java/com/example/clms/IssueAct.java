package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.List;

public class IssueAct extends BaseActivity {
    private static final String TAG = "IssueAct";
    private Spinner labSpinner;
    private Spinner pcSpinner;
    private EditText issueDescription;
    private Button submitButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_issue2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        labSpinner = findViewById(R.id.labSpinner);
        pcSpinner = findViewById(R.id.pcSpinner);
        issueDescription = findViewById(R.id.issueDescription);
        submitButton = findViewById(R.id.addIssueButton);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Setup spinners
        setupLabSpinner();
        setupPCSpinner();

        // Setup submit button
        setupSubmitButton();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupLabSpinner() {
        // Create lab options
        List<String> labs = new ArrayList<>();
        labs.add("Select Laboratory");
        labs.add("Computer Laboratory A");
        labs.add("Computer Laboratory B");
        labs.add("Computer Laboratory C");

        // Create and set adapter for lab spinner with custom layout
        PCSpinnerAdapter labAdapter = new PCSpinnerAdapter(this, labs);
        labSpinner.setAdapter(labAdapter);

        // Handle lab selection
        labSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLab = parent.getItemAtPosition(position).toString();
                if (!selectedLab.equals("Select Laboratory")) {
                    updatePCSpinner(selectedLab);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupPCSpinner() {
        List<String> initialList = new ArrayList<>();
        initialList.add("Select PC");
        PCSpinnerAdapter initialAdapter = new PCSpinnerAdapter(this, initialList);
        pcSpinner.setAdapter(initialAdapter);
    }

    private void updatePCSpinner(String selectedLab) {
        // Get computers for the selected lab from database
        List<String> computers = dbHelper.getComputersByLab(selectedLab);
        
        // Add "Select PC" as first option
        List<String> pcList = new ArrayList<>();
        pcList.add("Select PC");
        pcList.addAll(computers);

        // Create and set adapter for PC spinner
        PCSpinnerAdapter pcAdapter = new PCSpinnerAdapter(this, pcList);
        pcSpinner.setAdapter(pcAdapter);
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleAddIssue());
    }

    @Override
    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        
        // Additional IssueAct-specific navigation setup
        ImageView issueIcon = findViewById(R.id.issueIcon);
        if (issueIcon != null) {
            // Issue icon is current page, no action needed
            issueIcon.setOnClickListener(null);
        }
    }

    private void handleAddIssue() {
        try {
            // Get selected lab
            String selectedLab = labSpinner.getSelectedItem().toString();
            if (selectedLab.equals("Select Laboratory")) {
                Toast.makeText(this, "Please select a laboratory", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected PC
            String selectedPC = pcSpinner.getSelectedItem().toString();
            if (selectedPC.equals("Select PC")) {
                Toast.makeText(this, "Please select a PC", Toast.LENGTH_SHORT).show();
                return;
            }

            // Clean up PC string to get just the unit number and status
            String[] parts = selectedPC.split(" - ");
            String pcUnit = parts[0];
            String pcStatus = parts[1];

            // Check if PC is occupied
            if (pcStatus.equals("Occupied")) {
                Toast.makeText(this, "Cannot report issue for occupied PC. Please wait until the PC is available.", Toast.LENGTH_LONG).show();
                return;
            }

            // Get issue description
            String description = issueDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter issue description", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get account ID
            int accountId = dbHelper.getAccountId(username);
            if (accountId == -1) {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add issue to database
            if (dbHelper.addIssue(pcUnit, selectedLab, description, accountId)) {
                Toast.makeText(this, "Issue reported successfully", Toast.LENGTH_SHORT).show();
                // Clear inputs
                labSpinner.setSelection(0);
                setupPCSpinner(); // Reset PC spinner
                issueDescription.setText("");
            } else {
                Toast.makeText(this, "Failed to report issue", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling issue submission", e);
            Toast.makeText(this, "Error submitting issue", Toast.LENGTH_SHORT).show();
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