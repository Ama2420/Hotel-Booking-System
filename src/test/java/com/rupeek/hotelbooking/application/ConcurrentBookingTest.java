package com.rupeek.hotelbooking.application;

import static com.rupeek.hotelbooking.support.TestData.STAY;
import static com.rupeek.hotelbooking.support.TestData.bookingFor;
import static com.rupeek.hotelbooking.support.TestData.hotelWith;
import static com.rupeek.hotelbooking.support.TestData.roomType;
import static org.assertj.core.api.Assertions.assertThat;

import com.rupeek.hotelbooking.domain.inventory.InsufficientInventoryException;
import com.rupeek.hotelbooking.domain.payment.Payment;
import com.rupeek.hotelbooking.domain.payment.UpiInstrument;
import com.rupeek.hotelbooking.domain.property.Property;
import com.rupeek.hotelbooking.support.HotelTestContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ConcurrentBookingTest {

    private static final int THREADS = 64;

    private final HotelTestContext ctx = new HotelTestContext();

    @RepeatedTest(5)
    void concurrentRequestsNeverOversellTheLastRooms() throws Exception {
        int units = 5;
        Property hotel = ctx.onboard(hotelWith("FLEXIBLE", roomType("Deluxe", 2, units, "3000")));
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();

        runConcurrently(() -> {
            try {
                ctx.bookingService.createBooking(bookingFor(hotel));
                succeeded.incrementAndGet();
            } catch (InsufficientInventoryException e) {
                soldOut.incrementAndGet();
            }
            return null;
        });

        assertThat(succeeded.get()).isEqualTo(units);
        assertThat(soldOut.get()).isEqualTo(THREADS - units);
        assertThat(ctx.inventoryService.availableUnits(hotel.roomTypes().get(0).id(), STAY)).isZero();
    }

    @Test
    void concurrentRetriesWithTheSameIdempotencyKeyChargeOnce() throws Exception {
        Property hotel = ctx.onboard(hotelWith("FLEXIBLE", roomType("Deluxe", 2, 1, "3000")));
        String bookingId = ctx.bookingService.createBooking(bookingFor(hotel)).id();

        List<Payment> payments = runConcurrently(() ->
                ctx.paymentService.pay(bookingId, "retry-key", new UpiInstrument("asha@okaxis")));

        assertThat(payments).extracting(Payment::id).containsOnly(payments.get(0).id());
        assertThat(ctx.gateway.charges()).isEqualTo(1);
    }

    private <T> List<T> runConcurrently(Callable<T> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
