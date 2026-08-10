package com.example.inventorted.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorted.R;
import com.example.inventorted.data.InventoryItem;
import com.example.inventorted.data.InventorySort;

/**
 * inventoryActivity - Main screen displaying the inventory grid.
 * Handles all four CRUD operations:
 *   CREATE - Add new items via the input fields at the top
 *   READ   - Display all items in a RecyclerView grid
 *   UPDATE - Tap a row to edit the quantity via a dialog
 *   DELETE - Tap the trash icon on any row to remove it
 * also manages SMS notification permissions and sends zero-stock
 * alerts when an item's quantity is updated to 0.
 */
public class InventoryActivity extends AppCompatActivity
        implements InventoryAdapter.OnItemActionListener {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    // SMS recipient number for zero-stock alerts
    // In a production app this would be configurable by the user
    private static final String ALERT_PHONE_NUMBER = "5551234567";

    private InventoryViewModel viewModel;
    private InventoryAdapter adapter;

    // UI references
    private EditText editTextItemName;
    private EditText editTextItemQuantity;
    private TextView textViewEmptyState;
    private RecyclerView recyclerView;
    private boolean smsEnabled = false;

    // This tracks if the inventory is really empty so emptry state is correctly announced
    private boolean inventoryIsEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        observeViewModel();

        // Grab UI references
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        recyclerView = findViewById(R.id.recyclerViewInventory);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);

        // Set up the RecyclerView with a linear layout (vertical list)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(this);

        recyclerView.setAdapter(adapter);

        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        RadioGroup radioGroupSort = findViewById(R.id.radioGroupSort);
        radioGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioSortQuantity) {
                viewModel.setSortMode(InventorySort.QUANTITY);
            } else if (checkedId == R.id.radioSortLowStock) {
                viewModel.setSortMode(InventorySort.LOW_STOCK);
            } else {
                viewModel.setSortMode(InventorySort.NAME);
            }
        });

        // Add button: insert a new item into the database and refresh
        buttonAddItem.setOnClickListener(v -> addNewItem());

        // SMS permission setup (carried over from Project Two)
        updateSmsPermissionStatus();
        Button buttonEnableSms = findViewById(R.id.buttonEnableSms);
        buttonEnableSms.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not yet granted — request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted — toggle on/off
                smsEnabled = !smsEnabled;
                updateSmsPermissionStatus();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getDisplayItems().observe(this, items -> {
            adapter.updateItems(items);
            if (items.isEmpty()) {
                textViewEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textViewEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMessages().observe(this, message ->
                Toast.makeText(this, getString(message.getResId(), message.getFormatArgs()),
                        Toast.LENGTH_SHORT).show());

        viewModel.getZeroStockAlerts().observe(this, this::sendZeroStockAlert);
    }

    /**
     * This shows the appropriate empty state msg
     */
    private void updateEmptyStateText() {
        textViewEmptyState.setText(inventoryIsEmpty ? R.string.empty_state:R.string.empty_state_no_matches);
    }

    /**
     * CREATE: Reads the input fields, validates them, inserts into the
     * database, and refreshes the grid.
     */
    private void addNewItem() {
        String name = editTextItemName.getText().toString();
        String quantity = editTextItemQuantity.getText().toString();

        viewModel.addItem(name, quantity);

        editTextItemName.setText("");
        editTextItemQuantity.setText("");
    }

    /**
     * DELETE: Called by the adapter when the user taps the trash icon.
     * Removes the item from the database and refreshes the grid.
     */
    @Override
    public void onDeleteItem(InventoryItem item, int position) {
        viewModel.deleteItem(item);
    }

    /**
     * UPDATE: Called by the adapter when the user taps a row.
     * Shows a dialog where they can enter a new quantity for that item.
     */
    @Override
    public void onEditItem(InventoryItem item, int position) {
        // Build a simple dialog with a number input field
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Quantity");
        builder.setMessage("Enter new quantity for " + item.getName() + ":");

        // Create an EditText for the dialog input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getQuantity()));
        input.setSelectAllOnFocus(true);
        builder.setView(input);

        // Save button: update the database and refresh
        builder.setPositiveButton("Save", (dialog, which) ->
                viewModel.updateQuantity(item, input.getText().toString()));

        // Cancel button: dismiss without changes
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    /**
     * Sends an SMS alert when an item's stock reaches zero.
     * Only sends if the user has granted SMS permission.
     * If permission was denied, the app continues to function without alerts.
     */
    private void sendZeroStockAlert(String itemName) {
        if (smsEnabled && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String message = "Inventorted Alert: " + itemName
                        + " has reached zero stock. Please reorder.";
                smsManager.sendTextMessage(ALERT_PHONE_NUMBER, null,
                        message, null, null);
                Toast.makeText(this, "Zero stock alert sent for " + itemName,
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // If SMS fails for any reason, don't crash - just notify the user
                Toast.makeText(this, "Could not send SMS alert",
                        Toast.LENGTH_SHORT).show();
            }
        }
        // If permission not granted, silently skip - app continues to work
    }

    // SMS PERMISSION HANDLING

    /**
     * Updates the SMS status text and button based on current permission state.
     */
    private void updateSmsPermissionStatus() {
        TextView smsStatus = findViewById(R.id.textViewSmsStatus);
        Button enableButton = findViewById(R.id.buttonEnableSms);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            if (smsEnabled) {
                smsStatus.setText(R.string.sms_status_enabled);
                enableButton.setText("Disable SMS Notifications");
            } else {
                smsStatus.setText(R.string.sms_status_disabled);
                enableButton.setText("Enable SMS Notifications");
            }
            enableButton.setEnabled(true);
        } else {
            smsStatus.setText(R.string.sms_status_disabled);
            enableButton.setText("Enable SMS Notifications");
            enableButton.setEnabled(true);
            smsEnabled = false;
        }
    }

    /**
     * callback from Android's permission dialog.
     * Updates the UI based on their choice.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            updateSmsPermissionStatus();
            // If denied, app continues to function - just no SMS notifications
        }
    }
}