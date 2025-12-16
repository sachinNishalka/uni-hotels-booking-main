package lk.spence.uni_hotels_booking_main.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lk.spence.uni_hotels_booking_main.dto.BookingResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final RestTemplate restTemplate;

    // Mapping: hotelId (from Opera) -> Microservice URL
    @Value("${hotel.service.acr.url}")
    private String acrServiceUrl; // Service 1 - for hotelId "ACR"

    @Value("${hotel.service.har.url:http://localhost:8083}")
    private String harServiceUrl;

    @Value("${hotel.service.hrf.url:http://localhost:8084}")
    private String hrfServiceUrl;

    private Map<String, String> getHotelServiceMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("ACR", acrServiceUrl); // hotelId "ACR" -> Service 1
        mapping.put("HAR", harServiceUrl); // hotelId "HAR" -> Service 2
        mapping.put("HRF", hrfServiceUrl); // hotelId "HRF" -> Service 3
        // Add more mappings as needed
        return mapping;
    }

    public BookingResponseDTO routeOperaBooking(
            String correlationId,
            String hotelId,
            JsonNode operaJson) {

        hotelId = hotelId.toUpperCase();
        String serviceUrl = getHotelServiceMapping().get(hotelId);

        if (serviceUrl == null) {
            log.error("No microservice configured for hotelId: {}", hotelId);
            throw new IllegalArgumentException("Unknown hotelId: " + hotelId +
                    ". No microservice configured for this hotel.");
        }

        log.info("Routing Opera booking to hotel microservice for hotelId: {} at {}",
                hotelId, serviceUrl);

        try {
            // Prepare request with correlation ID header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // headers.set("X-Correlation-Id", correlationId);
            headers.set("X-Hotel-Id", hotelId); // Additional context for hotel service

            // Forward ENTIRE Opera JSON unchanged to hotel microservice
            HttpEntity<JsonNode> entity = new HttpEntity<>(operaJson, headers);

            String endpoint = serviceUrl;
            log.debug("Forwarding to endpoint: {}", endpoint);

            ResponseEntity<BookingResponseDTO> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    BookingResponseDTO.class);

            BookingResponseDTO responseBody = response.getBody();
            if (responseBody != null) {
                responseBody.setCorrelationId(correlationId);
            } else {
                // Fallback if hotel service doesn't return proper response
                responseBody = new BookingResponseDTO(
                        correlationId, null, "FORWARDED",
                        "Booking forwarded to hotel service");
            }

            log.info("Received response from hotel microservice {}: {}", hotelId, responseBody);
            return responseBody;

        } catch (HttpClientErrorException e) {
            // Hotel microservice validation failed (400-499)
            log.error("Validation error from hotel service {}: {} - {}",
                    hotelId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Hotel service validation failed: " +
                    e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            // Hotel microservice internal error (500-599)
            log.error("Server error from hotel service {}: {} - {}",
                    hotelId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Hotel service error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Error communicating with hotel service {}: {}",
                    hotelId, e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with hotel service: " +
                    e.getMessage());
        }
    }

}
