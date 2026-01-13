package lk.spence.uni_hotels_booking_main.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody; // Spring's @RequestBody

import lk.spence.uni_hotels_booking_main.dto.BookingResponseDTO;
import lk.spence.uni_hotels_booking_main.dto.ReservationDTO;
import lk.spence.uni_hotels_booking_main.service.BookingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    private String extractHotelId(JsonNode json) {
        try {
            return json.path("reservations")
                    .path("reservation")
                    .get(0)
                    .path("hotelId")
                    .asText(null);
        } catch (Exception e) {
            log.error("Failed to extract hotelId from JSON", e);
            return null;
        }
    }

    @PostMapping("/bookings")
    public ResponseEntity receiveBooking(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Opera PMS format reservation", required = true, content = @Content(schema = @Schema(implementation = ReservationDTO.class)))

            @RequestBody JsonNode operaBookingJson) {

        // Generate correlation ID for distributed tracing
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        try {
            log.info("Received Opera PMS booking from Uni Hotels Platform");
            log.debug("Full booking payload: {}", operaBookingJson);

            // Extract hotelId for routing (e.g., "ACR")
            String hotelId = extractHotelId(operaBookingJson);

            if (hotelId == null || hotelId.isEmpty()) {
                log.error("hotelId not found in booking payload");
                return ResponseEntity.badRequest()
                        .body(new BookingResponseDTO(correlationId, null, "FAILED",
                                "hotelId is required in reservation"));
            }
            log.info("Extracted hotelId: {} from booking", hotelId);

            // Forward entire Opera JSON to appropriate hotel microservice
            BookingResponseDTO response = bookingService.routeOperaBooking(
                    correlationId, hotelId, operaBookingJson);

            log.info("Successfully forwarded booking to hotel service. Correlation ID: {}", correlationId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid hotel ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new BookingResponseDTO(correlationId, null, "FAILED", e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing booking request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BookingResponseDTO(correlationId, null, "FAILED",
                            "Internal error: " + e.getMessage()));
        }

    }

//    TODO: get the hotel name as a path variable
    @GetMapping("/bookings")
    public ResponseEntity<String> getBookings(){
//        TODO: pass the path variable to here
        return bookingService.getBookings();
    }
}
