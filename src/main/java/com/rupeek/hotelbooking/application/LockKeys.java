package com.rupeek.hotelbooking.application;

final class LockKeys {

    private LockKeys() {
    }

    static String booking(String bookingId) {
        return "booking:" + bookingId;
    }

    static String inventory(String roomTypeId) {
        return "inventory:" + roomTypeId;
    }
}
