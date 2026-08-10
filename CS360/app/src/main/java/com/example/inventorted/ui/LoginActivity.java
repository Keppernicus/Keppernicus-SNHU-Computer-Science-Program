package com.example.inventorted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.inventorted.R;

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
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Grab references to the UI components from the layout
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewError = findViewById(R.id.textViewError);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Login button: hands credentials to the viewmodel
        buttonLogin.setOnClickListener(v -> attemptLogin());

        // Create account button: also hands credentials to the viewmodel
        buttonCreateAccount.setOnClickListener(v -> createAccount());
        observeViewModel();
    }

    /**
     * checks the entered username/password.
     * if valid, moves to the inventory screen. If not, shows an error.
     */
    private void attemptLogin() {
        viewModel.attemptLogin(
                editTextUsername.getText().toString(),
                editTextPassword.getText().toString());
    }

    private void createAccount() {
        viewModel.createAccount(
                editTextUsername.getText().toString(),
                editTextPassword.getText().toString());
    }

    private void observeViewModel() {
        viewModel.getInlineError().observe(this, message -> {
            if (message == null) {
                textViewError.setVisibility(View.GONE);
            } else {
                textViewError.setText(getString(message.getResId(), message.getFormatArgs()));
                textViewError.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMessages().observe(this, message ->
                Toast.makeText(this, getString(message.getResId(), message.getFormatArgs()),
                        Toast.LENGTH_SHORT).show());

        viewModel.getLoading().observe(this, loading -> {
            boolean idle = !Boolean.TRUE.equals(loading);
            findViewById(R.id.buttonLogin).setEnabled(idle);
            findViewById(R.id.buttonCreateAccount).setEnabled(idle);
        });

        viewModel.getLoginSucceeded().observe(this, ignored -> {
            startActivity(new Intent(LoginActivity.this, InventoryActivity.class));
            finish();
        });

    }



}