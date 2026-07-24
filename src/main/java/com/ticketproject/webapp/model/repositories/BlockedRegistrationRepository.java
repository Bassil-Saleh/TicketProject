package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.BlockedRegistration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedRegistrationRepository extends JpaRepository<BlockedRegistration, Long>
{
}
