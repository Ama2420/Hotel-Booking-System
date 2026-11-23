package com.rupeek.hotelbooking.domain.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RoomInventoryTest {

    private static final LocalDate DEC_10 = LocalDate.of(2026, 12, 10);

    private static DateRange nights(int fromOffset, int toOffset) {
        return new DateRange(DEC_10.plusDays(fromOffset), DEC_10.plusDays(toOffset));
    }

    @Test
    void reservingConsumesUnitsOnEveryNightOfTheStay() {
        RoomInventory inventory = new RoomInventory("rt_1", 3);
        inventory.reserve(nights(0, 3), 2);

        assertThat(inventory.availableUnits(nights(0, 3))).isEqualTo(1);
        assertThat(inventory.availableUnits(nights(2, 4))).isEqualTo(1);
    }

    @Test
    void checkOutDayIsFreeForTheNextGuest() {
        RoomInventory inventory = new RoomInventory("rt_1", 1);
        inventory.reserve(nights(0, 2), 1);

        assertThat(inventory.availableUnits(nights(2, 4))).isEqualTo(1);
        inventory.reserve(nights(2, 4), 1);
    }

    @Test
    void availabilityIsLimitedByTheBusiestNight() {
        RoomInventory inventory = new RoomInventory("rt_1", 3);
        inventory.reserve(nights(1, 2), 3);

        assertThat(inventory.availableUnits(nights(0, 5))).isZero();
        assertThat(inventory.availableUnits(nights(0, 1))).isEqualTo(3);
    }

    @Test
    void rejectsOverlappingReservationBeyondCapacityWithoutPartialEffect() {
        RoomInventory inventory = new RoomInventory("rt_1", 1);
        inventory.reserve(nights(1, 2), 1);

        assertThatThrownBy(() -> inventory.reserve(nights(0, 3), 1))
                .isInstanceOf(InsufficientInventoryException.class);
        assertThat(inventory.reservedOn(DEC_10)).isZero();
        assertThat(inventory.reservedOn(DEC_10.plusDays(2))).isZero();
    }

    @Test
    void releaseMakesUnitsAvailableAgain() {
        RoomInventory inventory = new RoomInventory("rt_1", 2);
        inventory.reserve(nights(0, 2), 2);
        inventory.release(nights(0, 2), 2);

        assertThat(inventory.availableUnits(nights(0, 2))).isEqualTo(2);
    }

    @Test
    void cannotReleaseUnitsThatWereNeverReserved() {
        RoomInventory inventory = new RoomInventory("rt_1", 2);
        inventory.reserve(nights(0, 1), 1);

        assertThatThrownBy(() -> inventory.release(nights(0, 2), 1)).isInstanceOf(IllegalStateException.class);
        assertThat(inventory.reservedOn(DEC_10)).isEqualTo(1);
    }
}
