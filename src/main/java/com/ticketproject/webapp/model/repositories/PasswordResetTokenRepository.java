package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PasswordResetTokenRepository is used to perform CRUD operations with PasswordResetToken entities.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>
{
    @Query("SELECT t FROM PasswordResetToken t JOIN FETCH t.eventHost WHERE t.tokenHash = :hash")
    Optional<PasswordResetToken> findByTokenHash(@Param("hash") byte[] hash);
}
