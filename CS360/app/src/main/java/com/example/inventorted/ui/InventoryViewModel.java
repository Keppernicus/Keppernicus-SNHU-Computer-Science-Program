package com.example.inventorted.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.inventorted.R;
import com.example.inventorted.data.InventoryItem;
import com.example.inventorted.data.InventoryQuery;
import com.example.inventorted.data.InventoryRepository;
import com.example.inventorted.data.InventorySort;
import com.example.inventorted.util.InputValidator;
import com.example.inventorted.util.SingleLiveEvent;
import com.example.inventorted.util.ValidationError;

import java.util.List;

/*
 * holds the inventory screen's state and logic. validates input, calls the
 * repository, decides what the user gets told. survives rotation, so the list
 * loads once here instead of every time the activity is rebuilt.
 */

public class InventoryViewModel extends AndroidViewModel {
    private final InventoryRepository repository;
    private final SingleLiveEvent<UiMessage> messages = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> zeroStockAlerts = new SingleLiveEvent<>();


    // the state of the screen that drives the look of hte list.
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<InventorySort> sortMode =
            new MutableLiveData<>(InventorySort.NAME);

    // This is the list the screen shows, filtered and sorted, recomputed in memory.
    private final MediatorLiveData<List<InventoryItem>> displayItems = new MediatorLiveData<>();

    private final LiveData<Boolean> inventoryEmpty;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = InventoryRepository.getInstance(application);

        LiveData<List<InventoryItem>> source = repository.getItems();
        displayItems.addSource(source, items -> recomputeDisplay());
        displayItems.addSource(searchQuery, query -> recomputeDisplay());
        displayItems.addSource(sortMode, mode -> recomputeDisplay());
        inventoryEmpty = Transformations.map(source, List::isEmpty);

        loadItems();
    }

    private void recomputeDisplay() {
        List<InventoryItem> current = repository.getItems().getValue();
        displayItems.setValue(
                InventoryQuery.apply(current, searchQuery.getValue(), sortMode.getValue()));
    }

    // the observable state

    @NonNull
    public LiveData<List<InventoryItem>> getItems() {
        return repository.getItems();
    }
    @NonNull
    public LiveData<List<InventoryItem>> getDisplayItems() {
        return displayItems;
    }

    @NonNull
    public LiveData<Boolean> getInventoryEmpty() {
        return inventoryEmpty;
    }

    @NonNull
    public LiveData<InventorySort> getSortMode() {
        return sortMode;
    }

    public void setSearchQuery(String query) {
        String next = query == null ? "" : query;
        if (!next.equals(searchQuery.getValue())) {
            searchQuery.setValue(next);
        }
    }

    public void setSortMode(@NonNull InventorySort mode) {
        if (mode != sortMode.getValue()) {
            sortMode.setValue(mode);
        }
    }

    @NonNull
    public LiveData<UiMessage> getMessages() {
        return messages;
    }

    @NonNull
    public LiveData<String> getZeroStockAlerts() {
        return zeroStockAlerts;
    }

    // actions and such

    public void loadItems() {
        repository.refreshItems(new InventoryRepository.Callback<List<InventoryItem>>() {
            @Override
            public void onSuccess(List<InventoryItem> result) {
                // list arrives via LiveData; nothing to do here
            }

            @Override
            public void onError(@NonNull Exception error) {
                messages.setValue(new UiMessage(R.string.error_loading_inventory));
            }
        });
    }

    public void addItem(String rawName, String rawQuantity) {
        ValidationError nameError = InputValidator.validateName(rawName);
        if (nameError != ValidationError.NONE) {
            messages.setValue(new UiMessage(messageFor(nameError)));
            return;
        }

        ValidationError quantityError = InputValidator.validateQuantity(rawQuantity);
        if (quantityError != ValidationError.NONE) {
            messages.setValue(new UiMessage(messageFor(quantityError)));
            return;
        }

        String name = InputValidator.normalizeName(rawName);
        int quantity = InputValidator.parseValidatedQuantity(rawQuantity);

        repository.addItem(name, quantity, new InventoryRepository.Callback<String>() {
            @Override
            public void onSuccess(String addedName) {
                messages.setValue(new UiMessage(R.string.toast_item_added, addedName));
            }

            @Override
            public void onError(@NonNull Exception error) {
                messages.setValue(new UiMessage(R.string.error_adding_item));
            }
        });
    }

    public void updateQuantity(@NonNull InventoryItem item, String rawNewQuantity) {
        ValidationError quantityError = InputValidator.validateQuantity(rawNewQuantity);
        if (quantityError != ValidationError.NONE) {
            messages.setValue(new UiMessage(messageFor(quantityError)));
            return;
        }

        int newQuantity = InputValidator.parseValidatedQuantity(rawNewQuantity);
        String itemName = item.getName();

        repository.updateItemQuantity(item.getId(), newQuantity,
                new InventoryRepository.Callback<Integer>() {
                    @Override
                    public void onSuccess(Integer rowsUpdated) {
                        if (newQuantity == 0) {
                            zeroStockAlerts.setValue(itemName);
                        }
                    }

                    @Override
                    public void onError(@NonNull Exception error) {
                        messages.setValue(new UiMessage(R.string.error_updating_item));
                    }
                });
    }

    public void deleteItem(@NonNull InventoryItem item) {
        String itemName = item.getName();
        repository.deleteItem(item.getId(), new InventoryRepository.Callback<Integer>() {
            @Override
            public void onSuccess(Integer rowsDeleted) {
                messages.setValue(new UiMessage(R.string.toast_item_removed, itemName));
            }

            @Override
            public void onError(@NonNull Exception error) {
                messages.setValue(new UiMessage(R.string.error_deleting_item));
            }
        });
    }

    // switch case for internals, errors and such

    @StringRes
    static int messageFor(@NonNull ValidationError error) {
        switch (error) {
            case NAME_EMPTY:
                return R.string.error_name_required;
            case NAME_TOO_LONG:
                return R.string.error_name_too_long;
            case QUANTITY_EMPTY:
                return R.string.error_quantity_required;
            case QUANTITY_NOT_A_NUMBER:
                return R.string.error_quantity_not_a_number;
            case QUANTITY_NEGATIVE:
                return R.string.error_quantity_negative;
            case QUANTITY_TOO_LARGE:
                return R.string.error_quantity_too_large;
            case NONE:
            default:
                return R.string.error_unexpected;
        }
    }
}




