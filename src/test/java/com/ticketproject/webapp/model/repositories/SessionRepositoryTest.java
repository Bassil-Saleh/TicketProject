package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.model.enums.ClientType;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SessionRepositoryTest contains integration tests for the Session entity
 * and SessionRepository, covering rollback on failure, data constraints,
 * data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SessionRepositoryTest
{
    @Autowired
    private SessionRepository sessionRepository;

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
            .firstName("Session")
            .lastName("User")
            .dateOfBirth(LocalDate.of(1991, 11, 11))
            .email("session-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        savedHost = eventHostRepository.saveAndFlush(host);
    }

    /**
     * Helper method to create a valid Session entity.
     * @return a new Session entity (not yet persisted)
     */
    private Session createSession()
    {
        Session session = new Session.Builder()
            .eventHost(savedHost)
            .clientType(ClientType.WEB)
            .ipAddress("192.168.1.100")
            .userAgent("Mozilla/5.0")
            .build();
        session.generateToken();
        return session;
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving session with null eventHost violates NOT NULL constraint")
        void nullEventHostThrowsException()
        {
            Session session = createSession();
            session.setEventHost(null);

            assertThatThrownBy(() -> sessionRepository.saveAndFlush(session))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving session with null tokenHash violates NOT NULL constraint")
        void nullTokenHashThrowsException()
        {
            Session session = new Session.Builder()
                .eventHost(savedHost)
                .clientType(ClientType.WEB)
                .ipAddress("10.0.0.1")
                .userAgent("TestAgent")
                .build();
            // Do not call generateToken(), so tokenHash remains null

            assertThatThrownBy(() -> sessionRepository.saveAndFlush(session))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving session with null clientType violates NOT NULL constraint")
        void nullClientTypeThrowsException()
        {
            Session session = createSession();
            session.setClientType(null);

            assertThatThrownBy(() -> sessionRepository.saveAndFlush(session))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating session with null eventHost violates NOT NULL constraint")
        void updateNullEventHostThrowsException()
        {
            Session session = createSession();
            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();
            assertThat(saved.getEventHost()).isNotNull();
            saved.setEventHost(null);

            assertThatThrownBy(() -> sessionRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating session with null clientType violates NOT NULL constraint")
        void updateNullClientTypeThrowsException()
        {
            Session session = createSession();
            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();
            assertThat(saved.getClientType()).isNotNull();
            saved.setClientType(null);

            assertThatThrownBy(() -> sessionRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Session references valid eventHost after save")
        void sessionReferencesValidEventHost()
        {
            Session session = createSession();
            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Session loaded = sessionRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getEventHost()).isNotNull();
            assertThat(loaded.getEventHost().getId()).isNotNull();
            assertThat(loaded.getEventHost().getId()).isEqualTo(savedHost.getId());
        }

        @Test
        @DisplayName("isActive returns true for non-revoked, non-expired session")
        void isActiveForValidSession()
        {
            Session session = createSession();
            Session saved = sessionRepository.saveAndFlush(session);

            assertThat(saved).isNotNull();
            assertThat(saved.isActive()).isTrue();
            assertThat(saved.isRevoked()).isFalse();
            assertThat(saved.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isRevoked returns true after setting revoked timestamp")
        void isRevokedAfterSettingRevoked()
        {
            Session session = createSession();
            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();

            saved.setRevoked(LocalDateTime.now());
            saved = sessionRepository.saveAndFlush(saved);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Session loaded = sessionRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.isRevoked()).isTrue();
            assertThat(loaded.isActive()).isFalse();
        }

        @Test
        @DisplayName("isExpired returns true for past expiry date")
        void isExpiredForPastExpiry()
        {
            Session session = createSession();
            // Bypass encapsulation to force a past expiry date
            ReflectionTestUtils.setField(session, "expires", LocalDateTime.now().minusHours(1));
            Session saved = sessionRepository.saveAndFlush(session);

            assertThat(saved).isNotNull();
            assertThat(saved.isExpired()).isTrue();
            assertThat(saved.isActive()).isFalse();
        }

        @Test
        @DisplayName("Client type persists correctly after round-trip")
        void clientTypePersistsCorrectly()
        {
            Session session = new Session.Builder()
                .eventHost(savedHost)
                .clientType(ClientType.ANDROID)
                .ipAddress("172.16.0.1")
                .userAgent("AndroidApp/2.0")
                .build();
            session.generateToken();

            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Session loaded = sessionRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getClientType()).isNotNull();
            assertThat(loaded.getClientType()).isEqualTo(ClientType.ANDROID);
        }

        @Test
        @DisplayName("IP address and user agent survive round-trip")
        void ipAddressAndUserAgentSurviveRoundTrip()
        {
            Session session = createSession();
            String ip = session.getIpAddress();
            String ua = session.getUserAgent();

            Session saved = sessionRepository.saveAndFlush(session);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Session loaded = sessionRepository.findById(saved.getId()).orElseThrow();

            assertThat(loaded).isNotNull();
            assertThat(loaded.getIpAddress()).isEqualTo(ip);
            assertThat(loaded.getUserAgent()).isEqualTo(ua);
        }

        @Test
        @DisplayName("Multiple sessions for same eventHost are allowed")
        void multipleSessionsForSameHost()
        {
            Session session1 = createSession();
            Session saved1 = sessionRepository.saveAndFlush(session1);
            assertThat(saved1).isNotNull();

            Session session2 = createSession();
            Session saved2 = sessionRepository.saveAndFlush(session2);
            assertThat(saved2).isNotNull();

            assertThat(sessionRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Rollback on failure")
    class RollbackOnFailure
    {
        @Test
        @DisplayName("Failed session save does not persist the session")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void failedSaveDoesNotPersist()
        {
            // Get the baseline counts BEFORE the transaction.
            long sessionCount = sessionRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Session session = createSession();
                session.setEventHost(null);
                sessionRepository.saveAndFlush(session);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // The baseline count should not have changed.
            assertThat(sessionRepository.count()).isEqualTo(sessionCount);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void constraintViolationRollsBackPriorSave()
        {
            // Get the baseline counts BEFORE the transaction.
            long sessionCount = sessionRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Session session1 = createSession();
                Session saved1 = sessionRepository.saveAndFlush(session1);
                assertThat(saved1).isNotNull();
                assertThat(saved1.getId()).isNotNull();
                assertThat(sessionRepository.findById(saved1.getId())).isPresent();

                // Force a NOT NULL violation
                Session session2 = createSession();
                session2.setClientType(null);
                sessionRepository.saveAndFlush(session2);

                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // The baseline count should not have changed.
            assertThat(sessionRepository.count()).isEqualTo(sessionCount);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful session save commits atomically")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void successfulSaveCommitsAtomically()
        {
            Long sessionId = txTemplate.execute(status ->
            {
                Session session = createSession();
                Session saved = sessionRepository.saveAndFlush(session);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            assertThat(sessionRepository.findById(sessionId)).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back session is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void rolledBackSessionNotVisible()
        {
            Long sessionId = txTemplate.execute(status ->
            {
                Session session = createSession();
                Session saved = sessionRepository.saveAndFlush(session);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                status.setRollbackOnly();
                return saved.getId();
            });

            assertThat(sessionRepository.findById(sessionId)).isEmpty();
        }

        @Test
        @DisplayName("Committed session is visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void committedSessionVisible()
        {
            Long sessionId = txTemplate.execute(status ->
            {
                Session session = createSession();
                Session saved = sessionRepository.saveAndFlush(session);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                return saved.getId();
            });

            assertThat(sessionRepository.findById(sessionId)).isPresent();
        }
    }
}
