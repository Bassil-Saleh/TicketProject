package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.BlockedRegistration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BlockedRegistrationRepository is used to perform CRUD operations with BlockedRegistration entities.
 */
@Repository
public interface BlockedRegistrationRepository extends JpaRepository<BlockedRegistration, Long>
{
}
