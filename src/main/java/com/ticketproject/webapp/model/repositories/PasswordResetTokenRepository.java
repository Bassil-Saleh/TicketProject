package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>
{
}
