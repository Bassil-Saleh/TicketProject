package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PasswordResetTokenRepository is used to perform CRUD operations with PasswordResetToken entities.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>
{
}
