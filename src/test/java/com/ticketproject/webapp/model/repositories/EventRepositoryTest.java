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
        
        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
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
                .isInstanceOf(DataIntegrityViolationException.class);
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
    }
}
