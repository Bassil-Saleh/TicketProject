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
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.BlockedRegistration;
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
 * BlockedRegistrationRepositoryTest contains integration tests for the
 * BlockedRegistration entity and BlockedRegistrationRepository, covering
 * rollback on failure, data constraints, data integrity, and commit atomicity.
 */
@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
class BlockedRegistrationRepositoryTest
{
    @Autowired
    private BlockedRegistrationRepository blockedRegistrationRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Attendee savedAttendee;
    private Event savedEvent;
    private EventHost savedHost;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists shared entities used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

        savedAttendee = attendeeRepository.saveAndFlush
        (
            new Attendee.Builder()
                .firstName("Blocked")
                .lastName("User")
                .email("blocked-" + UUID.randomUUID() + "@example.com")
                .build()
        );

        EventHost host = new EventHost.Builder()
            .firstName("Blocker")
            .lastName("Host")
            .dateOfBirth(LocalDate.of(1980, 12, 1))
            .email("blocker-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        savedHost = eventHostRepository.saveAndFlush(host);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("100 Block St")
            .city("Blocktown")
            .state("CA")
            .postalCode("90001")
            .country("USA")
            .build();

        Event event = new Event.Builder()
            .publicId(UUID.randomUUID().toString())
            .eventHost(savedHost)
            .name("Blocked Event")
            .description("An event with blocked registrations")
            .startDateTime(LocalDateTime.now().plusDays(30))
            .endDateTime(LocalDateTime.now().plusDays(30).plusHours(2))
            .eventType(EventType.PUBLIC)
            .maxAttendees(200)
            .build();

        EventSigningKey signingKey = createSigningKey(event);

        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.PUBLISHED);
        event.setSigningKey(signingKey);
        savedEvent = eventRepository.saveAndFlush(event);
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

    /**
     * Helper method to create a valid BlockedRegistration entity.
     * @return a new BlockedRegistration entity (not yet persisted)
     */
    private BlockedRegistration createBlock()
    {
        return new BlockedRegistration.Builder()
            .attendee(savedAttendee)
            .event(savedEvent)
            .blockedBy(savedHost)
            .reason("Spam registration")
            .build();
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving block with null attendee violates NOT NULL constraint")
        void nullAttendeeThrowsException()
        {
            BlockedRegistration block = createBlock();
            block.setAttendee(null);

            assertThatThrownBy(() -> blockedRegistrationRepository.saveAndFlush(block))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving block with null event violates NOT NULL constraint")
        void nullEventThrowsException()
        {
            BlockedRegistration block = createBlock();
            block.setEvent(null);

            assertThatThrownBy(() -> blockedRegistrationRepository.saveAndFlush(block))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving block with null blockedBy violates NOT NULL constraint")
        void nullBlockedByThrowsException()
        {
            BlockedRegistration block = createBlock();
            block.setBlockedBy(null);

            assertThatThrownBy(() -> blockedRegistrationRepository.saveAndFlush(block))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving block with null reason is allowed (optional field)")
        void nullReasonIsAllowed()
        {
            BlockedRegistration block = createBlock();
            block.setReason(null);

            BlockedRegistration saved = blockedRegistrationRepository.saveAndFlush(block);
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getReason()).isNull();
        }
    }
}