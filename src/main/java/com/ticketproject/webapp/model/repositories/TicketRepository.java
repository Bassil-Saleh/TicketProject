package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>
{
}
