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

public class SelectPC2 extends AppCompatActivity {
    private Spinner pcSpinner;
    private DatabaseHelper dbHelper;

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

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get the lab name from intent
        String labName = getIntent().getStringExtra("LAB_NAME");

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

        // Set the item selection listener
        pcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if (position == 0) {
                    // Do nothing for the default "Select PC" item
                    return;
                }
                
                String[] parts = selectedItem.split(" - ");
                String pcNumber = parts[0];
                String status = parts.length > 1 ? parts[1] : "";
                
                if (status.equals("Available")) {
                    // Create intent to start LoggedInActivity
                    Intent intent = new Intent(SelectPC2.this, Loggedin.class);
                    // Pass the selected PC number and lab name
                    intent.putExtra("PC_NUMBER", pcNumber);
                    intent.putExtra("LAB_NAME", labName);
                    startActivity(intent);
                } else {
                    Toast.makeText(SelectPC2.this,
                            "This PC is " + status,
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}