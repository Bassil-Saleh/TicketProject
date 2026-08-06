package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventHost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * EventHostRepository is used to perform CRUD operations with EventHost entities.
 */
@Repository
public interface EventHostRepository extends JpaRepository<EventHost, Long>
{
    /**
     * Find and retrieve an EventHost entity based on an email blind index.
     * 
     * @param index an email blind index
     * @return an Optional<EventHost>
     */
    @Query("SELECT eh FROM EventHost eh WHERE eh.emailBlindIndex = :index")
    Optional<EventHost> findByEmailIndex(@Param("index") byte[] index);

    /**
     * Check whether an EventHost entity with a specified email blind index exists.
     * 
     * @param index an email blind index
     * @return true if an EventHost entity with the specified
     * email blind index exists, false otherwise
     */
    @Query("SELECT COUNT(eh) > 0 FROM EventHost eh WHERE eh.emailBlindIndex = :index")
    boolean existsByEmailIndex(@Param("index") byte[] index);

    /**
     * Find and retrieve an EventHost entity based on a verification key hash.
     * 
     * @param hash a verification key hash
     * @return an Optional<EventHost>
     */
    @Query("SELECT eh FROM EventHost eh WHERE eh.verificationKeyHash = :hash")
    Optional<EventHost> findByVerificationKeyHash(@Param("hash") byte[] hash);
}