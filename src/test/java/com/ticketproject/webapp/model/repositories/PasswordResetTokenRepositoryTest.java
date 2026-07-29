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

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Token references valid eventHost after save")
        void tokenReferencesValidEventHost()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            PasswordResetToken loaded = passwordResetTokenRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getEventHost()).isNotNull();
            assertThat(loaded.getEventHost().getId()).isNotNull();

            assertThat(loaded.getEventHost().getId()).isEqualTo(savedHost.getId());
        }

        @Test
        @DisplayName("Token hash survives round-trip")
        void tokenHashSurvivesRoundTrip()
        {
            PasswordResetToken token = createToken();
            byte[] originalHash = token.getTokenHash();

            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            PasswordResetToken loaded = passwordResetTokenRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getTokenHash()).isNotNull();

            assertThat(loaded.getTokenHash()).isEqualTo(originalHash);
        }

        @Test
        @DisplayName("isUsed returns false for new token")
        void newTokenIsNotUsed()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);

            assertThat(saved).isNotNull();
            assertThat(saved.isUsed()).isFalse();
        }

        @Test
        @DisplayName("Setting used to true persists correctly")
        void setUsedPersists()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();

            saved.setUsed(true);
            saved = passwordResetTokenRepository.saveAndFlush(saved);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            PasswordResetToken loaded = passwordResetTokenRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.isUsed()).isTrue();
        }

        @Test
        @DisplayName("isValid returns true for unused, non-expired token")
        void isValidForUnusedNonExpired()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);

            assertThat(saved).isNotNull();
            assertThat(saved.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid returns false for used token")
        void isNotValidForUsedToken()
        {
            PasswordResetToken token = createToken();
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);
            assertThat(saved).isNotNull();

            saved.setUsed(true);
            saved = passwordResetTokenRepository.saveAndFlush(saved);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            PasswordResetToken loaded = passwordResetTokenRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded.isValid()).isFalse();
        }

        @Test
        @DisplayName("isValid returns false for expired token")
        void isNotValidForExpiredToken()
        {
            PasswordResetToken token = createToken();
            token.setExpires(LocalDateTime.now().minusHours(1));
            PasswordResetToken saved = passwordResetTokenRepository.saveAndFlush(token);

            assertThat(saved).isNotNull();
            assertThat(saved.isExpired()).isTrue();
            assertThat(saved.isValid()).isFalse();
        }

        @Test
        @DisplayName("Multiple tokens for same eventHost are allowed")
        void multipleTokensForSameHost()
        {
            PasswordResetToken token1 = createToken();
            PasswordResetToken saved1 = passwordResetTokenRepository.saveAndFlush(token1);
            assertThat(saved1).isNotNull();

            PasswordResetToken token2 = createToken();
            PasswordResetToken saved2 = passwordResetTokenRepository.saveAndFlush(token2);

            assertThat(saved2).isNotNull();
            assertThat(saved2.getId()).isNotNull();
            assertThat(passwordResetTokenRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Rollback on failure")
    class RollbackOnFailure
    {
        @Test
        @DisplayName("Failed token save does not persist the token")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void failedSaveDoesNotPersist()
        {
            // Get the baseline count BEFORE the transaction.
            long tokenCount = passwordResetTokenRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                PasswordResetToken token = createToken();
                token.setEventHost(null);
                passwordResetTokenRepository.saveAndFlush(token);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // The baseline count should not have changed.
            assertThat(passwordResetTokenRepository.count()).isEqualTo(tokenCount);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void constraintViolationRollsBackPriorSave()
        {
            // Get the baseline count BEFORE the transaction.
            long tokenCount = passwordResetTokenRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                PasswordResetToken token1 = createToken();
                PasswordResetToken saved1 = passwordResetTokenRepository.saveAndFlush(token1);
                assertThat(saved1).isNotNull();
                assertThat(saved1.getId()).isNotNull();
                assertThat(passwordResetTokenRepository.findById(saved1.getId())).isPresent();

                // Force a NOT NULL violation
                PasswordResetToken token2 = createToken();
                token2.setEventHost(null);
                passwordResetTokenRepository.saveAndFlush(token2);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // The baseline count should not have changed.
            assertThat(passwordResetTokenRepository.count()).isEqualTo(tokenCount);
        }
    }
}
