package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidParameterException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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

    private Event savedEvent;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists a shared Event entity used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);
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
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);
            key.setEvent(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving signing key with null privateKey violates NOT NULL constraint")
        void nullPrivateKeyThrowsException()
        {
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);
            key.setPrivateKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving signing key with null publicKey violates NOT NULL constraint")
        void nullPublicKeyThrowsException()
        {
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);
            key.setPublicKey(null);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null event violates NOT NULL constraint")
        void updateNullEventThrowsException()
        {
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);

            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getSigningKey()).isNotNull();
            key = saved.getSigningKey();
            key.setEvent(null);
            saved.setSigningKey(key);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null privateKey violates NOT NULL constraint")
        void updateNullPrivateKeyThrowsException()
        {
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);

            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getSigningKey()).isNotNull();
            key = saved.getSigningKey();
            key.setPrivateKey(null);
            saved.setSigningKey(key);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating signing key with null publicKey violates NOT NULL constraint")
        void updateNullPublicKeyThrowsException()
        {
            Event event = createEvent();
            EventSigningKey key = createSigningKey(event);
            event.setSigningKey(key);

            Event saved = eventRepository.saveAndFlush(event);
            assertThat(saved).isNotNull();
            assertThat(saved.getSigningKey()).isNotNull();
            key = saved.getSigningKey();
            key.setPublicKey(null);
            saved.setSigningKey(key);

            assertThatThrownBy(() -> eventRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}