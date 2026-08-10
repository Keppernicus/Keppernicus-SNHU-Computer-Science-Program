package com.example.inventorted.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class InventoryQuery {

    private InventoryQuery() {
    }

    public static List<InventoryItem> apply(List<InventoryItem> source, String search,
                                            InventorySort sort) {
        List<InventoryItem> result = new ArrayList<>();
        if (source == null) {
            return result;
        }

        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        for (InventoryItem item : source) {
            if (needle.isEmpty() || matches(item, needle)) {
                result.add(item);
            }
        }

        InventorySort ordering = sort == null ? InventorySort.NAME : sort;
        result.sort(ordering.comparator());
        return result;
    }

    private static boolean matches(InventoryItem item, String lowerNeedle) {
        String name = item.getName();
        if (name == null) {
            return false;
        }
        return name.toLowerCase(Locale.ROOT).contains(lowerNeedle);
    }

}
