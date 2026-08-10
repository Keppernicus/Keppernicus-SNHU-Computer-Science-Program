package com.example.inventorted;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper - Manages the SQLite database for Inventorted.
 * Two tables:
 *   users  - stores login credentials (username + password)
 *   items  - stores inventory items (name + quantity)
 * The database is persistent, so data survives app restarts.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //database info
    private static final String DATABASE_NAME = "inventorted.db";
    private static final int DATABASE_VERSION = 1;

    // users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    //items table
    private static final String TABLE_ITEMS = "items";
    private static final String COL_ITEM_ID = "item_id";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_ITEM_QUANTITY = "item_quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create users table for login authentication
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create items table for inventory tracking
        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " ("
                + COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ITEM_NAME + " TEXT NOT NULL, "
                + COL_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0)";
        db.execSQL(createItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // user operations

    /**
     * Adds a new user to the database.
     * Returns true if the insert succeeded, false if the username already exists.
     */
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        // insert returns -1 if the row wasn't inserted (e.g., duplicate username)
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Checks if a username/password combination exists in the database.
     * Returns true if credentials are valid.
     */
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null);

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    // item operations (CRUD)

    /**
     * CREATE: Adds a new item to the inventory.
     * Returns the row ID of the new item, or -1 on failure.
     */
    public long addItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name);
        values.put(COL_ITEM_QUANTITY, quantity);
        return db.insert(TABLE_ITEMS, null, values);
    }

    /**
     * READ: Returns all items in the inventory as a list of InventoryItem objects.
     */
    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEMS,
                null, null, null, null, null,
                COL_ITEM_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_QUANTITY));
                items.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    /**
     * UPDATE:Changes the quantity of an existing item.
     * Returns the number of rows affected (should be 1).
     */
    public int updateItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_QUANTITY, newQuantity);
        return db.update(TABLE_ITEMS, values,
                COL_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }

    /**
     * DELETE: removes an item from the inventory by its ID.
     * Returns the number of rows deleted.
     */
    public int deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS,
                COL_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }
}