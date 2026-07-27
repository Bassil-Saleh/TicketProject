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
import org.springframework.test.context.transaction.TestTransaction;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.Ticket;
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
 * TicketRepositoryTest contains integration tests for the Ticket entity
 * and TicketRepository, covering rollback on failure, data constraints,
 * data integrity, and commit atomicity.
 */
@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
class TicketRepositoryTest
{
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    private Attendee savedAttendee;
    private Event savedEvent;

    @BeforeEach
    void setUp()
    {
        Attendee attendee = new Attendee.Builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("attendee-" + UUID.randomUUID() + "@example.com")
            .build();
        savedAttendee = attendeeRepository.saveAndFlush(attendee);

        EventHost host = new EventHost.Builder()
            .firstName("Host")
            .lastName("User")
            .dateOfBirth(LocalDate.of(1985, 6, 15))
            .email("host-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        EventHost savedHost = eventHostRepository.saveAndFlush(host);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("789 Elm St")
            .city("Capital City")
            .state("NY")
            .postalCode("10001")
            .country("USA")
            .build();

        Event event = new Event.Builder()
            .publicId(UUID.randomUUID().toString())
            .eventHost(savedHost)
            .name("Concert")
            .description("A live concert event")
            .startDateTime(LocalDateTime.now().plusDays(14))
            .endDateTime(LocalDateTime.now().plusDays(14).plusHours(4))
            .eventType(EventType.PUBLIC)
            .maxAttendees(500)
            .build();

        EventSigningKey signingKey = createSigningKey(event);

        event.setSigningKey(signingKey);
        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.PUBLISHED);
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
     * Helper method to create a valid Ticket entity.
     * @return a new Ticket entity (not yet persisted)
     */
    private Ticket createTicket()
    {
        return new Ticket.Builder()
            .publicToken(UUID.randomUUID().toString())
            .tokenIdentifier(UUID.randomUUID().toString())
            .attendee(savedAttendee)
            .event(savedEvent)
            .invitationStatus(InvitationStatus.ACCEPTED)
            .build();
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving ticket with null publicToken violates NOT NULL constraint")
        void nullPublicTokenThrowsException()
        {
            Ticket ticket = createTicket();
            ticket.setPublicToken(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving ticket with null tokenIdentifier violates NOT NULL constraint")
        void nullTokenIdentifierThrowsException()
        {
            Ticket ticket = createTicket();
            ticket.setTokenIdentifier(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving ticket with null attendee violates NOT NULL constraint")
        void nullAttendeeThrowsException()
        {
            Ticket ticket = createTicket();
            ticket.setAttendee(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving ticket with null event violates NOT NULL constraint")
        void nullEventThrowsException()
        {
            Ticket ticket = createTicket();
            ticket.setEvent(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving ticket with null invitationStatus violates NOT NULL constraint")
        void nullInvitationStatusThrowsException()
        {
            Ticket ticket = createTicket();
            ticket.setInvitationStatus(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating ticket with null publicToken violates NOT NULL constraint")
        void updateNullPublicTokenThrowsException()
        {
            Ticket ticket = createTicket();
            Ticket saved = ticketRepository.saveAndFlush(ticket);
            assertThat(saved).isNotNull();
            saved.setPublicToken(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating ticket with null tokenIdentifier violates NOT NULL constraint")
        void updateNullTokenIdentifierThrowsException()
        {
            Ticket ticket = createTicket();
            Ticket saved = ticketRepository.saveAndFlush(ticket);
            assertThat(saved).isNotNull();
            saved.setTokenIdentifier(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating ticket with null attendee violates NOT NULL constraint")
        void updateNullAttendeeThrowsException()
        {
            Ticket ticket = createTicket();
            Ticket saved = ticketRepository.saveAndFlush(ticket);
            assertThat(saved).isNotNull();
            saved.setAttendee(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating ticket with null event violates NOT NULL constraint")
        void updateNullEventThrowsException()
        {
            Ticket ticket = createTicket();
            Ticket saved = ticketRepository.saveAndFlush(ticket);
            assertThat(saved).isNotNull();
            saved.setEvent(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating ticket with null invitationStatus violates NOT NULL constraint")
        void updateNullInvitationStatusThrowsException()
        {
            Ticket ticket = createTicket();
            Ticket saved = ticketRepository.saveAndFlush(ticket);
            assertThat(saved).isNotNull();
            saved.setInvitationStatus(null);

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate publicToken violates unique constraint")
        void duplicatePublicTokenThrowsException()
        {
            String token = UUID.randomUUID().toString();
            Ticket ticket1 = createTicket();
            ticket1.setPublicToken(token);
            ticket1 = ticketRepository.saveAndFlush(ticket1);

            // Create a second attendee so the composite unique (attendee, event) is not violated
            Attendee attendee2 = new Attendee.Builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob-" + UUID.randomUUID() + "@example.com")
                .build();
            Attendee savedAttendee2 = attendeeRepository.saveAndFlush(attendee2);

            Ticket ticket2 = createTicket();
            ticket2.setPublicToken(token);
            ticket2.setAttendee(savedAttendee2);
            ticket2.setTokenIdentifier(UUID.randomUUID().toString());

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}