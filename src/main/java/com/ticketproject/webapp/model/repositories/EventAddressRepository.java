package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventAddress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventAddressRepository extends JpaRepository<EventAddress, Long>
{
}
