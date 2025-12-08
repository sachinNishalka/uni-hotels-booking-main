package lk.spence.uni_hotels_booking_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private String correlationId;
    private String bookingId;
    private String status;
    private String message;
}