package com.example.inventorted.util;


/*
 * validates item name and quantity before anything hits the database.
 * the parse happens in here behind a guard, so the huge-number crash from the
 * code review can't happen. no android imports, so it's plain-java testable.
 */
public final class InputValidator {

    public static final int MAX_NAME_LENGTH = 50;

    private InputValidator() {
    }

    public static ValidationError validateName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            return ValidationError.NAME_EMPTY;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return ValidationError.NAME_TOO_LONG;
        }
        return ValidationError.NONE;
    }

    public static ValidationError validateQuantity(String rawQuantity) {
        String quantity = rawQuantity == null ? "" : rawQuantity.trim();
        if (quantity.isEmpty()) {
            return ValidationError.QUANTITY_EMPTY;
        }

        final int parsed;
        try {
            parsed = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            return ValidationError.QUANTITY_NOT_A_NUMBER;
        }

        if (parsed < 0) {
            return ValidationError.QUANTITY_NEGATIVE;
        }
        return ValidationError.NONE;
    }

    public static int parseValidatedQuantity(String rawQuantity) {
        if (validateQuantity(rawQuantity) != ValidationError.NONE) {
            throw new IllegalArgumentException(
                    "parseValidatedQuantity called on unvalidated input");
        }
        return Integer.parseInt(rawQuantity.trim());
    }

    public static String normalizeName(String rawName) {
        return rawName == null ? "" : rawName.trim();
    }
}

    