package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.TicketScan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TicketScanRepository is used to perform CRUD operations with TicketScan entities.
 */
@Repository
public interface TicketScanRepository extends JpaRepository<TicketScan, Long>
{
    @Query("SELECT s FROM TicketScan s JOIN FETCH s.ticket WHERE s.scannedBy.id = :eventHostId")
    List<TicketScan> findAllByEventHostId(@Param("eventHostId") Long eventHostId);
}
