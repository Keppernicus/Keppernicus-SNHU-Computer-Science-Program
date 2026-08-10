package com.example.inventorted.data;

import java.util.Comparator;


public enum InventorySort {

    //Alphabetical by name
    NAME(Comparator.comparing(InventorySort::nameOf, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(InventoryItem::getId)),

    // By quantity ascending, then by name
    QUANTITY(Comparator.comparingInt(InventoryItem::getQuantity)
            .thenComparing(InventorySort::nameOf, String.CASE_INSENSITIVE_ORDER)),


    // Out of stock at the top, then by name
    LOW_STOCK(Comparator.comparing((InventoryItem item) -> item.getQuantity() !=0)
            .thenComparing(InventorySort::nameOf, String.CASE_INSENSITIVE_ORDER));

    private final Comparator<InventoryItem> comparator;

    InventorySort(Comparator<InventoryItem> comparator) {
        this.comparator = comparator;
    }

    public Comparator<InventoryItem> comparator() {
        return comparator;
    }

    private static String nameOf(InventoryItem item) {
        return item.getName() == null ? "" : item.getName();
    }

}
