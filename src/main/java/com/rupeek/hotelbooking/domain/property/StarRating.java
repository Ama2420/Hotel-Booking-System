package com.rupeek.hotelbooking.domain.property;

public record StarRating(int value) {

    public static final int MIN = 1;
    public static final int MAX = 5;

    public StarRating {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("starRating must be between " + MIN + " and " + MAX);
        }
    }

    public boolean isAtLeast(int minimum) {
        return value >= minimum;
    }
}
