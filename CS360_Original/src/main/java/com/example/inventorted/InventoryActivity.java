package com.example.inventorted;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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

    private DatabaseHelper dbHelper;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;

    // UI references
    private EditText editTextItemName;
    private EditText editTextItemQuantity;
    private TextView textViewEmptyState;
    private RecyclerView recyclerView;
    private boolean smsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Grab UI references
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);
        recyclerView = findViewById(R.id.recyclerViewInventory);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);

        // Set up the RecyclerView with a linear layout (vertical list)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        adapter = new InventoryAdapter(itemList, this);
        recyclerView.setAdapter(adapter);

        // Load existing items from the database into the grid
        refreshInventoryList();

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

    /**
     * CREATE: Reads the input fields, validates them, inserts into the
     * database, and refreshes the grid.
     */
    private void addNewItem() {
        String name = editTextItemName.getText().toString().trim();
        String quantityStr = editTextItemQuantity.getText().toString().trim();

        //validate: both fields must have content
        if (name.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter both item name and quantity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);

        // Insert into database
        long result = dbHelper.addItem(name, quantity);
        if (result != -1) {
            // Clear the input fields after a successful add
            editTextItemName.setText("");
            editTextItemQuantity.setText("");

            // Refresh the grid to show the new item
            refreshInventoryList();

            Toast.makeText(this, name + " added to inventory",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error adding item",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * DELETE: Called by the adapter when the user taps the trash icon.
     * Removes the item from the database and refreshes the grid.
     */
    @Override
    public void onDeleteItem(InventoryItem item, int position) {
        dbHelper.deleteItem(item.getId());
        refreshInventoryList();
        Toast.makeText(this, item.getName() + " removed",
                Toast.LENGTH_SHORT).show();
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
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newQuantityStr = input.getText().toString().trim();
            if (!newQuantityStr.isEmpty()) {
                int newQuantity = Integer.parseInt(newQuantityStr);
                dbHelper.updateItemQuantity(item.getId(), newQuantity);
                refreshInventoryList();

                // Check if stock just hit zero and SMS is enabled
                if (newQuantity == 0) {
                    sendZeroStockAlert(item.getName());
                }
            }
        });

        // Cancel button: dismiss without changes
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * READ: Pulls all items from the database and updates the RecyclerView.
     * Also toggles the empty state message visibility.
     */
    private void refreshInventoryList() {
        itemList = dbHelper.getAllItems();
        adapter.updateItems(itemList);

        // Show/hide the "No items yet" message based on whether there's data
        if (itemList.isEmpty()) {
            textViewEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
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

    // ========== SMS PERMISSION HANDLING ==========

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