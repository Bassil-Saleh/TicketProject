package com.ticketproject.webapp.model.repositories;

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.Connection;

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
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventRepositoryTest contains integration tests for the Event entity
 * and EventRepository, covering rollback on failure, data constraints,
 * data integrity, cascade operations, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventRepositoryTest
{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private EventAddressRepository eventAddressRepository;

    @Autowired
    private EventSigningKeyRepository eventSigningKeyRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    private EventHost savedHost;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists a shared EventHost used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

        EventHost host = new EventHost.Builder()
            .firstName("Test")
            .lastName("Host")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .email("eventhost-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("securePass123")
            .build();
        host.generateVerificationToken();
        savedHost = eventHostRepository.saveAndFlush(host);
    }

    /**
     * Helper method to create a valid Event with all required fields.
     * @param publicId a unique public identifier
     * @return a new Event entity (not yet persisted)
     */
    private Event createEvent(String publicId)
    {
        EventAddress address = new EventAddress.Builder()
            .addressLine1("123 Main St")
            .city("Springfield")
            .state("IL")
            .postalCode("62701")
            .country("USA")
            .build();
        
        Event event = new Event.Builder()
            .publicId(publicId)
            .eventHost(savedHost)
            .name("Test Event")
            .description("A test event description")
            .startDateTime(LocalDateTime.now().plusDays(7))
            .endDateTime(LocalDateTime.now().plusDays(7).plusHours(3))
            .eventType(EventType.PUBLIC)
            .maxAttendees(100)
            .build();
        
        EventSigningKey signingKey = createSigningKey(event);
        
        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.DRAFT);
        event.setSigningKey(signingKey);

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
        @DisplayName("Saving event with null publicId violates NOT NULL constraint")
        void nullPublicIdThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setPublicId(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null name violates NOT NULL constraint")
        void nullNameThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setName(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null description violates NOT NULL constraint")
        void nullDescriptionThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setDescription(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null eventHost violates NOT NULL constraint")
        void nullEventHostThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setEventHost(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null eventAddress violates NOT NULL constraint")
        void nullEventAddressThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setEventAddress(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        @DisplayName("Saving event with null eventSigningKey violates NOT NULL constraint")
        void nullEventSigningKeyThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setSigningKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        @DisplayName("Saving event with null startDateTime violates NOT NULL constraint")
        void nullStartDateTimeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setStartDateTime(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null endDateTime violates NOT NULL constraint")
        void nullEndDateTimeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setEndDateTime(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null eventType violates NOT NULL constraint")
        void nullEventTypeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setEventType(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null registrationStatus violates NOT NULL constraint")
        void nullRegistrationStatusThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setRegistrationStatus(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving event with null eventStatus violates NOT NULL constraint")
        void nullEventStatusThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            event.setEventStatus(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate publicId violates unique constraint")
        void duplicatePublicIdThrowsException()
        {
            String publicId = UUID.randomUUID().toString();
            Event event1 = createEvent(publicId);
            eventRepository.saveAndFlush(event1);

            Event event2 = createEvent(publicId);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null publicId violates NOT NULL constraint")
        void updateNullPublicIdThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setPublicId(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null name violates NOT NULL constraint")
        void updateNullNameThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setName(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null description violates NOT NULL constraint")
        void updateNullDescriptionThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setDescription(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null eventHost violates NOT NULL constraint")
        void updateNullEventHostThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setEventHost(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null eventAddress violates NOT NULL constraint")
        void updateNullEventAddressThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setEventAddress(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        @DisplayName("Updating event with null eventSigningKey violates NOT NULL constraint")
        void updateNullEventSigningKeyThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setSigningKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        @DisplayName("Updating event with null startDateTime violates NOT NULL constraint")
        void updatingNullStartDateTimeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setStartDateTime(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null endDateTime violates NOT NULL constraint")
        void updatingNullEndDateTimeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setEndDateTime(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null eventType violates NOT NULL constraint")
        void updateNullEventTypeThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setEventType(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null registrationStatus violates NOT NULL constraint")
        void updateNullRegistrationStatusThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setRegistrationStatus(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating event with null eventStatus violates NOT NULL constraint")
        void updateNullEventStatusThrowsException()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            saved.setEventStatus(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate EventSigningKey violates unique constraint")
        void duplicateEventSigningKeyThrowsException()
        {
            // If the user creates an Event with an EventSigningKey,
            // creates tickets with the EventSigningKey, then gives the
            // Event a new EventSigningKey, then the tickets made using
            // the old EventSigningKey will no longer be usable.
            // That's why I put a uniqueness constraint for EventSigningKey.

            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);

            EventSigningKey newKey = createSigningKey(saved);
            saved.setSigningKey(newKey);

            // Since saved is not a final variable, I can't use it in a lambda,
            // so I have to check that it throws an exception a different way.
            boolean violated = false;
            try
            {
                eventRepository.saveAndFlush(saved);
            }
            catch (DataIntegrityViolationException e)
            {
                violated = true;
            }
            assertThat(violated).isTrue();
        }
    }

    @Nested
    @DisplayName("Data integrity: cascade and orphan removal")
    class DataIntegrity
    {
        @Test
        @DisplayName("Cascade ALL: saving event also persists its EventAddress")
        void cascadeAllPersistsEventAddress()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            EventAddress address = event.getEventAddress();

            Event saved = eventRepository.saveAndFlush(event);
            address = saved.getEventAddress();

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(address).isNotNull();
            assertThat(address.getId()).isNotNull();

            Optional<EventAddress> loaded = eventAddressRepository.findById(address.getId());
            assertThat(loaded).isPresent();
        }

        @Test
        @DisplayName("Orphan removal: replacing EventAddress deletes the old one")
        void orphanRemovalDeletesOldAddress()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            Long oldAddressId = saved.getEventAddress().getId();

            EventAddress newAddress = new EventAddress.Builder()
                .addressLine1("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .country("USA")
                .build();

            saved.setEventAddress(newAddress);
            saved = eventRepository.saveAndFlush(saved);
            newAddress = saved.getEventAddress();

            Optional<EventAddress> oldLoaded = eventAddressRepository.findById(oldAddressId);
            assertThat(oldLoaded).isEmpty();

            assertThat(newAddress.getId()).isNotNull();
        }

        @Test
        @DisplayName("Cascade ALL: saving event also persists its EventSigningKey")
        void cascadeAllPersistsSigningKey()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);

            EventSigningKey signingKey = saved.getSigningKey();
            assertThat(signingKey).isNotNull();
            assertThat(signingKey.getId()).isNotNull();

            Optional<EventSigningKey> loaded = eventSigningKeyRepository.findById(signingKey.getId());
            assertThat(loaded).isPresent();
        }

        @Test
        @DisplayName("Deleting event cascades to EventAddress and EventSigningKey")
        void deleteEventCascadesToChildren()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);

            EventSigningKey signingKey = saved.getSigningKey();
            EventAddress address = saved.getEventAddress();
            assertThat(signingKey).isNotNull();
            assertThat(signingKey.getId()).isNotNull();
            assertThat(address).isNotNull();
            assertThat(address.getId()).isNotNull();

            eventRepository.delete(saved);
            eventRepository.flush();

            assertThat(eventAddressRepository.findById(address.getId())).isEmpty();
            assertThat(eventSigningKeyRepository.findById(signingKey.getId())).isEmpty();
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
                System.out.println(">>> JDBC Auto-Commit is: " + isAutoCommit);
                
                // If this assertion fails, auto-commit is ON, which breaks rollback testing.
                assertThat(isAutoCommit)
                    .as("JDBC auto-commit must be false for transaction rollbacks to work")
                    .isFalse();
            }
        }

        @Test
        @DisplayName("Failed save does not persist partial data")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void failedSaveDoesNotPersistPartialData()
        {
            // Capture baseline counts before attempting the save
            long eventCountBefore = eventRepository.count();
            long addressCountBefore = eventAddressRepository.count();
            long signingKeyCountBefore = eventSigningKeyRepository.count();

            Event event = createEvent(UUID.randomUUID().toString());
            // Make the event invalid after setting up the address
            event.setPublicId(null);

            assertThatThrownBy
            (
                () -> txTemplate.execute
                (
                    status ->
                    {
                        eventRepository.saveAndFlush(event);
                        return null;
                    }
                )
            ).isInstanceOf(DataIntegrityViolationException.class);

            // Assert that the counts have NOT increased.
            assertThat(eventRepository.count())
                .as("Event count should not increase after rollback")
                .isEqualTo(eventCountBefore);
            assertThat(eventAddressRepository.count())
                .as("Address count should not increase after rollback")
                .isEqualTo(addressCountBefore);
            assertThat(eventSigningKeyRepository.count())
                .as("EventSigningKey count should not increase after rollback")
                .isEqualTo(signingKeyCountBefore);
        }

        @Test
        @DisplayName("Constraint violation on second save rolls back the first save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable test-managed transaction for this test
        void constraintViolationRollsBackPriorSave()
        {
            // Capture baseline counts before attempting the save
            long eventCountBefore = eventRepository.count();
            long addressCountBefore = eventAddressRepository.count();
            long signingKeyCountBefore = eventSigningKeyRepository.count();

            assertThatThrownBy
            (
                () -> txTemplate.execute
                (
                    status ->
                    {
                        Event event1 = createEvent(UUID.randomUUID().toString());
                        event1 = eventRepository.saveAndFlush(event1);
                        // Verify event1 was saved
                        assertThat(event1).isNotNull();
                        assertThat(event1.getId()).isNotNull();
                        assertThat(event1.getPublicId()).isNotNull();
                        assertThat(eventRepository.findById(event1.getId())).isPresent();

                        // Now attempt to save event2 with a duplicate publicId
                        Event event2 = createEvent(event1.getPublicId());
                        // This should throw an exception
                        eventRepository.saveAndFlush(event2);

                        return null;
                    }
                )
            ).isInstanceOf(DataIntegrityViolationException.class);

            // Assert that the counts have NOT increased.
            assertThat(eventRepository.count())
                .as("Event count should not increase after rollback")
                .isEqualTo(eventCountBefore);
            assertThat(eventAddressRepository.count())
                .as("Address count should not increase after rollback")
                .isEqualTo(addressCountBefore);
            assertThat(eventSigningKeyRepository.count())
                .as("EventSigningKey count should not increase after rollback")
                .isEqualTo(signingKeyCountBefore);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    class CommitAtomicity
    {
        @Test
        @DisplayName("Saving event with address and signing key atomically persists all")
        void saveEventGraphAtomically()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            EventSigningKey signingKey = saved.getSigningKey();
            EventAddress address = saved.getEventAddress();
            assertThat(signingKey).isNotNull();
            assertThat(signingKey.getId()).isNotNull();
            assertThat(address).isNotNull();
            assertThat(address.getId()).isNotNull();

            // Verify all parts of the graph are persisted
            assertThat(eventRepository.findById(saved.getId())).isPresent();
            assertThat(eventAddressRepository.findById(address.getId())).isPresent();
            assertThat(eventSigningKeyRepository.findById(signingKey.getId())).isPresent();
        }

        @Test
        @DisplayName("Successful transaction commits all changes")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void successfulTransactionCommitsAll()
        {
            Event event = createEvent(UUID.randomUUID().toString());

            Event saved = txTemplate.execute(status ->
            {
                Event aSave = eventRepository.saveAndFlush(event);
                assertThat(aSave).isNotNull();
                assertThat(aSave.getId()).isNotNull();

                EventSigningKey signingKey = aSave.getSigningKey();
                EventAddress address = aSave.getEventAddress();
                assertThat(signingKey).isNotNull();
                assertThat(signingKey.getId()).isNotNull();
                assertThat(address).isNotNull();
                assertThat(address.getId()).isNotNull();

                return aSave;
            });

            // After the lambda returns, the transaction should commit,
            // so everything should be persisted.
            assertThat(eventRepository.findById(saved.getId())).isPresent();
            assertThat(eventAddressRepository.findById(saved.getEventAddress().getId())).isPresent();
            assertThat(eventSigningKeyRepository.findById(saved.getSigningKey().getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Uncommitted changes are not visible after rollback")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void uncommittedChangesNotVisibleAfterRollback()
        {
            Long eventId = txTemplate.execute(status ->
            {
                Event event = createEvent(UUID.randomUUID().toString());
                Event saved = eventRepository.saveAndFlush(event);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                Long id = saved.getId();

                // Within the transaction, check that the event is visible
                assertThat(eventRepository.findById(id)).isPresent();

                // Flag the transaction for rollback
                status.setRollbackOnly();
                return id;
            });
            // After rollback, the event should not be visible
            assertThat(eventRepository.findById(eventId)).isEmpty();
        }

        @Test
        @DisplayName("Committed changes are visible in subsequent transactions")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void committedChangesVisibleInSubsequentTransaction()
        {
            String publicId = UUID.randomUUID().toString();

            // First transaction: save and commit
            Long eventId = txTemplate.execute(status ->
            {
                Event event = createEvent(publicId);
                Event saved = eventRepository.saveAndFlush(event);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            // Second transaction: should see the committed data
            txTemplate.execute(status ->
            {
                Optional<Event> loaded = eventRepository.findById(eventId);
                assertThat(loaded).isPresent();
                assertThat(loaded.get().getPublicId()).isEqualTo(publicId);
                return null;
            });
        }
    }
}
