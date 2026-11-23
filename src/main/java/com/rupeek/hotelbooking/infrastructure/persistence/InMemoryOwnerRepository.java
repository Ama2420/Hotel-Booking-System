package com.rupeek.hotelbooking.infrastructure.persistence;

import com.rupeek.hotelbooking.domain.property.Owner;
import com.rupeek.hotelbooking.domain.property.OwnerRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOwnerRepository extends InMemoryRepository<Owner> implements OwnerRepository {

    public InMemoryOwnerRepository() {
        super(Owner::id);
    }
}
