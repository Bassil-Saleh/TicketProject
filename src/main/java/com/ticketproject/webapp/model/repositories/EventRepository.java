package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * EventRepository is used to perform CRUD operations with Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>
{
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.eventHost.id = :eventHostId AND e.endDateTime >= CURRENT_TIMESTAMP")
    boolean activeEventsByEventHostIdExist(@Param("eventHostId") Long eventHostId);

    @Query("SELECT e FROM Event e WHERE e.publicId = :publicId LIMIT 1")
    Optional<Event> findByPublicId(@Param("publicId") String publicId);
}
