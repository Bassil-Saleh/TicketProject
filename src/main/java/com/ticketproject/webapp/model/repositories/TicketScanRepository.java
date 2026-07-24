package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.TicketScan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TicketScanRepository is used to perform CRUD operations with TicketScan entities.
 */
@Repository
public interface TicketScanRepository extends JpaRepository<TicketScan, Long>
{
}
