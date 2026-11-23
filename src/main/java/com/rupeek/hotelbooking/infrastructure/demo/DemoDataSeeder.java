package com.rupeek.hotelbooking.infrastructure.demo;

import com.rupeek.hotelbooking.application.NewProperty;
import com.rupeek.hotelbooking.application.NewRoomType;
import com.rupeek.hotelbooking.application.PropertyOnboardingService;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.Location;
import com.rupeek.hotelbooking.domain.property.Owner;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.domain.property.StarRating;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds a hotel chain and an independent hotel so the API can be explored immediately ({@code demo} profile). */
@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final PropertyOnboardingService onboardingService;

    public DemoDataSeeder(PropertyOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @Override
    public void run(String... args) {
        Owner chain = onboardingService.registerOwner("Lakeside Hotels Group", "ops@lakeside.example");
        seed(chain, "Lakeside Indiranagar", "Bengaluru", "Indiranagar", 4, "FLEXIBLE",
                List.of(room("Deluxe King", 2, 5, "4500"), room("Family Suite", 4, 2, "8200")), "wifi", "pool", "gym");
        seed(chain, "Lakeside Koramangala", "Bengaluru", "Koramangala", 3, "NON_REFUNDABLE",
                List.of(room("Standard Twin", 2, 8, "2800")), "wifi", "breakfast");

        Owner independent = onboardingService.registerOwner("Casa Mar", "hello@casamar.example");
        seed(independent, "Casa Mar Beach House", "Goa", "Anjuna", 5, "FLEXIBLE",
                List.of(room("Sea View Room", 2, 3, "9500")), "wifi", "pool", "beach-access");
    }

    private void seed(Owner owner, String name, String city, String locality, int stars, String policy,
                      List<NewRoomType> roomTypes, String... amenities) {
        Set<Amenity> amenitySet = Arrays.stream(amenities).map(Amenity::of).collect(Collectors.toSet());
        Property property = onboardingService.addProperty(owner.id(), new NewProperty(
                name, new Location(city, locality), new StarRating(stars), amenitySet, roomTypes, policy));
        log.info("Seeded {} (owner {}): property {} room types {}", name, owner.name(), property.id(),
                property.roomTypes().stream().map(rt -> rt.name() + "=" + rt.id()).toList());
    }

    private static NewRoomType room(String name, int maxOccupancy, int units, String price) {
        return new NewRoomType(name, maxOccupancy, units, Money.of(price));
    }
}
