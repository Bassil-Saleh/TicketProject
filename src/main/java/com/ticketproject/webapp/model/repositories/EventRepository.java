package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * EventRepository is used to perform CRUD operations with Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>
{
    /**
     * Check if there exist any active events created by
     * an EventHost with the specified id number.
     * 
     * @param eventHostId an EventHost id number
     * @return true if any such events exist, false otherwise
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.eventHost.id = :eventHostId AND e.endDateTime >= CURRENT_TIMESTAMP")
    boolean activeEventsByEventHostIdExist(@Param("eventHostId") Long eventHostId);

    /**
     * Find and retrieve an Event entity with a specified public id.
     * @param publicId a public id
     * 
     * @return an Optional<Event>
     */
    @Query("SELECT e FROM Event e JOIN FETCH e.eventHost WHERE e.publicId = :publicId LIMIT 1")
    Optional<Event> findByPublicId(@Param("publicId") String publicId);

    /**
     * Find and retrieve all Event entities created by an EventHost
     * with a specified EventHost id number.
     * 
     * @param eventHostId an EventHost id number
     * @return a List of Event entities
     */
    @Query("SELECT e FROM Event e WHERE e.eventHost.id = :eventHostId")
    List<Event> findAllByEventHostId(@Param("eventHostId") Long eventHostId);
}
