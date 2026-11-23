package com.rupeek.hotelbooking.api.booking;

import com.rupeek.hotelbooking.application.BookingService;
import com.rupeek.hotelbooking.application.CancellationService;
import com.rupeek.hotelbooking.application.PaymentService;
import com.rupeek.hotelbooking.domain.payment.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final CancellationService cancellationService;

    public BookingController(BookingService bookingService, PaymentService paymentService,
                             CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.cancellationService = cancellationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book rooms; inventory is held until payment or until the hold lapses")
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        return BookingResponse.from(bookingService.createBooking(request.toDomain()));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get a booking")
    public BookingResponse get(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pay for a booking. A success confirms it; a decline leaves it payable until the hold lapses. "
            + "Retrying with the same Idempotency-Key returns the original payment without charging again.")
    public PaymentResponse pay(
            @PathVariable String bookingId,
            @Parameter(description = "Client-generated key, unique per payment attempt")
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentInstrumentRequest request) {
        Payment payment = paymentService.pay(bookingId, idempotencyKey, request.toDomain());
        return PaymentResponse.from(payment, bookingService.getBooking(bookingId).status());
    }

    @PostMapping("/{bookingId}/cancellation")
    @Operation(summary = "Cancel a booking, refund per its cancellation policy, and release the rooms")
    public BookingResponse cancel(@PathVariable String bookingId) {
        return BookingResponse.from(cancellationService.cancel(bookingId));
    }
}
