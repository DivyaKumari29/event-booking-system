package com.bookingsystem.eventbookingsystem.repository;

import com.bookingsystem.eventbookingsystem.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategoryIgnoreCaseAndVenueContainingIgnoreCase(String category, String venue);
    List<Event> findByCategoryIgnoreCase(String category);
    List<Event> findByVenueContainingIgnoreCase(String venue);
}