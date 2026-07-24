package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TicketRepository is used to perform CRUD operations with Ticket entities.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>
{
}
