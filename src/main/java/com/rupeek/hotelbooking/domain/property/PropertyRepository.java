package com.rupeek.hotelbooking.domain.property;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(String id);

    List<Property> findByCity(String city);

    List<Property> findByOwnerId(String ownerId);
}
