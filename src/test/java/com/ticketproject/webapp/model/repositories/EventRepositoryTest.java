package com.ticketproject.webapp.model.repositories;

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

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
 * EventRepositoryTest contains integration tests for the Event entity
 * and EventRepository, covering rollback on failure, data constraints,
 * data integrity, cascade operations, and commit atomicity.
 */
@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
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

    private EventHost savedHost;

    /**
     * Creates and persists a shared EventHost used across tests.
     */
    @BeforeEach
    void setUp()
    {
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
            generator.initialize(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_SIZE_PROD);
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
        @DisplayName("Failed save does not persist partial data")
        void failedSaveDoesNotPersistPartialData()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            // Make the event invalid after setting up the address
            event.setPublicId(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
            
            // Since a failed transaction should automatically be flagged
            // by Spring as rollback-only, end the poisoned transaction
            // and start a clean one.
            TestTransaction.end();
            TestTransaction.start();

            // The event should not have been persisted
            assertThat(eventRepository.findAll()).isEmpty();
            // The address should not have been persisted either
            assertThat(eventAddressRepository.findAll()).isEmpty();
            // The signing key should not have been persisted either
            assertThat(eventSigningKeyRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Constraint violation on second save rolls back the first save in same transaction")
        void constraintViolationRollsBackPriorSave()
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
            assertThatThrownBy(() -> eventRepository.saveAndFlush(event2))
                .isInstanceOf(DataIntegrityViolationException.class);
            
            // End the poisoned transaction (Spring should automatically
            // mark it as rollback-only) and start a fresh one
            TestTransaction.end();
            TestTransaction.start();

            // There should be no Event, EventAddress, or EventSigningKey entities at this point
            assertThat(eventRepository.count()).isZero();
            assertThat(eventAddressRepository.count()).isZero();
            assertThat(eventSigningKeyRepository.count()).isZero();
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
        void successfulTransactionCommitsAll()
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

            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();

            // After commit, everything should be persisted
            assertThat(eventRepository.findById(saved.getId())).isPresent();
            assertThat(eventAddressRepository.findById(address.getId())).isPresent();
            assertThat(eventSigningKeyRepository.findById(signingKey.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Uncommitted changes are not visible after rollback")
        void uncommittedChangesNotVisibleAfterRollback()
        {
            Event event = createEvent(UUID.randomUUID().toString());
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Long eventId = saved.getId();

            // Within the transaction, check that the event is visible
            assertThat(eventRepository.findById(eventId)).isPresent();

            // Roll back and start a new transaction
            TestTransaction.flagForRollback();
            TestTransaction.end();
            TestTransaction.start();

            // After rollback, the event should not be visible
            assertThat(eventRepository.findById(eventId)).isEmpty();
        }

        @Test
        @DisplayName("Committed changes are visible in subsequent transactions")
        void committedChangesVisibleInSubsequentTransaction()
        {
            String publicId = UUID.randomUUID().toString();

            // First transaction: save and commit
            Event event = createEvent(publicId);
            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Long eventId = saved.getId();
            TestTransaction.flagForCommit();
            TestTransaction.end();

            // Second transaction: should see the committed data
            TestTransaction.start();
            Optional<Event> loaded = eventRepository.findById(eventId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getPublicId()).isEqualTo(publicId);
        }
    }
}
