package com.example.inventorted.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inventorted.R;
import com.example.inventorted.data.InventoryRepository;
import com.example.inventorted.util.SingleLiveEvent;

/*
 * same idea as the inventory viewmodel but for the login screen. checks credentials
 * off the main thread through the repository and tells the activity what happened.
 */

public class LoginViewModel extends AndroidViewModel {

    private final InventoryRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<UiMessage> inlineError = new MutableLiveData<>();
    private final SingleLiveEvent<UiMessage> messages = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> loginSucceeded = new SingleLiveEvent<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = InventoryRepository.getInstance(application);
    }

    @NonNull
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    @NonNull
    public LiveData<UiMessage> getInlineError() {
    return inlineError;
    }

    @NonNull
    public LiveData<Void> getLoginSucceeded() {
        return loginSucceeded;
    }

    @NonNull
    public LiveData<UiMessage> getMessages() {
        return messages;
    }

    // now for the actions


    public void attemptLogin(String rawUsername, String rawPassword) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        String password = rawPassword == null ? "" : rawPassword.trim();

        if (username.isEmpty() || password.isEmpty()) {
            inlineError.setValue(new UiMessage(R.string.error_empty_fields));
            return;
        }

        loading.setValue(true);
        repository.validateUser(username, password, new InventoryRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean valid) {
                loading.setValue(false);
                if (Boolean.TRUE.equals(valid)) {
                    inlineError.setValue(null);
                    loginSucceeded.setValue(null);
                } else {
                    inlineError.setValue(new UiMessage(R.string.error_invalid_login));
                }
            }

            @Override
            public void onError(@NonNull Exception error) {
                loading.setValue(false);
                inlineError.setValue(new UiMessage(R.string.error_database_unavailable));
            }
        });
    }

    /*
     * create an account with the credentials
     */
    public void createAccount(String rawUsername, String rawPassword) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        String password = rawPassword == null ? "" : rawPassword.trim();

        if (username.isEmpty() || password.isEmpty()) {
            inlineError.setValue(new UiMessage(R.string.error_empty_fields));
            return;
        }

        loading.setValue(true);
        repository.createUser(username, password, new InventoryRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean created) {
                loading.setValue(false);
                if (Boolean.TRUE.equals(created)) {
                    inlineError.setValue(null);
                    messages.setValue(new UiMessage(R.string.toast_account_created));
                } else {
                    inlineError.setValue(new UiMessage(R.string.error_username_taken));
                }
            }

            @Override
            public void onError(@NonNull Exception error) {
                loading.setValue(false);
                inlineError.setValue(new UiMessage(R.string.error_database_unavailable));
            }
        });
    }



}

