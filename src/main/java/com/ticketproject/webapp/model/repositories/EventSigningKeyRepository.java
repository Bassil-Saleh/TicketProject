package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventSigningKey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * EventSigningKeyRepository is used to perform CRUD operations with EventSigningKey entities.
 */
@Repository
public interface EventSigningKeyRepository extends JpaRepository<EventSigningKey, Long>
{
    /**
     * Find and retrieve an EventSigningKey based on the id of the Event
     * which the EventSigningKey is associated with.
     * @param eventId the ID of an Event
     * @return an Optional<EventSigningKey>
     */
    @Query("SELECT k FROM EventSigningKey k WHERE k.event.id = :eventId LIMIT 1")
    Optional<EventSigningKey> findByEventId(@Param("eventId") Long eventId);
}
