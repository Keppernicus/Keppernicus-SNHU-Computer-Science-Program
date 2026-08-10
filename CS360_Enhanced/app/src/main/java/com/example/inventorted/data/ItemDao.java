package com.example.inventorted.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data access for inventory items.
 * These methods are synchronous. The repository owns a
 * single threaded executor and calls them from inside, then maintains the
 * in memory inventoryindex and posts to livedata itself. returning plain values
 * here keeps the enhancement two architecture intact:
 * room replaces the raw sql, and the index still sits on top unchanged.
 */
@Dao
public interface ItemDao {

    @Query("SELECT * FROM items")
    List<InventoryItem> getAll();

    @Query("SELECT * FROM items WHERE item_id = :id")
    InventoryItem getById(int id);

    /** Returns the generated item_id so the repository can put it in the index. */
    @Insert
    long insert(InventoryItem item);

    @Update
    int update(InventoryItem item);

    @Query("DELETE FROM items WHERE item_id = :id")
    int deleteById(int id);
}