package com.rupeek.hotelbooking.api.owner;

import com.rupeek.hotelbooking.api.property.LocationDto;
import com.rupeek.hotelbooking.application.NewProperty;
import com.rupeek.hotelbooking.application.NewRoomType;
import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.property.Amenity;
import com.rupeek.hotelbooking.domain.property.StarRating;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AddPropertyRequest(
        @NotBlank String name,
        @NotNull @Valid LocationDto location,
        @NotNull @Min(1) @Max(5) Integer starRating,
        Set<String> amenities,
        @NotEmpty List<@Valid RoomTypeRequest> roomTypes,
        @Schema(description = "FLEXIBLE (default) or NON_REFUNDABLE") String cancellationPolicy) {

    public record RoomTypeRequest(
            @NotBlank String name,
            @NotNull @Positive Integer maxOccupancy,
            @NotNull @Positive Integer totalUnits,
            @NotNull @Positive BigDecimal pricePerNight) {

        NewRoomType toCommand() {
            return new NewRoomType(name, maxOccupancy, totalUnits, Money.of(pricePerNight));
        }
    }

    public NewProperty toCommand() {
        return new NewProperty(
                name,
                location.toDomain(),
                new StarRating(starRating),
                amenities == null ? Set.of() : amenities.stream().map(Amenity::of).collect(Collectors.toSet()),
                roomTypes.stream().map(RoomTypeRequest::toCommand).toList(),
                cancellationPolicy);
    }
}
