package com.bookingsystem.eventbookingsystem.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private final Random random = new Random();

    public boolean processPayment(Long bookingId, double amount) {
        try {
            // Simulate payment gateway latency
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate ~80% success rate
        return random.nextInt(100) < 80;
    }
}