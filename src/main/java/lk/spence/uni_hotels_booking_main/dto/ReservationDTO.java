package lk.spence.uni_hotels_booking_main.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationDTO {

    @Schema(description = "Container for reservation data")
    private Reservations reservations;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reservations {
        @Schema(description = "Array of reservations (typically one)")
        private List<Reservation> reservation;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reservation {
        @Schema(description = "Hotel identifier (e.g., ACR, HAR, HRF)", example = "ACR", required = true)
        private String hotelId;
    }

}
