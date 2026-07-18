package com.bookingsystem.eventbookingsystem.controller;

import com.bookingsystem.eventbookingsystem.dto.BookingRequest;
import com.bookingsystem.eventbookingsystem.dto.BookingResponse;
import com.bookingsystem.eventbookingsystem.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse bookSeat(@RequestBody BookingRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        return bookingService.bookSeat(userEmail, request);
    }
}