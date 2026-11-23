package com.rupeek.hotelbooking.application;

import java.util.UUID;

final class Ids {

    private Ids() {
    }

    static String newId(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }
}
