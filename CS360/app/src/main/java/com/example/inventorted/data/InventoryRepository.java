package com.example.inventorted.data;



import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inventorted.security.PasswordHasher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * the only class that touches the database now. everything goes through here.
 * runs the db work on a background thread so it's off the main/ui thread,
 * and hands results back through livedata or a callback posted to the main thread.
 */

public class InventoryRepository {

    private static final String TAG = "InventoryRepository";

    private static volatile InventoryRepository instance;

    public interface Callback<T> {
        @MainThread
        void onSuccess(T results);

        @MainThread
        void onError(@NonNull Exception error);
    }

    private final ItemDao itemDao;
    private final UserDao userDao;
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final MutableLiveData<List<InventoryItem>> items =
            new MutableLiveData<>(Collections.emptyList());

    private final InventoryIndex index = new InventoryIndex();

    private InventoryRepository(@NonNull Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        this.itemDao = db.itemDao();
        this.userDao = db.userDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static InventoryRepository getInstance(@NonNull Context context) {
        InventoryRepository local = instance;
        if (local == null) {
            synchronized (InventoryRepository.class) {
                local = instance;
                if (local ==null) {
                    local = new InventoryRepository(context);
                    instance = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public LiveData<List<InventoryItem>> getItems() {
        return items;
    }

    public void refreshItems(@Nullable Callback<List<InventoryItem>> callback) {
        executor.execute(() -> {
            try {
                // One full read from the database, done once. This is now the
                // only place the whole table is loaded
                List<InventoryItem> loaded = itemDao.getAll();
                index.rebuild(loaded);
                items.postValue(index.snapshot());
                deliverSuccess(callback, loaded);
            } catch (SQLiteException | IllegalArgumentException e) {
                Log.e(TAG, "Failed to load inventory items", e);
                deliverError(callback, e);
            }
        });
    }

    public void addItem(@NonNull String name, int quantity,
                        @Nullable Callback<String> callback) {
        executor.execute(() -> {
            try {
                long newId = itemDao.insert(new InventoryItem(0, name, quantity));
                if (newId == -1L) {
                    Log.w(TAG, "Insert rejected for item: " + name);
                    deliverError(callback, new SQLiteException("Insert returned -1 for " + name));
                    return;
                }
                // The new row's id comes straight back from the insert, so the
                // item drops into the index in O(1) with no full-table reload.
                index.put(new InventoryItem((int) newId, name, quantity));
                items.postValue(index.snapshot());
                deliverSuccess(callback, name);
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to add item: " + name, e);
                deliverError(callback, e);
            }
        });
    }

    public void updateItemQuantity(int itemId, int newQuantity,
                                   @Nullable Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                // O(1) lookup of the existing item to keep its name, then
                // overwrite that single entry
                InventoryItem existing = requireIndexed(itemId);
                if (existing == null) {
                    Log.w(TAG, "Update affected no rows for item id " + itemId);
                    deliverError(callback, new SQLiteException("No row updated for id " + itemId));
                    return;
                }
                InventoryItem updated = new InventoryItem(itemId, existing.getName(), newQuantity);
                int rows = itemDao.update(updated);
                if (rows == 0) {
                    Log.w(TAG, "Update affected no rows for item id " + itemId);
                    deliverError(callback, new SQLiteException("No row updated for id " + itemId));
                    return;
                }
                index.put(updated);
                items.postValue(index.snapshot());
                deliverSuccess(callback, rows);
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to update quantity for item id: " + itemId, e);
                deliverError(callback, e);
            }
        });
    }

    public void deleteItem(int itemId, @Nullable Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int rows = itemDao.deleteById(itemId);
                if (rows == 0) {
                    Log.w(TAG, "Delete affected no rows for item id " + itemId);
                    deliverError(callback, new SQLiteException("No row deleted for id " + itemId));
                    return;
                }
                // srop the single entry from the index in O(1); no reload.
                index.remove(itemId);
                items.postValue(index.snapshot());
                deliverSuccess(callback, rows);
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to delete item id " + itemId, e);
                deliverError(callback, e);
            }
        });
    }

    public void validateUser(@NonNull String username, @NonNull String password,
                             @NonNull Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                // verify against the stored hash; the password is never matched in SQL
                User user = userDao.findByUsername(username);
                boolean ok = user != null
                        && passwordHasher.verify(password, user.getPasswordHash());
                deliverSuccess(callback, ok);
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to validate user", e);
                deliverError(callback, e);
            }
        });
    }

    public void createUser(@NonNull String username, @NonNull String password,
                           @NonNull Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                // store only the hash; the unique index rejects a duplicate username
                long id = userDao.insert(new User(0, username, passwordHasher.hash(password)));
                deliverSuccess(callback, id != -1L);
            } catch (SQLiteConstraintException e) {
                Log.w(TAG, "Username already exists: " + username);
                deliverSuccess(callback, false);
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to create user", e);
                deliverError(callback, e);
            }
        });
    }

    /**
     * Returns the indexed item for an id, rebuilding the index from the database
     * once if it has not been populated yet
     */
    @Nullable
    private InventoryItem requireIndexed(int itemId) {
        InventoryItem item = index.get(itemId);
        if (item == null && index.isEmpty()) {
            index.rebuild(itemDao.getAll());
            item = index.get(itemId);
        }
        return item;
    }

    private <T> void deliverSuccess(@Nullable Callback<T> callback, T result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private <T> void deliverError(@Nullable Callback<T> callback, @NonNull Exception error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }


}