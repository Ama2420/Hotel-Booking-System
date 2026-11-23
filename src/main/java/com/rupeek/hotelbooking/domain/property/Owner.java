package com.rupeek.hotelbooking.domain.property;

import static com.rupeek.hotelbooking.domain.common.Validation.requireEmail;
import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

/**
 * An account that owns one or more properties. A standalone hotel is simply an owner with a single
 * property, so there is one onboarding path for both independents and chains.
 */
public record Owner(String id, String name, String email) {

    public Owner {
        id = requireNonBlank(id, "owner.id");
        name = requireNonBlank(name, "owner.name");
        email = requireEmail(email, "owner.email");
    }
}
