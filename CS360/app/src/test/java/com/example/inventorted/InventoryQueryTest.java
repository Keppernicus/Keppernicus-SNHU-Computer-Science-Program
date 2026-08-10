package com.example.inventorted;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.inventorted.data.InventoryItem;
import com.example.inventorted.data.InventoryQuery;
import com.example.inventorted.data.InventorySort;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for InventoryQuery, which filters by name and then sorts, all in
 * memory. Covers case-insensitive matching, blank/no-match searches, and null
 * safety so a bad input can never crash the screen.
 */
public class InventoryQueryTest {

    private List<InventoryItem> sample() {
        return new ArrayList<>(Arrays.asList(
                new InventoryItem(1, "Bolts", 5),
                new InventoryItem(2, "anchors", 0),
                new InventoryItem(3, "Clamps", 0),
                new InventoryItem(4, "Dowels", 2)));
    }

    private List<Integer> ids(List<InventoryItem> items) {
        List<Integer> ids = new ArrayList<>();
        for (InventoryItem item : items) {
            ids.add(item.getId());
        }
        return ids;
    }
    @Test
    public void filtersByCaseInsensitiveSubstringThenSorts() {
        // names containing "o": Bolts, anchors, Dowels; sorted by name
        List<InventoryItem> result = InventoryQuery.apply(sample(), "O", InventorySort.NAME);
        assertEquals(Arrays.asList(2, 1, 4), ids(result));
    }

    @Test
    public void blankSearchReturnsEverything() {
        List<InventoryItem> result = InventoryQuery.apply(sample(), "   ", InventorySort.QUANTITY);
        assertEquals(4, result.size());
    }

    @Test
    public void noMatchReturnsEmptyList() {
        List<InventoryItem> result = InventoryQuery.apply(sample(), "zzz", InventorySort.NAME);
        assertTrue(result.isEmpty());
    }

    @Test
    public void nullSourceReturnsEmptyListWithoutCrashing() {
        List<InventoryItem> result = InventoryQuery.apply(null, "bolt", InventorySort.NAME);
        assertTrue(result.isEmpty());
    }

    @Test
    public void nullSortFallsBackToNameOrder() {
        List<InventoryItem> result = InventoryQuery.apply(sample(), "", null);
        assertEquals(Arrays.asList(2, 1, 3, 4), ids(result));
    }

}