package com.example.clms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Base64;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "CLMS.db";
    private static final int DATABASE_VERSION = 2;
    private final Context context;

    // Table Names
    private static final String TABLE_ACCOUNT = "Account";
    private static final String TABLE_COMPUTER = "Computer";
    private static final String TABLE_LOGIN_HISTORY = "Login_History";
    private static final String TABLE_ACTIVITY_LOG = "Activity_Log";
    private static final String TABLE_ISSUE = "Issue";

    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ACCOUNT_ID = "account_id";
    private static final String COLUMN_COMPUTER_ID = "computer_id";

    // Account Table Columns
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_COURSE = "course";
    private static final String COLUMN_ACCOUNT_CREATED = "account_created";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_IS_ARCHIVED = "isArchived";

    // Computer Table Columns
    private static final String COLUMN_LABORATORY_NAME = "laboratory_name";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_UNIT = "unit";

    // Login History Table Columns
    private static final String COLUMN_LOG_STATUS = "log_status";
    private static final String COLUMN_LOGIN_DATE = "login_date";

    // Activity Log Table Columns
    private static final String COLUMN_ACTIVITY_NAME = "activity_name";
    private static final String COLUMN_DATE_TIME = "date_time";

    // Issue Table Columns
    private static final String COLUMN_REPORTED_BY = "reported_by";
    private static final String COLUMN_ISSUE_DESCRIPTION = "issue_description";
    private static final String COLUMN_DATE_REPORTED = "date_reported";

    // Create Table Statements
    private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + TABLE_ACCOUNT + "("
            + COLUMN_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_COURSE + " TEXT,"
            + COLUMN_ACCOUNT_CREATED + " DATETIME,"
            + COLUMN_ROLE + " TEXT,"
            + COLUMN_IS_ARCHIVED + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_COMPUTER + "("
            + COLUMN_COMPUTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACCOUNT_ID + " INTEGER,"
            + COLUMN_LABORATORY_NAME + " TEXT,"
            + COLUMN_STATUS + " TEXT,"
            + COLUMN_UNIT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + ")"
            + ")";

    private static final String CREATE_TABLE_LOGIN_HISTORY = "CREATE TABLE " + TABLE_LOGIN_HISTORY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACCOUNT_ID + " INTEGER,"
            + COLUMN_COMPUTER_ID + " INTEGER,"
            + COLUMN_LOG_STATUS + " TEXT,"
            + COLUMN_LOGIN_DATE + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + "),"
            + "FOREIGN KEY(" + COLUMN_COMPUTER_ID + ") REFERENCES " + TABLE_COMPUTER + "(" + COLUMN_COMPUTER_ID + ")"
            + ")";

    private static final String CREATE_TABLE_ACTIVITY_LOG = "CREATE TABLE " + TABLE_ACTIVITY_LOG + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACTIVITY_NAME + " TEXT,"
            + COLUMN_NAME + " INTEGER,"
            + COLUMN_DATE_TIME + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_NAME + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + ")"
            + ")";

    private static final String CREATE_TABLE_ISSUE = "CREATE TABLE " + TABLE_ISSUE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_REPORTED_BY + " INTEGER,"
            + COLUMN_ISSUE_DESCRIPTION + " TEXT,"
            + COLUMN_COMPUTER_ID + " INTEGER,"
            + COLUMN_STATUS + " TEXT,"
            + COLUMN_DATE_REPORTED + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_REPORTED_BY + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + "),"
            + "FOREIGN KEY(" + COLUMN_COMPUTER_ID + ") REFERENCES " + TABLE_COMPUTER + "(" + COLUMN_COMPUTER_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        verifyDatabaseExists();
    }

    private void verifyDatabaseExists() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        Log.d(TAG, "Database path: " + dbFile.getAbsolutePath());
        if (dbFile.exists()) {
            Log.d(TAG, "Database exists at: " + dbFile.getAbsolutePath());
        } else {
            Log.d(TAG, "Database does not exist yet. It will be created when first accessed.");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        try {
            // Enable foreign key support
            db.execSQL("PRAGMA foreign_keys = ON;");
            
            // Creating required tables
            db.execSQL(CREATE_TABLE_ACCOUNT);
            Log.d(TAG, "Account table created successfully");
            
            db.execSQL(CREATE_TABLE_COMPUTER);
            Log.d(TAG, "Computer table created successfully");
            
            db.execSQL(CREATE_TABLE_LOGIN_HISTORY);
            Log.d(TAG, "Login History table created successfully");
            
            db.execSQL(CREATE_TABLE_ACTIVITY_LOG);
            Log.d(TAG, "Activity Log table created successfully");
            
            db.execSQL(CREATE_TABLE_ISSUE);
            Log.d(TAG, "Issue table created successfully");
            
            Log.d(TAG, "All tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables", e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            // On upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ISSUE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_LOG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);

            // Create new tables
            onCreate(db);
            Log.d(TAG, "Database upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            throw e;
        }
    }

    // Method to force database creation
    public void initializeDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            Log.d(TAG, "Database initialized successfully");
            db.close();
        }
    }

    public boolean registerAccount(String name, String course, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Hash the password using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            String hashedPassword = Base64.encodeToString(hash, Base64.DEFAULT);

            // Get current timestamp
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            // Prepare values
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_COURSE, course);
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, hashedPassword);
            values.put(COLUMN_ROLE, "Student");  // Default role
            values.put(COLUMN_IS_ARCHIVED, 0);   // Not archived by default
            values.put(COLUMN_ACCOUNT_CREATED, currentDateTime);

            // Insert the record
            long result = db.insert(TABLE_ACCOUNT, null, values);
            return result != -1;

        } catch (Exception e) {
            Log.e(TAG, "Error in registration", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Check if username already exists
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCOUNT, 
            new String[]{COLUMN_ACCOUNT_ID}, 
            COLUMN_USERNAME + "=?", 
            new String[]{username}, 
            null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean validateLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            // Hash the input password for comparison
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            String hashedPassword = Base64.encodeToString(hash, Base64.DEFAULT);

            // Query to check username and password
            String[] columns = {COLUMN_ACCOUNT_ID};
            String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=? AND " + COLUMN_IS_ARCHIVED + "=?";
            String[] selectionArgs = {username, hashedPassword, "0"};

            Cursor cursor = db.query(
                TABLE_ACCOUNT,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;

        } catch (Exception e) {
            Log.e(TAG, "Error validating login", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Get user role for the logged-in user
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String role = "";
        
        try {
            String[] columns = {COLUMN_ROLE};
            String selection = COLUMN_USERNAME + "=?";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(
                TABLE_ACCOUNT,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_ROLE);
                if (columnIndex != -1) {
                    role = cursor.getString(columnIndex);
                } else {
                    Log.e(TAG, "COLUMN_ROLE not found in cursor");
                }
            }
            cursor.close();
            return role;

        } catch (Exception e) {
            Log.e(TAG, "Error getting user role", e);
            return "";
        } finally {
            db.close();
        }
    }

    public void insertInitialComputerData() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            // Get the first account_id from Account table
            Cursor cursor = db.query(
                TABLE_ACCOUNT,
                new String[]{COLUMN_ACCOUNT_ID},
                null,
                null,
                null,
                null,
                COLUMN_ACCOUNT_ID + " ASC",
                "1"
            );

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_ACCOUNT_ID);
                if (columnIndex != -1) {
                    int accountId = cursor.getInt(columnIndex);
                    cursor.close();

                    // Begin transaction for better performance
                    db.beginTransaction();

                    try {
                        // Insert computers for Laboratory A (40 computers)
                        insertComputersForLab(db, "Computer Laboratory A", 40, accountId);

                        // Insert computers for Laboratory B (40 computers)
                        insertComputersForLab(db, "Computer Laboratory B", 40, accountId);

                        // Insert computers for Laboratory C (50 computers)
                        insertComputersForLab(db, "Computer Laboratory C", 50, accountId);

                        // Mark transaction as successful
                        db.setTransactionSuccessful();
                        
                        Log.d(TAG, "Successfully inserted all computer data");
                    } finally {
                        // End transaction
                        db.endTransaction();
                    }
                } else {
                    Log.e(TAG, "COLUMN_ACCOUNT_ID not found in cursor");
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting computer data", e);
        } finally {
            db.close();
        }
    }

    private void insertComputersForLab(SQLiteDatabase db, String labName, int count, int accountId) {
        for (int i = 1; i <= count; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ACCOUNT_ID, accountId);
            values.put(COLUMN_LABORATORY_NAME, labName);
            values.put(COLUMN_STATUS, "Available");
            values.put(COLUMN_UNIT, "PC" + i);
            
            db.insert(TABLE_COMPUTER, null, values);
        }
        Log.d(TAG, "Inserted " + count + " computers for " + labName + " with units PC1-PC" + count);
    }

    // Method to check if computers are already inserted
    public boolean areComputersInitialized() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_COMPUTER, new String[]{"COUNT(*)"}, 
                null, null, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // Get computers by laboratory name
    public List<String> getComputersByLab(String labName) {
        List<String> computers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            String[] columns = {COLUMN_UNIT, COLUMN_STATUS};
            String selection = COLUMN_LABORATORY_NAME + "=?";
            String[] selectionArgs = {labName};
            // Use SQLite's CAST and REPLACE functions to sort numerically
            String orderBy = "CAST(REPLACE(" + COLUMN_UNIT + ", 'PC', '') AS INTEGER) ASC";

            Cursor cursor = db.query(
                TABLE_COMPUTER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            );

            while (cursor.moveToNext()) {
                int unitColumnIndex = cursor.getColumnIndex(COLUMN_UNIT);
                int statusColumnIndex = cursor.getColumnIndex(COLUMN_STATUS);
                
                if (unitColumnIndex != -1 && statusColumnIndex != -1) {
                    String unit = cursor.getString(unitColumnIndex);
                    String status = cursor.getString(statusColumnIndex);
                    computers.add(unit + " - " + status);
                } else {
                    Log.e(TAG, "Required columns not found in cursor");
                }
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting computers for lab: " + labName, e);
        } finally {
            db.close();
        }
        
        return computers;
    }

    // Get computer ID by unit and lab name
    public int getComputerId(String unit, String labName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int computerId = -1;
        
        try {
            String[] columns = {COLUMN_COMPUTER_ID};
            String selection = COLUMN_UNIT + "=? AND " + COLUMN_LABORATORY_NAME + "=?";
            String[] selectionArgs = {unit, labName};

            Cursor cursor = db.query(
                TABLE_COMPUTER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_COMPUTER_ID);
                if (columnIndex != -1) {
                    computerId = cursor.getInt(columnIndex);
                } else {
                    Log.e(TAG, "COLUMN_COMPUTER_ID not found in cursor");
                }
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting computer ID", e);
        } finally {
            db.close();
        }
        
        return computerId;
    }

    // Update computer status
    public boolean updateComputerStatus(String unit, String labName, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_STATUS, newStatus);

            String whereClause = COLUMN_UNIT + "=? AND " + COLUMN_LABORATORY_NAME + "=?";
            String[] whereArgs = {unit, labName};

            int rowsAffected = db.update(TABLE_COMPUTER, values, whereClause, whereArgs);
            success = rowsAffected > 0;
            
            if (success) {
                Log.d(TAG, "Successfully updated status for " + unit + " in " + labName + " to " + newStatus);
            } else {
                Log.e(TAG, "Failed to update status for " + unit + " in " + labName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating computer status", e);
        } finally {
            db.close();
        }
        
        return success;
    }

    // Add new issue
    public boolean addIssue(String pcUnit, String labName, String description, int reportedBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        
        try {
            // First get the computer_id
            String[] columns = {COLUMN_COMPUTER_ID};
            String selection = COLUMN_UNIT + "=? AND " + COLUMN_LABORATORY_NAME + "=?";
            String[] selectionArgs = {pcUnit, labName};

            Cursor cursor = db.query(
                TABLE_COMPUTER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_COMPUTER_ID);
                if (columnIndex != -1) {
                    int computerId = cursor.getInt(columnIndex);
                    cursor.close();

                    // Now insert the issue
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_REPORTED_BY, reportedBy);
                    values.put(COLUMN_ISSUE_DESCRIPTION, description);
                    values.put(COLUMN_COMPUTER_ID, computerId);
                    values.put(COLUMN_STATUS, "Pending"); // Initial status for new issues
                    values.put(COLUMN_DATE_REPORTED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                    long issueId = db.insert(TABLE_ISSUE, null, values);
                    success = issueId != -1;

                    if (success) {
                        // Update computer status to Unavailable
                        success = updateComputerStatus(pcUnit, labName, "Unavailable");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding issue", e);
        } finally {
            db.close();
        }
        
        return success;
    }
} 