package com.rupeek.hotelbooking.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Drives the whole guest journey over HTTP: onboard, search, book, pay, cancel, and search again. */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {

    private static final LocalDate CHECK_IN = LocalDate.now().plusDays(20);
    private static final LocalDate CHECK_OUT = CHECK_IN.plusDays(2);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void guestCanFindBookPayAndCancel() throws Exception {
        String ownerId = read(postJson("/api/v1/owners", """
                {"name": "Lakeside Group", "email": "ops@lakeside.example"}""")
                .andExpect(status().isCreated())).get("id").asText();

        JsonNode property = read(postJson("/api/v1/owners/" + ownerId + "/properties", """
                {"name": "Lakeside Pune", "location": {"city": "Pune", "locality": "Baner"}, "starRating": 4,
                 "amenities": ["wifi", "pool"],
                 "roomTypes": [{"name": "Deluxe", "maxOccupancy": 2, "totalUnits": 1, "pricePerNight": 4000}]}""")
                .andExpect(status().isCreated()));
        String propertyId = property.get("id").asText();
        String roomTypeId = property.at("/roomTypes/0/id").asText();

        searchPune().andExpect(jsonPath("$[*].propertyId", hasItem(propertyId)));

        String bookingId = read(postJson("/api/v1/bookings", """
                {"propertyId": "%s", "roomTypeId": "%s", "guest": {"name": "Asha", "email": "asha@example.com"},
                 "checkIn": "%s", "checkOut": "%s", "guests": 2}""".formatted(propertyId, roomTypeId, CHECK_IN, CHECK_OUT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(8000.0))).get("id").asText();

        searchPune().andExpect(jsonPath("$[*].propertyId", not(hasItem(propertyId))));

        mvc.perform(post("/api/v1/bookings/" + bookingId + "/payments")
                        .header("Idempotency-Key", "pay-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\": \"UPI\", \"vpa\": \"asha@okaxis\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        mvc.perform(post("/api/v1/bookings/" + bookingId + "/cancellation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(8000.0));

        searchPune().andExpect(jsonPath("$[*].propertyId", hasItem(propertyId)));
    }

    @Test
    void invalidRequestsAreRejectedWithProblemDetails() throws Exception {
        postJson("/api/v1/owners", "{\"name\": \"\", \"email\": \"not-an-email\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        mvc.perform(get("/api/v1/bookings/bk_missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Booking 'bk_missing' not found"));

        mvc.perform(get("/api/v1/properties/search").param("city", "Pune")
                        .param("checkIn", CHECK_OUT.toString()).param("checkOut", CHECK_IN.toString())
                        .param("guests", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentRequiresAnIdempotencyKeyAndAKnownMethod() throws Exception {
        mvc.perform(post("/api/v1/bookings/bk_any/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\": \"UPI\", \"vpa\": \"asha@okaxis\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/bookings/bk_any/payments")
                        .header("Idempotency-Key", "k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\": \"CASH\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchCanFilterByStarsAndAmenities() throws Exception {
        String ownerId = read(postJson("/api/v1/owners", """
                {"name": "Filter Group", "email": "filters@example.com"}""")).get("id").asText();
        postJson("/api/v1/owners/" + ownerId + "/properties", """
                {"name": "Jaipur Haveli", "location": {"city": "Jaipur"}, "starRating": 5, "amenities": ["spa"],
                 "roomTypes": [{"name": "Royal", "maxOccupancy": 2, "totalUnits": 2, "pricePerNight": 9000}]}""");
        postJson("/api/v1/owners/" + ownerId + "/properties", """
                {"name": "Jaipur Lodge", "location": {"city": "Jaipur"}, "starRating": 2,
                 "roomTypes": [{"name": "Basic", "maxOccupancy": 2, "totalUnits": 2, "pricePerNight": 1200}]}""");

        mvc.perform(get("/api/v1/properties/search").param("city", "Jaipur")
                        .param("checkIn", CHECK_IN.toString()).param("checkOut", CHECK_OUT.toString())
                        .param("guests", "2").param("minStars", "4").param("amenities", "spa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Jaipur Haveli")));
    }

    private ResultActions searchPune() throws Exception {
        return mvc.perform(get("/api/v1/properties/search")
                        .param("city", "Pune")
                        .param("checkIn", CHECK_IN.toString())
                        .param("checkOut", CHECK_OUT.toString())
                        .param("guests", "2"))
                .andExpect(status().isOk());
    }

    private ResultActions postJson(String path, String body) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
