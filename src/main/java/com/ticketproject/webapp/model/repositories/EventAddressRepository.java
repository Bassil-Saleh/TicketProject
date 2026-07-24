package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventAddress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * EventAddressRepository is used to perform CRUD operations with EventAddress entities.
 */
@Repository
public interface EventAddressRepository extends JpaRepository<EventAddress, Long>
{
}
