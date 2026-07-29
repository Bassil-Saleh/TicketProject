package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventSigningKeyRepositoryTest contains integration tests for the
 * EventSigningKey entity and EventSigningKeyRepository, covering
 * rollback on failure, data constraints, data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventSigningKeyRepositoryTest
{
    @Autowired
    private EventSigningKeyRepository eventSigningKeyRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    private Event event1;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists a shared Event entity used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

        // Does not add a signing key or persist the event to the database
        event1 = createEvent();
    }

    /**
     * Generates an Event for testing (no signing key assigned to it, though).
     * @return an Event
     */
    private Event createEvent()
    {
        EventHost host = new EventHost.Builder()
            .firstName("Key")
            .lastName("Host")
            .dateOfBirth(LocalDate.of(1995, 5, 5))
            .email("keyhost-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        EventHost savedHost = eventHostRepository.saveAndFlush(host);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("500 Key Blvd")
            .city("Keytown")
            .state("TX")
            .postalCode("75001")
            .country("USA")
            .build();

        Event event = new Event.Builder()
            .publicId(UUID.randomUUID().toString())
            .eventHost(savedHost)
            .name("Key Event")
            .description("An event for signing key tests")
            .startDateTime(LocalDateTime.now().plusDays(10))
            .endDateTime(LocalDateTime.now().plusDays(10).plusHours(2))
            .eventType(EventType.PRIVATE)
            .maxAttendees(50)
            .build();

        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.CLOSED);
        event.setEventStatus(EventStatus.DRAFT);

        return event;
    }

    /**
     * Generates a fresh key pair for testing.
     * @return a KeyPair
     * @throws RuntimeException if key pair generation fails
     */
    private KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator
                .getInstance(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_ALGORITHM);
            generator.initialize(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_SIZE_TEST);
            return generator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot generate keypair - algorithm not supported", e);
        }
        catch (InvalidParameterException e)
        {
            throw new RuntimeException("Cannot generate keypair - key size not supported", e);
        }
    }
    /**
     * Helper method to create a valid EventSigningKey for a given event.
     * @param event the event to associate the signing key with
     * @return a new EventSigningKey entity (not yet persisted)
     */
    private EventSigningKey createSigningKey(Event event)
    {
        KeyPair keyPair = generateKeyPair();
        return new EventSigningKey.Builder()
            .event(event)
            .privateKey(keyPair.getPrivate())
            .publicKey(keyPair.getPublic())
            .build();
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving signing key with null event violates NOT NULL constraint")
        void nullEventThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            key1.setEvent(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving signing key with null privateKey violates NOT NULL constraint")
        void nullPrivateKeyThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            key1.setPrivateKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving signing key with null publicKey violates NOT NULL constraint")
        void nullPublicKeyThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            key1.setPublicKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null event violates NOT NULL constraint")
        void updateNullEventThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);

            Event saved1 = eventRepository.saveAndFlush(event1);
            assertThat(saved1).isNotNull();
            assertThat(saved1.getSigningKey()).isNotNull();
            key1 = saved1.getSigningKey();
            key1.setEvent(null);
            saved1.setSigningKey(key1);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null privateKey violates NOT NULL constraint")
        void updateNullPrivateKeyThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);

            Event saved1 = eventRepository.saveAndFlush(event1);
            assertThat(saved1).isNotNull();
            assertThat(saved1.getSigningKey()).isNotNull();
            key1 = saved1.getSigningKey();
            key1.setPrivateKey(null);
            saved1.setSigningKey(key1);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null publicKey violates NOT NULL constraint")
        void updateNullPublicKeyThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);

            Event saved1 = eventRepository.saveAndFlush(event1);
            assertThat(saved1).isNotNull();
            assertThat(saved1.getSigningKey()).isNotNull();
            key1 = saved1.getSigningKey();
            key1.setPublicKey(null);
            saved1.setSigningKey(key1);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved1))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate event_id violates unique constraint (one-to-one)")
        void duplicateEventIdThrowsException()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            eventRepository.saveAndFlush(event1);

            Event event2 = createEvent();
            // Try to create a second signing key for the same event (savedEvent).
            // This should fail because event_id has a UNIQUE constraint.
            EventSigningKey key2 = createSigningKey(event1);
            event2.setSigningKey(key2);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Signing key references valid event after save")
        void signingKeyReferencesValidEvent()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            Event saved1 = eventRepository.saveAndFlush(event1);

            assertThat(saved1).isNotNull();
            assertThat(saved1.getId()).isNotNull();
            assertThat(saved1.getSigningKey()).isNotNull();
            assertThat(saved1.getSigningKey().getId()).isNotNull();

            EventSigningKey loaded = eventSigningKeyRepository.findById(saved1.getSigningKey().getId()).orElseThrow();
            assertThat(loaded.getEvent().getId()).isEqualTo(event1.getId());
        }

        @Test
        @DisplayName("Private and public keys survive round-trip")
        void keysSurviveRoundTrip()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);

            Event saved1 = eventRepository.saveAndFlush(event1);
            assertThat(saved1).isNotNull();
            assertThat(saved1.getSigningKey()).isNotNull();
            assertThat(saved1.getSigningKey().getId()).isNotNull();

            EventSigningKey loadedKey1 = eventSigningKeyRepository
                .findById(saved1.getSigningKey().getId()).orElseThrow();

            assertThat(loadedKey1.getPrivateKey()).isEqualTo(key1.getPrivateKey());
            assertThat(loadedKey1.getPublicKey()).isEqualTo(key1.getPublicKey());
        }

        @Test
        @DisplayName("Created timestamp is set upon creation")
        void createdTimestampIsSetAndNotUpdatable()
        {
            EventSigningKey key1 = createSigningKey(event1);
            event1.setSigningKey(key1);
            Event saved1 = eventRepository.saveAndFlush(event1);

            assertThat(saved1).isNotNull();
            assertThat(saved1.getCreated()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Rollback on failure")
    class RollbackOnFailure
    {
        @Test
        @DisplayName("Diagnostic: Verify JDBC auto-commit is disabled")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void verifyAutoCommitIsDisabled() throws SQLException
        {
            try (Connection conn = dataSource.getConnection())
            {
                boolean isAutoCommit = conn.getAutoCommit();

                // If this assertion fails, auto-commit is ON, which breaks rollback testing.
                assertThat(isAutoCommit)
                    .as("JDBC auto-commit must be false for transaction rollbacks to work")
                    .isFalse();
            }
        }

        @Test
        @DisplayName("Failed signing key save does not persist the key")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void failedSaveDoesNotPersist()
        {
            // Get the baseline counts BEFORE running the transaction.
            long signingKeyCountBefore = eventSigningKeyRepository.count();
            long eventCountBefore = eventRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Event event = createEvent();
                EventSigningKey key = createSigningKey(event);
                key.setEvent(null);
                eventRepository.saveAndFlush(event);
                return null;
            })).isInstanceOf(InvalidDataAccessApiUsageException.class);

            // The counts should not have changed.
            assertThat(eventSigningKeyRepository.count()).isEqualTo(signingKeyCountBefore);
            assertThat(eventRepository.count()).isEqualTo(eventCountBefore);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void constraintViolationRollsBackPriorSave()
        {
            // Get the baseline counts BEFORE running the transaction.
            long signingKeyCountBefore = eventSigningKeyRepository.count();
            long eventCountBefore = eventRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Event event = createEvent();
                EventSigningKey key = createSigningKey(event);
                event.setSigningKey(key);
                Event saved = eventRepository.saveAndFlush(event);

                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getSigningKey()).isNotNull();
                assertThat(saved.getSigningKey().getId()).isNotNull();

                Event anotherEvent = createEvent();
                EventSigningKey anotherKey = createSigningKey(anotherEvent);
                // Force a NOT NULL violation.
                anotherKey.setPrivateKey(null);
                anotherEvent.setSigningKey(anotherKey);

                eventRepository.saveAndFlush(anotherEvent);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // The counts should not have changed.
            assertThat(eventSigningKeyRepository.count()).isEqualTo(signingKeyCountBefore);
            assertThat(eventRepository.count()).isEqualTo(eventCountBefore);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful signing key save commits atomically")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void successfulSaveCommitsAtomically()
        {
            Long signingKeyId = txTemplate.execute(status ->
            {
                Event event = createEvent();
                EventSigningKey key = createSigningKey(event);
                event.setSigningKey(key);
                Event saved = eventRepository.saveAndFlush(event);

                assertThat(saved).isNotNull();
                assertThat(saved.getSigningKey()).isNotNull();
                assertThat(saved.getSigningKey().getId()).isNotNull();

                return saved.getSigningKey().getId();
            });

            assertThat(eventSigningKeyRepository.findById(signingKeyId)).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back signing key is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rolledBackKeyNotVisible()
        {
            Long signingKeyId = txTemplate.execute(status ->
            {
                Event event = createEvent();
                EventSigningKey key = createSigningKey(event);
                event.setSigningKey(key);
                Event saved = eventRepository.saveAndFlush(event);

                assertThat(saved).isNotNull();
                assertThat(saved.getSigningKey()).isNotNull();
                assertThat(saved.getSigningKey().getId()).isNotNull();

                status.setRollbackOnly();
                return saved.getSigningKey().getId();
            });

            assertThat(eventSigningKeyRepository.findById(signingKeyId)).isEmpty();
        }

        @Test
        @DisplayName("Committed signing key is visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void committedKeyVisible()
        {
            Long signingKeyId = txTemplate.execute(status ->
            {
                Event event = createEvent();
                EventSigningKey key = createSigningKey(event);
                event.setSigningKey(key);
                Event saved = eventRepository.saveAndFlush(event);

                assertThat(saved).isNotNull();
                assertThat(saved.getSigningKey()).isNotNull();
                assertThat(saved.getSigningKey().getId()).isNotNull();

                return saved.getSigningKey().getId();
            });

            assertThat(eventSigningKeyRepository.findById(signingKeyId)).isPresent();
        }
    }
}