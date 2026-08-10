package com.example.inventorted;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.inventorted.data.InventoryItem;
import com.example.inventorted.data.InventorySort;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the three sort orderings. This sample data mixes upper and lower
 * case names and zero quantity items so the tests pin down case insensitivity and
 * the low stock grouping.
 */
public class InventorySortTest {

    private List<InventoryItem> sample() {
        return new ArrayList<>(Arrays.asList(
                new InventoryItem(1, "Bells", 5),
                new InventoryItem(2, "anchovies", 0),   // lowercase on purpose
                new InventoryItem(3, "Clams", 0),
                new InventoryItem(4, "Dustpans", 2)));
    }

    private List<Integer> idsAfterSort(InventorySort sort) {
        List<InventoryItem> items = sample();
        items.sort(sort.comparator());
        List<Integer> ids = new ArrayList<>();
        for (InventoryItem item : items) {
            ids.add(item.getId());
        }
        return ids;
    }

    @Test
    public void nameSortsCaseInsensitiveAToZ() {

        assertEquals(Arrays.asList(2, 1, 3, 4), idsAfterSort(InventorySort.NAME));
    }

    @Test
    public void quantitySortsLowToHighWithNameTieBreak() {

        assertEquals(Arrays.asList(2, 3, 4, 1), idsAfterSort(InventorySort.QUANTITY));
    }

    @Test
    public void lowStockGroupsEmptiesFirstThenByName() {

        assertEquals(Arrays.asList(2, 3, 1, 4), idsAfterSort(InventorySort.LOW_STOCK));
    }

    @Test
    public void quantityAndLowStockProduceDifferentOrder() {
        assertNotEquals(idsAfterSort(InventorySort.QUANTITY),
                idsAfterSort(InventorySort.LOW_STOCK));
    }



}