# Hotel Booking Service

A Spring Boot backend for a hotel booking platform. Owners onboard properties, and guests search for available rooms, book, pay and cancel.

- **Stack:** Java 17+, Spring Boot 3.3, in-memory storage behind repository interfaces, mocked payment gateway.
- **Bonus items:** concurrent-booking safety, pluggable pricing, idempotent payments, OpenAPI/Swagger.

The full API reference, diagrams and reasoning behind each decision are in **[docs/DESIGN.md](docs/DESIGN.md)**.

## Run

JDK 17+ is required. Maven is not needed because the wrapper is included.

```bash
./mvnw test                                              # 84 tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo   # :8080 with seed data
./scripts/demo.sh                                        # walk through the flow (needs the app running)
```

Swagger UI is at <http://localhost:8080/swagger-ui.html>. The mock gateway declines a card ending in `0002` and any UPI handle or wallet id containing `fail`; everything else succeeds.

| Operation | Endpoint |
|---|---|
| Register owner / onboard property | `POST /api/v1/owners`, `POST /api/v1/owners/{id}/properties` |
| Search | `GET /api/v1/properties/search?city=&checkIn=&checkOut=&guests=` + optional `locality`, `rooms`, `minPrice`, `maxPrice`, `amenities`, `minStars` |
| Book | `POST /api/v1/bookings` |
| Pay | `POST /api/v1/bookings/{id}/payments` (`Idempotency-Key` header required) |
| Cancel | `POST /api/v1/bookings/{id}/cancellation` |

## Key design decisions

- **Layers.** `api → application → domain ← infrastructure`. The domain has no Spring imports and defines its own ports (repositories, `PaymentGateway`, `LockManager`).
- **Single vs chain.** An owner has *n* properties; a standalone hotel is *n = 1*. There is one onboarding path, with no special case.
- **Inventory is a per-night ledger** per room type. Availability is `total − max(reserved over the stay's nights)`, so double-booking is impossible and search and booking share one source of truth.
- **Booking lifecycle.** `PENDING_PAYMENT → CONFIRMED → CANCELLED`, or `PENDING_PAYMENT → EXPIRED`, enforced by a transition table. Rooms are held when the booking is created and released if payment doesn't arrive within 15 minutes, on cancellation, or by a periodic sweep.
- **Payment.** A declined payment keeps the booking payable for a retry; success confirms it. Each method is a `PaymentProcessor` behind one gateway port. Payments are idempotent per booking and `Idempotency-Key`.
- **Concurrency.** Locks are keyed per room type for inventory and per booking for payment, cancellation and expiry, always taken booking-first. A test races 64 threads for 5 rooms and asserts exactly 5 succeed. In production this maps to conditional updates and row locks.
- **Extension points.** `SearchFilter`, `PaymentProcessor`, `CancellationPolicy` and `PricingStrategy` are each injected as a list of beans. Adding a new one is a new class and bean; the code that uses them doesn't change.
- **Terms frozen at booking.** Price and cancellation policy are copied onto the booking, so later property changes don't affect it.

## Assumptions

- Single currency (INR). No authentication: the guest is identified by name and email.
- A booking covers one room type, with `rooms ≥ 1`, and `guests ≤ rooms × maxOccupancy`. Inventory is counted per room type, not per physical room.
- Check-in is at 14:00, check-in can't be in the past, stays are at most 30 nights, and the payment hold is 15 minutes. All of these are configurable.
- Cancellation policies:
  - **FLEXIBLE** (default): full refund until 24 hours before check-in, then the first night is kept.
  - **NON_REFUNDABLE**: no refund.
- No cancellation after check-in. A failed refund aborts the cancellation.

## With more time

- PostgreSQL with migrations; a transactional, conditional-update inventory table; a DB unique constraint on idempotency keys.
- Retry refunds through an outbox; handle asynchronous payments (UPI collect, 3-D Secure) via gateway webhooks.
- Search indexing, caching and pagination; rate plans and dynamic pricing; bookings with several room types.
- Authentication and authorisation, observability, rate limiting.
