package com.example.inventorted.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Data access for login credentials.
 * findByUsername returns the row so the repository can verify a candidate
 * password against it with PasswordHasher.
 * insert uses ABORT so violating the unique username index throws rather than
 * overwriting.
 */
@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);
}