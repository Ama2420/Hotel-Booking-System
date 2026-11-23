package com.rupeek.hotelbooking.domain.inventory;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;
import static com.rupeek.hotelbooking.domain.common.Validation.requirePositive;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nightly inventory ledger for one room type: how many of its units are reserved on each night.
 *
 * <p>A stay of N nights needs the requested units free on every one of those nights, so availability for a range
 * is {@code totalUnits - max(reserved over the range)}. The check-out date is not a night of the stay.
 *
 * <p>Check-then-reserve is a compound operation; callers must serialise mutations per room type
 * (see {@code InventoryService}). Reads are safe to run concurrently.
 */
public class RoomInventory {

    private final String roomTypeId;
    private final int totalUnits;
    private final Map<LocalDate, Integer> reservedUnitsByNight = new ConcurrentHashMap<>();

    public RoomInventory(String roomTypeId, int totalUnits) {
        this.roomTypeId = requireNonBlank(roomTypeId, "roomTypeId");
        this.totalUnits = requirePositive(totalUnits, "totalUnits");
    }

    public int availableUnits(DateRange stay) {
        int peakReserved = stay.nights().mapToInt(this::reservedOn).max().orElse(0);
        return totalUnits - peakReserved;
    }

    public void reserve(DateRange stay, int units) {
        requirePositive(units, "units");
        int available = availableUnits(stay);
        if (available < units) {
            throw new InsufficientInventoryException(roomTypeId, units, available);
        }
        stay.nights().forEach(night -> reservedUnitsByNight.merge(night, units, Integer::sum));
    }

    public void release(DateRange stay, int units) {
        requirePositive(units, "units");
        if (stay.nights().anyMatch(night -> reservedOn(night) < units)) {
            throw new IllegalStateException("cannot release more units than reserved for room type " + roomTypeId);
        }
        stay.nights().forEach(night ->
                reservedUnitsByNight.computeIfPresent(night, (date, reserved) -> reserved == units ? null : reserved - units));
    }

    public int reservedOn(LocalDate night) {
        return reservedUnitsByNight.getOrDefault(night, 0);
    }

    public String roomTypeId() {
        return roomTypeId;
    }

    public int totalUnits() {
        return totalUnits;
    }
}
