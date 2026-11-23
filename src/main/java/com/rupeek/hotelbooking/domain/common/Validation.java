package com.rupeek.hotelbooking.domain.common;

import java.util.regex.Pattern;

/**
 * Argument guards for domain constructors. Violations surface as {@link IllegalArgumentException},
 * which the API layer maps to HTTP 400.
 */
public final class Validation {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    public static String requireEmail(String value, String field) {
        String email = requireNonBlank(value, field);
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException(field + " must be a valid email address");
        }
        return email;
    }
}
