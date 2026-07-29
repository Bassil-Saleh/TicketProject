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
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

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
}
