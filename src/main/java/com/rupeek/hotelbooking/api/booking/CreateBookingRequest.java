package com.rupeek.hotelbooking.api.booking;

import com.rupeek.hotelbooking.domain.booking.BookingRequest;
import com.rupeek.hotelbooking.domain.booking.DateRange;
import com.rupeek.hotelbooking.domain.booking.Guest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateBookingRequest(
        @NotBlank String propertyId,
        @NotBlank String roomTypeId,
        @NotNull @Valid GuestDto guest,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @Positive Integer guests,
        @Positive Integer rooms) {

    public record GuestDto(@NotBlank String name, @NotBlank @Email String email) {

        static GuestDto from(Guest guest) {
            return new GuestDto(guest.name(), guest.email());
        }
    }

    public BookingRequest toDomain() {
        return new BookingRequest(
                propertyId,
                roomTypeId,
                new Guest(guest.name(), guest.email()),
                new DateRange(checkIn, checkOut),
                guests,
                rooms == null ? 1 : rooms);
    }
}
