package com.bookingsystem.eventbookingsystem.controller;

import com.bookingsystem.eventbookingsystem.entity.Event;
import com.bookingsystem.eventbookingsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.bookingsystem.eventbookingsystem.service.SeatService;
import com.bookingsystem.eventbookingsystem.entity.Seat;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.bookingsystem.eventbookingsystem.repository.SeatRepository;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final SeatService seatService;

    @GetMapping
    @Cacheable("events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping
    @CacheEvict(value = "events", allEntries = true)
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    @PostMapping("/{eventId}/seats/generate")
    public List<Seat> generateSeats(@PathVariable Long eventId,
                                    @RequestParam int count,
                                    @RequestParam double price) {
        return seatService.generateSeats(eventId, count, price);
    }

    @GetMapping("/search")
    public List<Event> searchEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String venue) {

        if (category != null && venue != null) {
            return eventRepository.findByCategoryIgnoreCaseAndVenueContainingIgnoreCase(category, venue);
        } else if (category != null) {
            return eventRepository.findByCategoryIgnoreCase(category);
        } else if (venue != null) {
            return eventRepository.findByVenueContainingIgnoreCase(venue);
        }
        return eventRepository.findAll();
    }

    private final SeatRepository seatRepository;
    @GetMapping("/{eventId}/seats")
    public List<Seat> getSeatsByEvent(@PathVariable Long eventId) {
        return seatRepository.findByEventId(eventId);
    }
}