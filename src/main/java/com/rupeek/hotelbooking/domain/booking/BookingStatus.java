package com.rupeek.hotelbooking.domain.booking;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Booking lifecycle.
 *
 * <pre>
 *   PENDING_PAYMENT ──pay ok──▶ CONFIRMED ──cancel──▶ CANCELLED
 *        │  │                                           ▲
 *        │  └──────────────────cancel───────────────────┘
 *        └──hold lapses──▶ EXPIRED
 * </pre>
 *
 * A failed payment leaves the booking in PENDING_PAYMENT so the guest can retry until the hold lapses.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED;

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PENDING_PAYMENT, EnumSet.of(CONFIRMED, CANCELLED, EXPIRED));
        ALLOWED_TRANSITIONS.put(CONFIRMED, EnumSet.of(CANCELLED));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(BookingStatus.class));
        ALLOWED_TRANSITIONS.put(EXPIRED, EnumSet.noneOf(BookingStatus.class));
    }

    public boolean canTransitionTo(BookingStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}
