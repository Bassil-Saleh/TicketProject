package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.TicketScan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketScanRepository extends JpaRepository<TicketScan, Long>
{
}
