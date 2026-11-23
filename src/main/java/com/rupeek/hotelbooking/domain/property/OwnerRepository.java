package com.rupeek.hotelbooking.domain.property;

import java.util.Optional;

public interface OwnerRepository {

    Owner save(Owner owner);

    Optional<Owner> findById(String id);
}
