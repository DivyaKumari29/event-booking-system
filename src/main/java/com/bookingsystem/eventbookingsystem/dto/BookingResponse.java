package com.bookingsystem.eventbookingsystem.dto;

import com.bookingsystem.eventbookingsystem.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long seatId;
    private String seatNumber;
    private BookingStatus status;
    private LocalDateTime bookingTime;
}