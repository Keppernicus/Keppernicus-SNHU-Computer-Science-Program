package com.example.inventorted.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * InventoryItem - the data model representing a single inventory item.
 *
 * It holds the database ID, item name, and current quantity.
 * Used to pass data between the database and the RecyclerView adapter.
 * I removed the setters I mentioned in the code review, they were never being called.
 * Now the object is just built from a cursor row and only read after that point,
 * and the fields they were setting are now made final
 */
@Entity(tableName = "items")
public final class InventoryItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    private final int id;

    @NonNull
    @ColumnInfo(name = "name")
    private final String name;

    @ColumnInfo(name = "quantity")
    private final int quantity;

    public InventoryItem(int id, @NonNull String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    @NonNull public String getName() { return name; }
    public int getQuantity() { return quantity; }
}