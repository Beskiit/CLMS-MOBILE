package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class IssueAct extends AppCompatActivity {

    private Spinner labSpinner;
    private Spinner pcSpinner;
    private EditText issueTypeInput;
    private Button addIssueButton;

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
        issueTypeInput = findViewById(R.id.issueTypeInput);
        addIssueButton = findViewById(R.id.addIssueButton);

        // Setup lab spinner
        setupLabSpinner();

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
        ArrayAdapter<String> labAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labs
        );
        labAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply custom layout for selected item and dropdown
        labAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, labs);
        labAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        labSpinner.setAdapter(labAdapter);

        // Handle lab selection
        labSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLab = parent.getItemAtPosition(position).toString();
                updatePCSpinner(selectedLab);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updatePCSpinner(String selectedLab) {
        List<String> pcs = new ArrayList<>();
        pcs.add("Select PC");

        int pcCount = 0;
        switch (selectedLab) {
            case "Computer Laboratory A":
            case "Computer Laboratory B":
                pcCount = 40;
                break;
            case "Computer Laboratory C":
                pcCount = 50;
                break;
        }

        // Add PC numbers based on the selected lab
        for (int i = 1; i <= pcCount; i++) {
            pcs.add("PC " + i);
        }

        // Create and set adapter for PC spinner with custom layout
        ArrayAdapter<String> pcAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, pcs);
        pcAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
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
}