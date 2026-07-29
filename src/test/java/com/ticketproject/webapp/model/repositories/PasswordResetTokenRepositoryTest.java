package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.PasswordResetToken;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PasswordResetTokenRepositoryTest contains integration tests for the
 * PasswordResetToken entity and PasswordResetTokenRepository, covering
 * rollback on failure, data constraints, data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PasswordResetTokenRepositoryTest
{
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private EventHost savedHost;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists a shared EventHost entity used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

        EventHost host = new EventHost.Builder()
            .firstName("Reset")
            .lastName("User")
            .dateOfBirth(LocalDate.of(1993, 9, 9))
            .email("reset-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        savedHost = eventHostRepository.saveAndFlush(host);
    }

    /**
     * Helper method to create a valid PasswordResetToken entity.
     * @return a new PasswordResetToken entity (not yet persisted)
     */
    private PasswordResetToken createToken()
    {
        PasswordResetToken token = new PasswordResetToken.Builder()
            .eventHost(savedHost)
            .tokenHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
            .build();
        token.generateToken();
        return token;
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving token with null eventHost violates NOT NULL constraint")
        void nullEventHostThrowsException()
        {
            PasswordResetToken token = createToken();
            token.setEventHost(null);

            assertThatThrownBy(() -> passwordResetTokenRepository.saveAndFlush(token))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving token with null tokenHash violates NOT NULL constraint")
        void nullTokenHashThrowsException()
        {
            PasswordResetToken token = new PasswordResetToken.Builder()
                .eventHost(savedHost)
                .tokenHash(null)
                .build();

            assertThatThrownBy(() -> passwordResetTokenRepository.saveAndFlush(token))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating token with null eventHost violates NOT NULL constraint")
        void updateNullEventHostThrowsException()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();
            assertThat(saved.getEventHost()).isNotNull();
            saved.setEventHost(null);

            assertThatThrownBy(() -> passwordResetTokenRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating token with null tokenHash violates NOT NULL constraint")
        void updateNullTokenHashThrowsException()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();
            assertThat(saved.getTokenHash()).isNotNull();
            saved.setTokenHash(null);

            assertThatThrownBy(() -> passwordResetTokenRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
