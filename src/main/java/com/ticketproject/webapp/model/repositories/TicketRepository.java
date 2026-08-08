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
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.attendee.emailBlindIndex = :emailBlindIndex AND t.event.id = :eventId")
    boolean attendeeRegistrationExistsByEventId
    (
        @Param("emailBlindIndex")
        byte[] emailBlindIndex,

        @Param("eventId")
        Long eventId
    );

    @Query("SELECT t FROM Ticket t JOIN FETCH t.event WHERE t.tokenIdentifier = :tokenIdentifier LIMIT 1")
    Optional<Ticket> findByTokenIdentifier(@Param("tokenIdentifier") String tokenIdentifier);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId")
    long getRegistrationCountByEventId(@Param("eventId") Long eventId);
}
