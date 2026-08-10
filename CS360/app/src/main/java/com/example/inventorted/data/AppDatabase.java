package com.example.inventorted.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room database for Inventorted.
 *  Uses a new file name, "inventorted_room.db", so the old SQLite file
 * is left behind and its plaintext data is never read.
 */
@Database(
        entities = { InventoryItem.class, User.class },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemDao itemDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventorted_room.db")
                            .build();
                }
            }
        }
        return instance;
    }
}