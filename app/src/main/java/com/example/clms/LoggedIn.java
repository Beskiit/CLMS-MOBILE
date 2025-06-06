package com.example.clms;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoggedIn extends BaseActivity {
    private static final String TAG = "LoggedIn";
    private String username;
    private String labName;
    private String pcName;
    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loggedin2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            // Get data from intent
            Intent intent = getIntent();
            username = intent.getStringExtra("USERNAME");
            labName = intent.getStringExtra("LAB_NAME");
            pcName = intent.getStringExtra("PC_NAME");
            accountId = intent.getIntExtra("ACCOUNT_ID", -1);

            Log.d(TAG, "Received intent extras - Username: " + username + ", Lab: " + labName + ", PC: " + pcName + ", AccountID: " + accountId);

            // Validate intent extras
            if (username == null || labName == null || pcName == null || accountId == -1) {
                Log.e(TAG, "Missing required intent extras");
                Toast.makeText(this, "Error: Missing required information", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set up the current session info
            TextView currentSessionInfo = findViewById(R.id.currentSessionInfo);
            if (currentSessionInfo != null) {
                currentSessionInfo.setText(pcName);
            } else {
                Log.e(TAG, "currentSessionInfo TextView not found");
            }

            // Set up logout button
            ImageView logoutButton = findViewById(R.id.logoutButton);
            if (logoutButton != null) {
                logoutButton.setOnClickListener(v -> handleLogout());
            } else {
                Log.e(TAG, "logoutButton not found");
            }

            // Set up bottom navigation
            setupBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing the activity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void setupBottomNavigation() {
        super.setupBottomNavigation();
        
        // Since we're in the LoggedIn activity, we don't need the return to PC button
        ImageView returnToPCIcon = findViewById(R.id.returnToPCIcon);
        if (returnToPCIcon != null) {
            returnToPCIcon.setVisibility(View.GONE);
        }
    }

    private void handleLogout() {
        try {
            if (dbHelper.logOutFromPC(accountId, labName, pcName)) {
                Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                // Return to HomePage
                Intent intent = new Intent(LoggedIn.this, HomePage.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to log out", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
        }
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