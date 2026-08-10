package com.example.inventorted.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * User - a login credential row.
 *
 * The password is never stored. Password_hash is the stored pw, the encoded
 * PBKDF2 string produced by PasswordHasher (iterations:salt:hash), which is not
 * reversible.
 *
 * The unique index on username enforces at the database level that two accounts
 * cannot share a name
 */
@Entity(
        tableName = "users",
        indices = { @Index(value = "username", unique = true) }
)
public final class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private final int id;

    @NonNull
    @ColumnInfo(name = "username")
    private final String username;

    @NonNull
    @ColumnInfo(name = "password_hash")
    private final String passwordHash;

    public User(int id, @NonNull String username, @NonNull String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    @NonNull public String getUsername() { return username; }
    @NonNull public String getPasswordHash() { return passwordHash; }
}
