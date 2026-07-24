package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SessionRepository is used to perform CRUD operations with Session entities.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long>
{
}
