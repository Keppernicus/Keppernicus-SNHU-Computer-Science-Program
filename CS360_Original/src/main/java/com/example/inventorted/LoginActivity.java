package com.example.inventorted;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity handles user authentication and account creation.
 * Flow of things:
 *  1. User enters username and password
 *  2. "Log In" checks credentials against the users table in SQLite
 *  3. "Create New Account" inserts a new row into the users table
 *  4. On successful login, navigates to InventoryActivity
 * If a user has never logged in before, they create an account first,then log in with those
 * credentials.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView textViewError;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Grab references to the UI components from the layout
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewError = findViewById(R.id.textViewError);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Login button: validate credentials against the database
        buttonLogin.setOnClickListener(v -> attemptLogin());

        // Create account button: add new credentials to the database
        buttonCreateAccount.setOnClickListener(v -> createAccount());
    }

    /**
     * cecks the entered username/password against the users table.
     * if valid, moves to the inventory screen. If not, shows an error.
     */
    private void attemptLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //don't let them submit empty fields
        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        // check credentials against the database
        if (dbHelper.validateUser(username, password)) {
            // Clear any previous error message
            textViewError.setVisibility(View.GONE);

            // navigate to the main inventory screen
            Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish(); // Remove login from the back stack
        } else {
            showError(getString(R.string.error_invalid_login));
        }
    }

    /**
     * creates a new user account with the entered credentials.
     * sows a success toast if created, or an error if the username is taken.
     */
    private void createAccount() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Don't let them submit empty fields
        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_empty_fields));
            return;
        }

        //try to add the user - fails if username already exists (UNIQUE constraint)
        if (dbHelper.addUser(username, password)) {
            textViewError.setVisibility(View.GONE);
            Toast.makeText(this, "Account created! You can now log in.",
                    Toast.LENGTH_SHORT).show();
        } else {
            showError("Username already exists. Please choose another.");
        }
    }

    /*
     * displays an error message below the buttons.
     */
    private void showError(String message) {
        textViewError.setText(message);
        textViewError.setVisibility(View.VISIBLE);
    }
}