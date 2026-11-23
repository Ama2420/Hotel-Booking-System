package com.rupeek.hotelbooking.infrastructure.persistence;

import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.PropertyRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPropertyRepository extends InMemoryRepository<Property> implements PropertyRepository {

    public InMemoryPropertyRepository() {
        super(Property::id);
    }

    @Override
    public List<Property> findByCity(String city) {
        return all().filter(property -> property.location().isInCity(city)).toList();
    }

    @Override
    public List<Property> findByOwnerId(String ownerId) {
        return all().filter(property -> property.ownerId().equals(ownerId)).toList();
    }
}
