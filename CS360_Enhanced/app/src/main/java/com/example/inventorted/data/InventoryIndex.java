package com.example.inventorted.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventoryIndex {

    private final Map<Integer, InventoryItem> byId = new HashMap<>();

    /**
     * Replaces the whole index from a freshly loaded list. This is the one O(n)
     * pass, run once on the initial database load. Every later change is O(1).
     */

    public void rebuild(List<InventoryItem> items) {
        byId.clear();
        if (items == null) {
            return;
        }
        for (InventoryItem item : items) {
            byId.put(item.getId(), item);
        }
    }

    /** This inserts or overwrite an item by its id, is O(1). */
    public void put(InventoryItem item) {
        byId.put(item.getId(), item);
    }

    public InventoryItem get(int id) {
        return byId.get(id);
    }

    /** Removes a single item by id, is O(1). If there's no id, no effect */
    public void remove(int id) {
        byId.remove(id);
    }

    public boolean isEmpty() {
        return byId.isEmpty();
    }

    public int size() {
        return byId.size();
    }

    /**
     * This returns a new list with the current items. The list is a copy,
     * so anything done to affect the list doens't affect the index.
     */
    public List<InventoryItem> snapshot() {
        return new ArrayList<>(byId.values());
    }

}