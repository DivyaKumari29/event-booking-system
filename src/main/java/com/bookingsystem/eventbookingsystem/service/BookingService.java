package com.bookingsystem.eventbookingsystem.service;

import com.bookingsystem.eventbookingsystem.dto.BookingRequest;
import com.bookingsystem.eventbookingsystem.dto.BookingResponse;
import com.bookingsystem.eventbookingsystem.entity.*;
import com.bookingsystem.eventbookingsystem.repository.BookingRepository;
import com.bookingsystem.eventbookingsystem.repository.SeatRepository;
import com.bookingsystem.eventbookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PaymentService paymentService;
    private final EmailService emailService;

    @Transactional
    public BookingResponse bookSeat(String userEmail, BookingRequest request) {

        // Idempotency check — if this exact request was already processed, return the existing result
        if (request.getIdempotencyKey() != null) {
            var existing = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                Booking b = existing.get();
                return new BookingResponse(b.getId(), b.getSeat().getId(), b.getSeat().getSeatNumber(),
                        b.getStatus(), b.getBookingTime());
            }
        }

        // Lock the seat row — no other transaction can touch it until we commit/rollback
        Seat seat = seatRepository.findByIdForUpdate(request.getSeatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat is no longer available");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 1: Tentatively lock the seat while payment is processed
        seat.setStatus(SeatStatus.LOCKED);
        seatRepository.save(seat);

        Booking booking = Booking.builder()
                .user(user)
                .seat(seat)
                .event(seat.getEvent())
                .status(BookingStatus.PENDING)
                .bookingTime(LocalDateTime.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();
        bookingRepository.save(booking);

        // Step 2: Process payment (simulated — ~80% success rate)
        boolean paymentSuccess = paymentService.processPayment(booking.getId(), seat.getPrice());

        if (!paymentSuccess) {
            // Throwing here rolls back the ENTIRE transaction — seat reverts to AVAILABLE automatically,
            // and the booking row is never actually persisted. No manual cleanup needed.
            throw new RuntimeException("Payment failed — seat has been released");
        }

        // Step 3: Payment succeeded — confirm the booking
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Broadcast the confirmed seat status to everyone watching this event
        messagingTemplate.convertAndSend(
                "/topic/seats/" + seat.getEvent().getId(),
                new SeatUpdateMessage(seat.getId(), seat.getSeatNumber(), seat.getStatus().name())
        );

        // Fire-and-forget async email — does NOT block this response
        emailService.sendBookingConfirmation(user.getEmail(), seat.getSeatNumber(), booking.getId());

        return new BookingResponse(
                booking.getId(),
                seat.getId(),
                seat.getSeatNumber(),
                booking.getStatus(),
                booking.getBookingTime()
        );
    }

    public record SeatUpdateMessage(Long seatId, String seatNumber, String status) {}
}