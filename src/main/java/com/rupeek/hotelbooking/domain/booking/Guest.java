package com.rupeek.hotelbooking.domain.booking;

import static com.rupeek.hotelbooking.domain.common.Validation.requireEmail;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

public record Guest(String name, String email) {

    public Guest {
        name = requireNonBlank(name, "guest.name");
        email = requireEmail(email, "guest.email");
    }
}
