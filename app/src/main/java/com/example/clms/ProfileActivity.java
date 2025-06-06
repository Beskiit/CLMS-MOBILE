package com.example.clms;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ImageView profileImage;
    private TextView nameTextView, courseTextView, emailTextView;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    try {
                        // Validate image type
                        String mimeType = getContentResolver().getType(imageUri);
                        Log.d("ProfileActivity", "Selected image MIME type: " + mimeType);
                        
                        if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/png"))) {
                            // Convert image to byte array
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            if (inputStream == null) {
                                Toast.makeText(this, "Failed to read image file", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Process and compress the image
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(inputStream, null, options);
                            inputStream.close();

                            // Calculate inSampleSize
                            options.inSampleSize = calculateInSampleSize(options, 400, 400);
                            options.inJustDecodeBounds = false;

                            // Decode bitmap with inSampleSize set
                            inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                            inputStream.close();

                            if (originalBitmap == null) {
                                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.d("ProfileActivity", "Original image size: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                            // Scale down the image if it's too large
                            Bitmap scaledBitmap = scaleBitmap(originalBitmap, 400);
                            Log.d("ProfileActivity", "Scaled image size: " + scaledBitmap.getWidth() + "x" + scaledBitmap.getHeight());
                            
                            // Convert to byte array with compression
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                            byte[] imageData = outputStream.toByteArray();
                            Log.d("ProfileActivity", "Compressed image size: " + imageData.length + " bytes");

                            // Check if image size is too large
                            if (imageData.length > 500000) { // 500KB limit
                                Toast.makeText(this, "Image size too large. Please choose a smaller image.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            // Update database and UI
                            if (dbHelper.updateProfilePicture(currentUsername, imageData)) {
                                // Create a copy of the bitmap for the ImageView
                                Bitmap displayBitmap = Bitmap.createBitmap(scaledBitmap);
                                profileImage.setImageBitmap(displayBitmap);
                                Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                profileImage.setImageResource(R.drawable.default_profile);
                            }

                            // Clean up
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                Log.e("ProfileActivity", "Error closing output stream", e);
                            }

                            // Clean up bitmaps
                            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                                originalBitmap.recycle();
                            }
                            if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
                                scaledBitmap.recycle();
                            }

                        } else {
                            Toast.makeText(this, "Please select a JPEG or PNG image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("ProfileActivity", "Error processing image: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ProfileActivity", "Unexpected error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    );

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean isGranted : permissions.values()) {
                allGranted = allGranted && isGranted;
            }
            if (allGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission required to access gallery", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_profile2);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            Log.d("ProfileActivity", "onCreate started");

            // Initialize views
            profileImage = findViewById(R.id.profileImage);
            nameTextView = findViewById(R.id.nameTextView);
            courseTextView = findViewById(R.id.courseTextView);
            emailTextView = findViewById(R.id.emailTextView);
            ImageButton editButton = findViewById(R.id.editButton);
            Button logoutButton = findViewById(R.id.logoutButton);

            if (profileImage == null || nameTextView == null || courseTextView == null || 
                emailTextView == null || editButton == null || logoutButton == null) {
                Log.e("ProfileActivity", "Failed to initialize one or more views");
                Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize database helper
            dbHelper = new DatabaseHelper(this);
            Log.d("ProfileActivity", "Database helper initialized");

            // Get username from intent first
            currentUsername = getIntent().getStringExtra("USERNAME");
            Log.d("ProfileActivity", "Username from intent: " + currentUsername);

            // If not in intent, try shared preferences
            if (currentUsername == null || currentUsername.isEmpty()) {
                currentUsername = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                    .getString("username", "");
                Log.d("ProfileActivity", "Username from SharedPreferences: " + currentUsername);
            }

            if (currentUsername == null || currentUsername.isEmpty()) {
                Log.e("ProfileActivity", "No username found");
                Toast.makeText(this, "Error: No username found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d("ProfileActivity", "Final username value: " + currentUsername);

            // Load user data
            loadUserProfile();

            // Set up edit button click listener
            editButton.setOnClickListener(v -> checkPermissionAndPickImage());

            // Set up logout button click listener
            logoutButton.setOnClickListener(v -> handleLogout());

            // Setup bottom navigation
            setupBottomNavigation();

            Log.d("ProfileActivity", "onCreate completed");

        } catch (Exception e) {
            Log.e("ProfileActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserProfile() {
        try {
            if (currentUsername == null || currentUsername.isEmpty()) {
                Log.e("ProfileActivity", "Username is null or empty");
                Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper.AccountData profile = dbHelper.getUserProfile(currentUsername);
            
            if (profile != null) {
                Log.d("ProfileActivity", "Profile loaded successfully");
                
                // Set name
                if (profile.name != null) {
                    nameTextView.setText(profile.name);
                    Log.d("ProfileActivity", "Set name: " + profile.name);
                } else {
                    Log.e("ProfileActivity", "Name is null");
                    nameTextView.setText("");
                }
                
                // Set course
                if (profile.course != null) {
                    courseTextView.setText(profile.course);
                    Log.d("ProfileActivity", "Set course: " + profile.course);
                } else {
                    Log.e("ProfileActivity", "Course is null");
                    courseTextView.setText("");
                }
                
                // Set email
                if (profile.email != null) {
                    emailTextView.setText(profile.email);
                    Log.d("ProfileActivity", "Set email: " + profile.email);
                } else {
                    Log.e("ProfileActivity", "Email is null");
                    emailTextView.setText("");
                }

                // Load profile picture
                if (profile.picture != null && profile.picture.length > 0) {
                    Log.d("ProfileActivity", "Profile picture data found, size: " + profile.picture.length + " bytes");
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(profile.picture, 0, profile.picture.length);
                        if (bitmap != null) {
                            profileImage.setImageBitmap(bitmap);
                            Log.d("ProfileActivity", "Successfully set profile picture");
                        } else {
                            Log.e("ProfileActivity", "Failed to decode profile picture");
                            profileImage.setImageResource(R.drawable.default_profile);
                        }
                    } catch (Exception e) {
                        Log.e("ProfileActivity", "Error loading profile picture: " + e.getMessage());
                        e.printStackTrace();
                        profileImage.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    Log.d("ProfileActivity", "No profile picture found, using default");
                    profileImage.setImageResource(R.drawable.default_profile);
                }
            } else {
                Log.e("ProfileActivity", "Failed to load user profile");
                Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                
                // Set default values
                nameTextView.setText("");
                courseTextView.setText("");
                emailTextView.setText("");
                profileImage.setImageResource(R.drawable.default_profile);
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error in loadUserProfile: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show();
            
            // Set default values
            nameTextView.setText("");
            courseTextView.setText("");
            emailTextView.setText("");
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }

    private void checkPermissionAndPickImage() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // Calculate new dimensions while maintaining aspect ratio
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            if (originalWidth > originalHeight) {
                newWidth = maxDimension;
                newHeight = (int) (originalHeight * ((float) maxDimension / originalWidth));
            } else {
                newHeight = maxDimension;
                newWidth = (int) (originalWidth * ((float) maxDimension / originalHeight));
            }
        }

        // Return original bitmap if no scaling is needed
        if (newWidth == originalWidth && newHeight == originalHeight) {
            return bitmap;
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleLogout() {
        try {
            // Get account ID
            int accountId = dbHelper.getAccountId(currentUsername);
            
            // Check for active session
            String[] activeSession = dbHelper.getActiveSession(accountId);
            if (activeSession != null) {
                // Show warning if user has active session
                new AlertDialog.Builder(this)
                    .setTitle("Active Session")
                    .setMessage("You have an active computer session. Please end your session before logging out.")
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
                return;
            }

            // Clear shared preferences
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Show success message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to login screen
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("ProfileActivity", "Error during logout: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error in onDestroy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}