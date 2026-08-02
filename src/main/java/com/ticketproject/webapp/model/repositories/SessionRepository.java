package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SessionRepository is used to perform CRUD operations with Session entities.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long>
{
    /**
     * Find a Session entity by its token hash.
     * @param hash the token hash to search for
     * @return an Optional containing the Session if found, empty otherwise
     */
    @Query("SELECT s FROM Session s WHERE s.tokenHash = :hash")
    Optional<Session> findByTokenHash(@Param("hash") byte[] hash);
}
