package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.EventHost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventHostRepository extends JpaRepository<EventHost, Long>
{
    @Query("SELECT eh FROM EventHost eh WHERE eh.emailBlindIndex = :index")
    Optional<EventHost> findByEmailIndex(@Param("index") byte[] index);

    @Query("SELECT COUNT(eh) > 0 FROM EventHost eh WHERE eh.emailBlindIndex = :index")
    boolean existsByEmailIndex(byte[] index);

    @Query("SELECT eh FROM EventHost eh WHERE eh.verificationKeyHash = :hash")
    Optional<EventHost> findByVerificationKeyHash(@Param("hash") byte[] hash);
}