#!/usr/bin/env bash
# End-to-end walkthrough against a running server started with the demo profile:
#   ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
# then, in another terminal:
#   ./scripts/demo.sh
# Requires curl and python3 (used only to pretty-print and extract ids).
set -euo pipefail

BASE="${BASE_URL:-http://localhost:8080}/api/v1"
CHECK_IN=$(python3 -c 'import datetime as d; print(d.date.today() + d.timedelta(days=10))')
CHECK_OUT=$(python3 -c 'import datetime as d; print(d.date.today() + d.timedelta(days=12))')

pretty() { python3 -m json.tool; }
field() { python3 -c "import json,sys; print(json.load(sys.stdin)$1)"; }
step() { printf '\n\033[1m== %s\033[0m\n' "$1"; }

step "Search Bengaluru, 2 guests, with a pool ($CHECK_IN -> $CHECK_OUT)"
SEARCH=$(curl -sf "$BASE/properties/search?city=Bengaluru&checkIn=$CHECK_IN&checkOut=$CHECK_OUT&guests=2&amenities=pool")
echo "$SEARCH" | pretty
PROPERTY_ID=$(echo "$SEARCH" | field '[0]["propertyId"]')
ROOM_TYPE_ID=$(echo "$SEARCH" | field '[0]["availableRooms"][0]["roomTypeId"]')

step "Book 2 rooms for 3 guests (inventory is held for 15 minutes)"
BOOKING=$(curl -sf -X POST "$BASE/bookings" -H 'Content-Type: application/json' -d @- <<EOF
{"propertyId": "$PROPERTY_ID", "roomTypeId": "$ROOM_TYPE_ID",
 "guest": {"name": "Asha Rao", "email": "asha@example.com"},
 "checkIn": "$CHECK_IN", "checkOut": "$CHECK_OUT", "guests": 3, "rooms": 2}
EOF
)
echo "$BOOKING" | pretty
BOOKING_ID=$(echo "$BOOKING" | field '["id"]')

step "Pay with a UPI handle the mock gateway declines -> booking stays PENDING_PAYMENT"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/payments" -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: attempt-1' -d '{"method": "UPI", "vpa": "fail@okaxis"}' | pretty

step "Retry with a card and a new Idempotency-Key -> booking CONFIRMED"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/payments" -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: attempt-2' -d '{"method": "CARD", "cardNumber": "4111111111111111", "holderName": "Asha Rao"}' | pretty

step "Replay attempt-2 (e.g. client timeout) -> same payment returned, no second charge"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/payments" -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: attempt-2' -d '{"method": "CARD", "cardNumber": "4111111111111111", "holderName": "Asha Rao"}' | pretty

step "Pay again with a fresh key -> 409, booking is already confirmed"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/payments" -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: attempt-3' -d '{"method": "UPI", "vpa": "asha@okaxis"}' | pretty

step "Cancel -> FLEXIBLE policy refunds in full and releases both rooms"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/cancellation" | pretty

step "Cancel again -> 409 invalid state transition"
curl -s -X POST "$BASE/bookings/$BOOKING_ID/cancellation" | pretty

step "Validation: 5 guests in one 2-person room -> 422"
curl -s -X POST "$BASE/bookings" -H 'Content-Type: application/json' -d @- <<EOF | pretty
{"propertyId": "$PROPERTY_ID", "roomTypeId": "$ROOM_TYPE_ID",
 "guest": {"name": "Asha Rao", "email": "asha@example.com"},
 "checkIn": "$CHECK_IN", "checkOut": "$CHECK_OUT", "guests": 5}
EOF
