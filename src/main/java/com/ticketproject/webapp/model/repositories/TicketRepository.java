package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Ticket;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TicketRepository is used to perform CRUD operations with Ticket entities.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>
{
    /**
     * Check whether an Attendee is registered for an Event
     * based on the ID of a given Event.
     * @param emailBlindIndex the blind index of an Attendee's email address
     * @param eventId the ID of an Event
     * @return true if the Attendee has a registration for the given Event, false otherwise
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.attendee.emailBlindIndex = :emailBlindIndex AND t.event.id = :eventId")
    boolean attendeeRegistrationExistsByEventId
    (
        @Param("emailBlindIndex")
        byte[] emailBlindIndex,

        @Param("eventId")
        Long eventId
    );

    /**
     * Find and retrieve a Ticket based on a given token identifier value.
     * @param tokenIdentifier a token identifier
     * @return an Optional<Ticket>
     */
    @Query("SELECT t FROM Ticket t JOIN FETCH t.event WHERE t.tokenIdentifier = :tokenIdentifier LIMIT 1")
    Optional<Ticket> findByTokenIdentifier(@Param("tokenIdentifier") String tokenIdentifier);

    /**
     * Get the number of registrations for a given Event identified by an Event's ID.
     * @param eventId the ID of an Event
     * @return the number of registrations for the given Event
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    long getRegistrationCountByEventId(@Param("eventId") Long eventId);
}
