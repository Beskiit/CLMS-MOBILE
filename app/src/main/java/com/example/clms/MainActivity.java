package com.example.clms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseHelper dbHelper;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        dbHelper.initializeDatabase();

        // Initialize computer data if not already done
        if (!dbHelper.areComputersInitialized()) {
            dbHelper.insertInitialComputerData();
        }

        Log.d(TAG, "Database initialization completed");

        // Initialize views
        usernameEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> attemptLogin());

        // Set up signup button click listener
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        // Get input values and trim whitespace
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt login
        if (dbHelper.validateLogin(username, password)) {
            // Get user role
            String userRole = dbHelper.getUserRole(username);
            Intent intent;
            if ("Admin".equalsIgnoreCase(userRole)) {
                intent = new Intent(MainActivity.this, HomePage.class);
                intent.putExtra("ROLE", "Admin");
            } else {
                intent = new Intent(MainActivity.this, HomePage.class);
                intent.putExtra("ROLE", "Student");
            }
            
            // Pass username to next activity
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish(); // Close login activity
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
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