package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.inventory.InventoryRepository;
import com.rupeek.hotelbooking.domain.inventory.RoomInventory;
import com.rupeek.hotelbooking.domain.property.RoomType;
import org.springframework.stereotype.Service;

/**
 * The single gateway to room inventory. Every reserve/release runs under a per-room-type lock, so concurrent
 * bookings for the same room type are serialised while different room types proceed in parallel.
 */
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final LockManager lockManager;

    public InventoryService(InventoryRepository inventoryRepository, LockManager lockManager) {
        this.inventoryRepository = inventoryRepository;
        this.lockManager = lockManager;
    }

    public void initialise(RoomType roomType) {
        inventoryRepository.save(new RoomInventory(roomType.id(), roomType.totalUnits()));
    }

    /** Unlocked read, so it may be momentarily stale; {@link #reserve} re-checks under the lock. */
    public int availableUnits(String roomTypeId, DateRange stay) {
        return inventoryOf(roomTypeId).availableUnits(stay);
    }

    public void reserve(String roomTypeId, DateRange stay, int units) {
        lockManager.runWithLock(LockKeys.inventory(roomTypeId), () -> {
            RoomInventory inventory = inventoryOf(roomTypeId);
            inventory.reserve(stay, units);
            inventoryRepository.save(inventory);
        });
    }

    public void release(String roomTypeId, DateRange stay, int units) {
        lockManager.runWithLock(LockKeys.inventory(roomTypeId), () -> {
            RoomInventory inventory = inventoryOf(roomTypeId);
            inventory.release(stay, units);
            inventoryRepository.save(inventory);
        });
    }

    private RoomInventory inventoryOf(String roomTypeId) {
        return inventoryRepository.findByRoomTypeId(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for room type", roomTypeId));
    }
}
