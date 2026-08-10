package com.example.inventorted;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.inventorted.data.InventoryIndex;
import com.example.inventorted.data.InventoryItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for InventoryIndex. Plain JUnit, no Android, so they run on the JVM
 * without an emulator. They cover the behaviors the repository depends on:
 * building the index, keyed lookup, single-item overwrite/insert/remove, and the
 * snapshot being a defensive copy.
 */
public class InventoryIndexTest {


    private InventoryIndex indexOf(InventoryItem... items) {
        InventoryIndex index = new InventoryIndex();
        index.rebuild(Arrays.asList(items));
        return index;
    }

    @Test
    public void rebuildPopulatesById() {
        InventoryIndex index = indexOf(
                new InventoryItem(1, "Bolts", 5),
                new InventoryItem(2, "Anchors", 0));

        assertEquals(2, index.size());
        assertEquals("Bolts", index.get(1).getName());
        assertEquals("Anchors", index.get(2).getName());
    }

    @Test
    public void getMissingIdReturnsNull() {
        InventoryIndex index = indexOf(new InventoryItem(1, "Bolts", 5));
        assertNull(index.get(99));
    }

    @Test
    public void putOverwritesSameKeyWithoutGrowing() {
        InventoryIndex index = indexOf(new InventoryItem(1, "Bolts", 5));

        index.put(new InventoryItem(1, "Bolts", 42));

        assertEquals(1, index.size());
        assertEquals(42, index.get(1).getQuantity());
    }

    @Test
    public void putNewKeyGrowsIndex() {
        InventoryIndex index = indexOf(new InventoryItem(1, "Bolts", 5));

        index.put(new InventoryItem(2, "Dowels", 7));

        assertEquals(2, index.size());
        assertEquals("Dowels", index.get(2).getName());
    }
    @Test
    public void removeDropsEntryAndIsNoOpWhenAbsent() {
        InventoryIndex index = indexOf(
                new InventoryItem(1, "Bolts", 5),
                new InventoryItem(2, "Dowels", 7));

        index.remove(2);
        assertEquals(1, index.size());
        assertNull(index.get(2));

        index.remove(2); // absent now; must not throw or change size
        assertEquals(1, index.size());
    }

    @Test
    public void snapshotIsADefensiveCopy() {
        InventoryIndex index = indexOf(
                new InventoryItem(1, "Bolts", 5),
                new InventoryItem(2, "Dowels", 7));

        List<InventoryItem> snapshot = index.snapshot();
        snapshot.clear();

        assertEquals("clearing the snapshot must not affect the index", 2, index.size());
    }

    @Test
    public void rebuildWithNullClearsIndex() {
        InventoryIndex index = indexOf(new InventoryItem(1, "Bolts", 5));
        index.rebuild(null);
        assertTrue(index.isEmpty());
    }

}
