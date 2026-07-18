package com.bookingsystem.eventbookingsystem.service;

import com.bookingsystem.eventbookingsystem.entity.*;
import com.bookingsystem.eventbookingsystem.repository.EventRepository;
import com.bookingsystem.eventbookingsystem.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    public List<Seat> generateSeats(Long eventId, int count, double price) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(Seat.builder()
                    .seatNumber("S" + i)
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .event(event)
                    .build());
        }
        return seatRepository.saveAll(seats);
    }
}