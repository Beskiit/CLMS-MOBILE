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
    private static final int DATABASE_VERSION = 4;
    private final Context context;

    // Table Names
    private static final String TABLE_ACCOUNT = "Account";
    private static final String TABLE_COMPUTER = "Computer";
    private static final String TABLE_LOGIN_HISTORY = "Login_History";
    private static final String TABLE_ACTIVITY_LOG = "Activity_Log";
    private static final String TABLE_ISSUE = "Issue";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";

    // Account Table Columns
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_COURSE = "course";
    public static final String COLUMN_ACCOUNT_CREATED = "account_created";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_IS_ARCHIVED = "isArchived";

    // Computer Table Columns
    public static final String COLUMN_LABORATORY_NAME = "laboratory_name";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_CURRENT_USER = "current_user";

    // Login History Table Columns
    public static final String COLUMN_COMPUTER_LABORATORY = "computer_laboratory";
    public static final String COLUMN_PC_NAME = "pc_name";
    public static final String COLUMN_LOGGED_IN = "logged_in";
    public static final String COLUMN_LOGGED_OUT = "logged_out";
    public static final String COLUMN_LOG_STATUS = "log_status";

    // Activity Log Table Columns
    public static final String COLUMN_ACTIVITY_NAME = "activity_name";
    public static final String COLUMN_DATE_TIME = "date_time";

    // Issue Table Columns
    public static final String COLUMN_REPORTED_BY = "reported_by";
    public static final String COLUMN_ISSUE_DESCRIPTION = "issue_description";
    public static final String COLUMN_DATE_REPORTED = "date_reported";

    // Create Table Statements
    private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + TABLE_ACCOUNT + "("
            + COLUMN_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_COURSE + " TEXT,"
            + COLUMN_ACCOUNT_CREATED + " DATETIME,"
            + COLUMN_ROLE + " TEXT,"
            + COLUMN_IS_ARCHIVED + " INTEGER DEFAULT 0"
            + ")";

    private static final String CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_COMPUTER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_LABORATORY_NAME + " TEXT,"
            + COLUMN_UNIT + " TEXT,"
            + COLUMN_STATUS + " TEXT,"
            + COLUMN_CURRENT_USER + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_LOGIN_HISTORY = "CREATE TABLE " + TABLE_LOGIN_HISTORY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACCOUNT_ID + " INTEGER,"
            + COLUMN_COMPUTER_LABORATORY + " TEXT,"
            + COLUMN_PC_NAME + " TEXT,"
            + COLUMN_LOGGED_IN + " DATETIME,"
            + COLUMN_LOGGED_OUT + " DATETIME,"
            + COLUMN_LOG_STATUS + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + ")"
            + ")";

    private static final String CREATE_TABLE_ACTIVITY_LOG = "CREATE TABLE " + TABLE_ACTIVITY_LOG + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACTIVITY_NAME + " TEXT,"
            + COLUMN_ACCOUNT_ID + " INTEGER,"
            + COLUMN_DATE_TIME + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + ")"
            + ")";

    private static final String CREATE_TABLE_ISSUE = "CREATE TABLE " + TABLE_ISSUE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_REPORTED_BY + " INTEGER,"
            + COLUMN_ISSUE_DESCRIPTION + " TEXT,"
            + COLUMN_COMPUTER_LABORATORY + " TEXT,"
            + COLUMN_PC_NAME + " TEXT,"
            + COLUMN_STATUS + " TEXT DEFAULT 'Pending',"
            + COLUMN_DATE_REPORTED + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_REPORTED_BY + ") REFERENCES " + TABLE_ACCOUNT + "(" + COLUMN_ACCOUNT_ID + ")"
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
            
            // Create default admin account
            ContentValues adminValues = new ContentValues();
            adminValues.put(COLUMN_NAME, "Administrator");
            adminValues.put(COLUMN_USERNAME, "admin");
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest("admin123".getBytes("UTF-8"));
                String hashedPassword = Base64.encodeToString(hash, Base64.DEFAULT);
                adminValues.put(COLUMN_PASSWORD, hashedPassword);
            } catch (Exception e) {
                Log.e(TAG, "Error hashing admin password", e);
                adminValues.put(COLUMN_PASSWORD, "admin123"); // Fallback to plain password if hashing fails
            }
            adminValues.put(COLUMN_ROLE, "Admin");
            adminValues.put(COLUMN_ACCOUNT_CREATED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            
            long adminId = db.insert(TABLE_ACCOUNT, null, adminValues);
            Log.d(TAG, "Admin account created with ID: " + adminId);
            
            // Initialize computers for each lab
            if (adminId != -1) {
                insertComputersForLab(db, "Computer Laboratory A", 40);
                insertComputersForLab(db, "Computer Laboratory B", 40);
                insertComputersForLab(db, "Computer Laboratory C", 50);
            }
            
            Log.d(TAG, "All tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables", e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        if (oldVersion < 5) {
            // Add current_user column to computer table if it doesn't exist
            try {
                db.execSQL("ALTER TABLE " + TABLE_COMPUTER + " ADD COLUMN " + COLUMN_CURRENT_USER + " INTEGER");
                Log.d(TAG, "Added current_user column to computer table");
            } catch (Exception e) {
                Log.e(TAG, "Error adding current_user column: " + e.getMessage());
            }
        }
        
        try {
            // Enable foreign key support
            db.execSQL("PRAGMA foreign_keys = OFF;");

            // Backup existing data if needed
            // For this version, we'll just drop and recreate
            
            // Drop existing tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ISSUE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_LOG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);

            // Create new tables
            onCreate(db);

            // Re-enable foreign key support
            db.execSQL("PRAGMA foreign_keys = ON;");

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
                        insertComputersForLab(db, "Computer Laboratory A", 40);

                        // Insert computers for Laboratory B (40 computers)
                        insertComputersForLab(db, "Computer Laboratory B", 40);

                        // Insert computers for Laboratory C (50 computers)
                        insertComputersForLab(db, "Computer Laboratory C", 50);

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

    private void insertComputersForLab(SQLiteDatabase db, String labName, int count) {
        try {
            for (int i = 1; i <= count; i++) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_LABORATORY_NAME, labName);
                values.put(COLUMN_STATUS, "Available");
                values.put(COLUMN_UNIT, "PC" + i);
                
                long result = db.insert(TABLE_COMPUTER, null, values);
                if (result == -1) {
                    Log.e(TAG, "Failed to insert computer: PC" + i + " in " + labName);
                }
            }
            Log.d(TAG, "Successfully inserted " + count + " computers for " + labName);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting computers for " + labName, e);
        }
    }

    // Method to check if computers are already initialized
    public boolean areComputersInitialized() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_COMPUTER, new String[]{"COUNT(*)"}, 
                null, null, null, null, null);
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                Log.d(TAG, "Found " + count + " computers in database");
                return count > 0;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if computers are initialized", e);
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
            String[] columns = {COLUMN_ID};
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
                int columnIndex = cursor.getColumnIndex(COLUMN_ID);
                if (columnIndex != -1) {
                    computerId = cursor.getInt(columnIndex);
                    Log.d(TAG, "Found computer ID: " + computerId);
                } else {
                    Log.e(TAG, "Column ID not found in cursor");
                }
            } else {
                Log.e(TAG, "No computer found for unit: " + unit + " in lab: " + labName);
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
            db.beginTransaction();
            
            // First verify the computer exists and get its status
            String[] columns = {COLUMN_STATUS};
            String selection = COLUMN_LABORATORY_NAME + "=? AND " + COLUMN_UNIT + "=?";
            String[] selectionArgs = {labName, pcUnit};
            
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
                // Create the issue entry
                ContentValues values = new ContentValues();
                values.put(COLUMN_REPORTED_BY, reportedBy);
                values.put(COLUMN_ISSUE_DESCRIPTION, description);
                values.put(COLUMN_COMPUTER_LABORATORY, labName);
                values.put(COLUMN_PC_NAME, pcUnit);
                values.put(COLUMN_STATUS, "Pending"); // Initial status for new issues
                values.put(COLUMN_DATE_REPORTED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                long issueId = db.insert(TABLE_ISSUE, null, values);
                
                if (issueId != -1) {
                    // Update computer status to Unavailable
                    ContentValues computerValues = new ContentValues();
                    computerValues.put(COLUMN_STATUS, "Unavailable");
                    
                    int updateResult = db.update(TABLE_COMPUTER, computerValues, selection, selectionArgs);
                    
                    if (updateResult > 0) {
                        db.setTransactionSuccessful();
                        success = true;
                        Log.d(TAG, "Successfully added issue and updated computer status");
                    } else {
                        Log.e(TAG, "Failed to update computer status");
                    }
                } else {
                    Log.e(TAG, "Failed to insert issue");
                }
            } else {
                Log.e(TAG, "Computer not found: " + pcUnit + " in " + labName);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error adding issue", e);
        } finally {
            try {
                db.endTransaction();
                db.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing database", e);
            }
        }
        
        return success;
    }

    // Method to log in to a PC
    public boolean loginToPC(int accountId, String computerLab, String pcName) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            db.beginTransaction();
            
            String[] columns = {COLUMN_STATUS};
            String whereClause = COLUMN_LABORATORY_NAME + "=? AND " + COLUMN_UNIT + "=?";
            String[] whereArgs = {computerLab, pcName};
            
            Log.d(TAG, "Checking PC status - Lab: " + computerLab + ", PC: " + pcName);
            Cursor cursor = db.query(TABLE_COMPUTER, columns, whereClause, whereArgs, null, null, null);

            if (cursor.moveToFirst()) {
                String currentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                cursor.close();
                
                Log.d(TAG, "Current PC status: " + currentStatus);
                
                if ("Available".equals(currentStatus)) {
                    // First create login history entry
                    ContentValues historyValues = new ContentValues();
                    historyValues.put(COLUMN_ACCOUNT_ID, accountId);
                    historyValues.put(COLUMN_COMPUTER_LABORATORY, computerLab);
                    historyValues.put(COLUMN_PC_NAME, pcName);
                    historyValues.put(COLUMN_LOGGED_IN, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    historyValues.put(COLUMN_LOGGED_OUT, (String) null); // Set logged_out to null initially
                    historyValues.put(COLUMN_LOG_STATUS, "Active");
                    
                    long insertResult = db.insert(TABLE_LOGIN_HISTORY, null, historyValues);
                    
                    if (insertResult != -1) {
                        // Then update computer status
                        ContentValues computerValues = new ContentValues();
                        computerValues.put(COLUMN_STATUS, "Occupied");
                        computerValues.put(COLUMN_CURRENT_USER, accountId);
                        
                        int updateResult = db.update(TABLE_COMPUTER, computerValues, whereClause, whereArgs);
                        
                        if (updateResult > 0) {
                            db.setTransactionSuccessful();
                            success = true;
                            Log.d(TAG, "Successfully logged in to PC");
                        } else {
                            Log.e(TAG, "Failed to update computer status");
                        }
                    } else {
                        Log.e(TAG, "Failed to insert login history");
                    }
                } else {
                    Log.e(TAG, "PC is not available. Current status: " + currentStatus);
                }
            } else {
                Log.e(TAG, "Computer not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during PC login", e);
        } finally {
            try {
                db.endTransaction();
                db.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing database", e);
            }
        }
        
        return success;
    }

    // Method to log out from a PC
    public boolean logOutFromPC(int accountId, String computerLab, String pcName) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        
        try {
            Log.d(TAG, "Starting logout process...");
            db.beginTransaction();
            
            // First update the login history entry
            ContentValues historyValues = new ContentValues();
            historyValues.put(COLUMN_LOGGED_OUT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            historyValues.put(COLUMN_LOG_STATUS, "Completed");
            
            String historyWhereClause = COLUMN_ACCOUNT_ID + "=? AND " + COLUMN_COMPUTER_LABORATORY + "=? AND " 
                                    + COLUMN_PC_NAME + "=? AND " + COLUMN_LOG_STATUS + "=? AND " + COLUMN_LOGGED_OUT + " IS NULL";
            String[] historyWhereArgs = {String.valueOf(accountId), computerLab, pcName, "Active"};
            
            int historyUpdateResult = db.update(TABLE_LOGIN_HISTORY, historyValues, historyWhereClause, historyWhereArgs);
            
            if (historyUpdateResult > 0) {
                // Then update computer status
                ContentValues computerValues = new ContentValues();
                computerValues.put(COLUMN_STATUS, "Available");
                computerValues.put(COLUMN_CURRENT_USER, (Integer) null); // Clear current user
                
                String computerWhereClause = COLUMN_LABORATORY_NAME + "=? AND " + COLUMN_UNIT + "=?";
                String[] computerWhereArgs = {computerLab, pcName};
                
                int computerUpdateResult = db.update(TABLE_COMPUTER, computerValues, computerWhereClause, computerWhereArgs);
                
                if (computerUpdateResult > 0) {
                    db.setTransactionSuccessful();
                    success = true;
                    Log.d(TAG, "Logout process completed successfully");
                } else {
                    Log.e(TAG, "Failed to update computer status during logout");
                }
            } else {
                Log.e(TAG, "Failed to update login history during logout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout process", e);
            e.printStackTrace();
        } finally {
            try {
                db.endTransaction();
                db.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing database", e);
            }
        }
        
        return success;
    }

    // Method to get login history for a user
    public Cursor getLoginHistory(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
            COLUMN_COMPUTER_LABORATORY,
            COLUMN_PC_NAME,
            COLUMN_LOGGED_IN,
            COLUMN_LOGGED_OUT,
            COLUMN_LOG_STATUS
        };
        String selection = COLUMN_ACCOUNT_ID + "=?";
        String[] selectionArgs = {String.valueOf(accountId)};
        String orderBy = COLUMN_LOGGED_IN + " DESC";
        
        return db.query(TABLE_LOGIN_HISTORY, columns, selection, selectionArgs, null, null, orderBy);
    }

    // Method to get account ID by username
    public int getAccountId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int accountId = -1;
        
        try {
            String[] columns = {COLUMN_ACCOUNT_ID};
            String selection = COLUMN_USERNAME + "=?";
            String[] selectionArgs = {username};

            Cursor cursor = db.query(TABLE_ACCOUNT, columns, selection, selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_ACCOUNT_ID);
                if (columnIndex != -1) {
                    accountId = cursor.getInt(columnIndex);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting account ID", e);
        } finally {
            db.close();
        }
        
        return accountId;
    }

    // Method to verify computer status
    public String getComputerStatus(String labName, String pcUnit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = null;
        
        try {
            String[] columns = {COLUMN_STATUS};
            String selection = COLUMN_LABORATORY_NAME + "=? AND " + COLUMN_UNIT + "=?";
            String[] selectionArgs = {labName, pcUnit};
            
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
                int statusIndex = cursor.getColumnIndex(COLUMN_STATUS);
                if (statusIndex != -1) {
                    status = cursor.getString(statusIndex);
                    Log.d(TAG, "Computer " + pcUnit + " status: " + status);
                } else {
                    Log.e(TAG, "Status column not found in cursor");
                }
            } else {
                Log.e(TAG, "Computer " + pcUnit + " not found");
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting computer status", e);
        } finally {
            db.close();
        }
        
        return status;
    }

    // Method to check if user has an active session
    public String[] getActiveSession(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] sessionInfo = null;
        
        try {
            String[] columns = {COLUMN_COMPUTER_LABORATORY, COLUMN_PC_NAME};
            String selection = COLUMN_ACCOUNT_ID + "=? AND " + COLUMN_LOG_STATUS + "=? AND " + COLUMN_LOGGED_OUT + " IS NULL";
            String[] selectionArgs = {String.valueOf(accountId), "Active"};
            
            Cursor cursor = db.query(TABLE_LOGIN_HISTORY, columns, selection, selectionArgs, null, null, null);
            
            if (cursor.moveToFirst()) {
                sessionInfo = new String[2];
                int labIndex = cursor.getColumnIndex(COLUMN_COMPUTER_LABORATORY);
                int pcIndex = cursor.getColumnIndex(COLUMN_PC_NAME);
                
                if (labIndex != -1 && pcIndex != -1) {
                    sessionInfo[0] = cursor.getString(labIndex);  // Laboratory name
                    sessionInfo[1] = cursor.getString(pcIndex);   // PC name
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error checking active session", e);
        } finally {
            db.close();
        }
        
        return sessionInfo;
    }
} 