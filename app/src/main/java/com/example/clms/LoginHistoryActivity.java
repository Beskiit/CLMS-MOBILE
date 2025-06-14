package com.example.clms;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class LoginHistoryActivity extends BaseActivity {
    private static final String TAG = "LoginHistoryActivity";
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history2);

        try {
            // Initialize database helper
            dbHelper = new DatabaseHelper(this);

            // Get username from intent
            username = getIntent().getStringExtra("USERNAME");
            if (username == null || username.isEmpty()) {
                Log.e(TAG, "Username not found in intent");
                Toast.makeText(this, "Error: Username not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set up bottom navigation
            setupBottomNavigation();

            // Display login history
            displayLoginHistory();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing the activity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void setupBottomNavigation() {
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView issueIcon = findViewById(R.id.issueIcon);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHistoryActivity.this, HomePage.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            }
        });

        issueIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHistoryActivity.this, IssueAct.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHistoryActivity.this, ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }

    private void displayLoginHistory() {
        try {
            int accountId = dbHelper.getAccountId(username);
            if (accountId != -1) {
                TableLayout historyTable = findViewById(R.id.historyTable);
                if (historyTable == null) {
                    Log.e(TAG, "History table not found");
                    return;
                }
                
                // Clear existing rows except header
                int childCount = historyTable.getChildCount();
                if (childCount > 1) {
                    historyTable.removeViews(1, childCount - 1);
                }

                Cursor cursor = dbHelper.getLoginHistory(accountId);
                if (cursor != null) {
                    try {
                        // Get column indices
                        int labIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPUTER_LABORATORY);
                        int pcIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PC_NAME);
                        int loginIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOGGED_IN);
                        int logoutIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOGGED_OUT);
                        int statusIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_STATUS);

                        while (cursor.moveToNext()) {
                            TableRow row = new TableRow(this);
                            // Set alternating row backgrounds
                            row.setBackgroundResource(cursor.getPosition() % 2 == 0 ? 
                                android.R.color.white : R.color.light_gray);
                            row.setPadding(0, 8, 0, 8);

                            // Add cells to the row
                            addCell(row, cursor.getString(labIndex));
                            addCell(row, cursor.getString(pcIndex));
                            addCell(row, cursor.getString(loginIndex));
                            String loggedOut = cursor.getString(logoutIndex);
                            addCell(row, loggedOut != null ? loggedOut : "Active");
                            addCell(row, cursor.getString(statusIndex));

                            historyTable.addView(row);
                        }
                    } finally {
                        cursor.close();
                    }
                } else {
                    Log.e(TAG, "Login history cursor is null");
                }
            } else {
                Log.e(TAG, "Account ID not found for username: " + username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying login history", e);
            Toast.makeText(this, "Error displaying login history", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCell(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text != null ? text : "");
        textView.setPadding(10, 10, 10, 10);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(textView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}