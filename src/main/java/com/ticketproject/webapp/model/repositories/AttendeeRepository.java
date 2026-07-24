package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AttendeeRepository is used to perform CRUD operations with Attendee entities.
 */
@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long>
{
    @Query("SELECT a from Attendee a WHERE a.emailBlindIndex = :index")
    Optional<Attendee> findByEmailIndex(@Param("index") byte[] index);

    @Query("SELECT COUNT(a) > 0 FROM Attendee a WHERE a.emailBlindIndex = :index")
    boolean existsByEmailIndex(@Param("index") byte[] index);
}
