package com.bookingsystem.eventbookingsystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Async
    public void sendBookingConfirmation(String toEmail, String seatNumber, Long bookingId) {
        try {
            // Simulate network delay of a real email provider
            Thread.sleep(2000);
            log.info(" [ASYNC EMAIL SENT] To: {} | Subject: Booking Confirmed | Seat: {} | Booking ID: {}",
                    toEmail, seatNumber, bookingId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}