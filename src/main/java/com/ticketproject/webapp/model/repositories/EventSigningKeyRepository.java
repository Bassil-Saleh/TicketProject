package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventSigningKey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventSigningKeyRepository extends JpaRepository<EventSigningKey, Long>
{
}
