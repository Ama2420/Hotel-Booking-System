package com.rupeek.hotelbooking.infrastructure.persistence;

import com.rupeek.hotelbooking.domain.inventory.InventoryRepository;
import com.rupeek.hotelbooking.domain.inventory.RoomInventory;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryInventoryRepository extends InMemoryRepository<RoomInventory> implements InventoryRepository {

    public InMemoryInventoryRepository() {
        super(RoomInventory::roomTypeId);
    }

    @Override
    public Optional<RoomInventory> findByRoomTypeId(String roomTypeId) {
        return findById(roomTypeId);
    }
}
