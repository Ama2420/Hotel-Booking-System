package com.rupeek.hotelbooking.api.property;

import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.search.SearchQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

/** Query parameters for property search. Prices are per room per night. */
public record SearchPropertiesRequest(
        @NotBlank String city,
        String locality,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @NotNull @Positive Integer guests,
        @Positive Integer rooms,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,
        Set<String> amenities,
        @Min(1) @Max(5) Integer minStars) {

    public SearchQuery toQuery() {
        return new SearchQuery(
                city,
                locality,
                new DateRange(checkIn, checkOut),
                guests,
                rooms == null ? 1 : rooms,
                minPrice == null ? null : Money.of(minPrice),
                maxPrice == null ? null : Money.of(maxPrice),
                amenities == null ? Set.of() : amenities.stream().map(Amenity::of).collect(Collectors.toSet()),
                minStars);
    }
}
