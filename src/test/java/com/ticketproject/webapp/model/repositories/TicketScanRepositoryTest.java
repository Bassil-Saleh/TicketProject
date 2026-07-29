package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.entities.TicketScan;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.InvitationStatus;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TicketScanRepositoryTest contains integration tests for the TicketScan entity
 * and TicketScanRepository, covering rollback on failure, data constraints,
 * data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TicketScanRepositoryTest
{
    @Autowired
    private TicketScanRepository ticketScanRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Ticket savedTicket;
    private EventHost savedScanner;
    private TransactionTemplate txTemplate;

    /**
     * Creates and persists shared entities used across tests.
     */
    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

        Attendee attendee = new Attendee.Builder()
            .firstName("Scan")
            .lastName("Attendee")
            .email("scan-" + UUID.randomUUID() + "@example.com")
            .build();
        Attendee savedAttendee = attendeeRepository.saveAndFlush(attendee);

        EventHost host = new EventHost.Builder()
            .firstName("Scan")
            .lastName("Host")
            .dateOfBirth(LocalDate.of(1987, 8, 8))
            .email("scanhost-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        EventHost savedHost = eventHostRepository.saveAndFlush(host);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("300 Scan Ave")
            .city("Scantown")
            .state("FL")
            .postalCode("33101")
            .country("USA")
            .build();

        Event event = new Event.Builder()
            .publicId(UUID.randomUUID().toString())
            .eventHost(savedHost)
            .name("Scan Event")
            .description("An event for ticket scan tests")
            .startDateTime(LocalDateTime.now().plusDays(5))
            .endDateTime(LocalDateTime.now().plusDays(5).plusHours(3))
            .eventType(EventType.PUBLIC)
            .maxAttendees(300)
            .build();
        
        EventSigningKey signingKey = createSigningKey(event);

        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.PUBLISHED);
        event.setSigningKey(signingKey);
        Event savedEvent = eventRepository.saveAndFlush(event);

        Ticket ticket = new Ticket.Builder()
            .publicToken(UUID.randomUUID().toString())
            .tokenIdentifier(UUID.randomUUID().toString())
            .attendee(savedAttendee)
            .event(savedEvent)
            .invitationStatus(InvitationStatus.ACCEPTED)
            .build();
        savedTicket = ticketRepository.saveAndFlush(ticket);

        // Create a separate EventHost who will act as the scanner
        EventHost scanner = new EventHost.Builder()
            .firstName("Scanner")
            .lastName("Person")
            .dateOfBirth(LocalDate.of(1990, 2, 2))
            .email("scanner-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        scanner.generateVerificationToken();
        savedScanner = eventHostRepository.saveAndFlush(scanner);
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
     * Helper method to create a valid TicketScan entity.
     * @return a new TicketScan entity (not yet persisted)
     */
    private TicketScan createScan()
    {
        return new TicketScan.Builder()
            .ticket(savedTicket)
            .scannedBy(savedScanner)
            .scannedAt(LocalDateTime.now())
            .deviceInfo("Pixel 7, Android 14")
            .build();
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving scan with null ticket violates NOT NULL constraint")
        void nullTicketThrowsException()
        {
            TicketScan scan = createScan();
            scan.setTicket(null);

            assertThatThrownBy(() -> ticketScanRepository.saveAndFlush(scan))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving scan with null scannedBy violates NOT NULL constraint")
        void nullScannedByThrowsException()
        {
            TicketScan scan = createScan();
            scan.setScannedBy(null);

            assertThatThrownBy(() -> ticketScanRepository.saveAndFlush(scan))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving scan with null scannedAt violates NOT NULL constraint")
        void nullScannedAtThrowsException()
        {
            TicketScan scan = createScan();
            scan.setScannedAt(null);

            assertThatThrownBy(() -> ticketScanRepository.saveAndFlush(scan))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving scan with null deviceInfo is allowed (optional field)")
        void nullDeviceInfoIsAllowed()
        {
            TicketScan scan = createScan();
            scan.setDeviceInfo(null);

            TicketScan saved = ticketScanRepository.saveAndFlush(scan);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDeviceInfo()).isNull();
        }
    }
}
