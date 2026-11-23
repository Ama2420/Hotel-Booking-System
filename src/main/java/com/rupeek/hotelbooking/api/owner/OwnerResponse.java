package com.rupeek.hotelbooking.api.owner;

import com.rupeek.hotelbooking.domain.property.Owner;

public record OwnerResponse(String id, String name, String email) {

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.id(), owner.name(), owner.email());
    }
}
