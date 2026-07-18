package com.bookingsystem.eventbookingsystem;

import com.bookingsystem.eventbookingsystem.dto.BookingRequest;
import com.bookingsystem.eventbookingsystem.entity.Seat;
import com.bookingsystem.eventbookingsystem.entity.SeatStatus;
import com.bookingsystem.eventbookingsystem.repository.SeatRepository;
import com.bookingsystem.eventbookingsystem.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatRepository seatRepository;

    private static final Long TEST_SEAT_ID = 21L;
    private static final String TEST_USER_EMAIL = "nikhil@test.com";
    private static final int THREAD_COUNT = 20;

    @BeforeEach
    void resetSeat() {
        // Ensure the seat is AVAILABLE before each test run
        Seat seat = seatRepository.findById(TEST_SEAT_ID)
                .orElseThrow(() -> new RuntimeException("Test seat not found — create it first"));
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }

    @Test
    void onlyOneThreadShouldSuccessfullyBookTheSameSeat() throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // all threads wait here, then fire together

                    BookingRequest request = new BookingRequest();
                    request.setSeatId(TEST_SEAT_ID);
                    request.setIdempotencyKey("concurrency-test-" + threadId);

                    bookingService.bookSeat(TEST_USER_EMAIL, request);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();       // wait until all 20 threads are ready
        startLatch.countDown();   // release them all at once
        doneLatch.await(10, TimeUnit.SECONDS); // wait for all to finish
        executor.shutdown();

        System.out.println("Successful bookings: " + successCount.get());
        System.out.println("Failed bookings: " + failureCount.get());

        assertEquals(1, successCount.get(), "Exactly one booking should succeed");
        assertEquals(THREAD_COUNT - 1, failureCount.get(), "All other attempts should fail");
    }
}