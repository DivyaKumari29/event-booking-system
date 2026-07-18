package com.bookingsystem.eventbookingsystem.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private Long seatId;
    private String idempotencyKey;
}