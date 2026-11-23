package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.hotelWith;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.property.Owner;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.support.HotelTestContext;
import org.junit.jupiter.api.Test;

class PropertyOnboardingServiceTest {

    private final HotelTestContext ctx = new HotelTestContext();
    private final PropertyOnboardingService onboarding = ctx.onboardingService;

    @Test
    void chainOwnerManagesSeveralPropertiesUnderOneAccount() {
        Owner chain = onboarding.registerOwner("Lakeside Group", "ops@lakeside.example");
        onboarding.addProperty(chain.id(), hotelWith("FLEXIBLE", roomType("Deluxe", 2, 5, "4000")));
        onboarding.addProperty(chain.id(), hotelWith("FLEXIBLE", roomType("Suite", 4, 2, "9000")));

        assertThat(onboarding.getPropertiesOf(chain.id())).hasSize(2)
                .allMatch(property -> property.ownerId().equals(chain.id()));
    }

    @Test
    void standaloneHotelIsAnOwnerWithOneProperty() {
        Owner independent = onboarding.registerOwner("Casa Mar", "hello@casamar.example");
        Property property = onboarding.addProperty(independent.id(), hotelWith(null, roomType("Sea View", 2, 3, "9500")));

        assertThat(onboarding.getPropertiesOf(independent.id())).containsExactly(property);
        assertThat(property.cancellationPolicyCode()).isEqualTo("FLEXIBLE");
    }

    @Test
    void onboardingCreatesInventoryForEveryRoomType() {
        Property property = ctx.onboard(hotelWith("FLEXIBLE",
                roomType("Deluxe", 2, 5, "4000"), roomType("Suite", 4, 2, "9000")));

        assertThat(property.roomTypes())
                .extracting(roomType -> ctx.inventoryService.availableUnits(roomType.id(), STAY))
                .containsExactly(5, 2);
    }

    @Test
    void rejectsUnknownOwnerAndUnsupportedCancellationPolicy() {
        Owner owner = ctx.owner();

        assertThatThrownBy(() -> onboarding.addProperty("own_missing", hotelWith("FLEXIBLE", roomType("A", 2, 1, "100"))))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> onboarding.addProperty(owner.id(), hotelWith("SUPER_STRICT", roomType("A", 2, 1, "100"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPER_STRICT");
    }

    @Test
    void rejectsPropertyWithoutRoomTypesOrWithDuplicateRoomTypeNames() {
        Owner owner = ctx.owner();

        assertThatThrownBy(() -> onboarding.addProperty(owner.id(), hotelWith("FLEXIBLE")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> onboarding.addProperty(owner.id(),
                hotelWith("FLEXIBLE", roomType("Deluxe", 2, 1, "100"), roomType("deluxe", 2, 1, "200"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }
}
