package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class IssueAct extends AppCompatActivity {
    private Spinner labSpinner;
    private Spinner pcSpinner;
    private EditText issueTypeInput;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue2);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        labSpinner = findViewById(R.id.labSpinner);
        pcSpinner = findViewById(R.id.pcSpinner);
        issueTypeInput = findViewById(R.id.issueTypeInput);

        // Setup spinners
        setupLabSpinner();
        setupBottomNavigation();

        // Setup add issue button
        findViewById(R.id.addIssueButton).setOnClickListener(v -> handleAddIssue());
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

    private void setupBottomNavigation() {
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView issueIcon = findViewById(R.id.issueIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IssueAct.this, HomePage.class);
                startActivity(intent);
                finish();
            }
        });

        // Issue icon is already active in this activity
        issueIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already in issue activity
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IssueAct.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleAddIssue() {
        // Get selected values
        String selectedLab = labSpinner.getSelectedItem().toString();
        String selectedPC = pcSpinner.getSelectedItem().toString();
        String issueDescription = issueTypeInput.getText().toString().trim();

        // Validate inputs
        if (selectedLab.equals("Select Laboratory") || 
            selectedPC.equals("Select PC") || 
            issueDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract PC unit from the selected PC (remove status part)
        String pcUnit = selectedPC.split(" - ")[0];

        // TODO: Get the actual account ID of the logged-in user
        int reportedBy = 1; // Temporary default value

        // Add the issue to database
        boolean success = dbHelper.addIssue(pcUnit, selectedLab, issueDescription, reportedBy);

        if (success) {
            Toast.makeText(this, "Issue reported successfully", Toast.LENGTH_SHORT).show();
            // Reset form
            labSpinner.setSelection(0);
            pcSpinner.setSelection(0);
            issueTypeInput.setText("");
        } else {
            Toast.makeText(this, "Failed to report issue", Toast.LENGTH_SHORT).show();
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