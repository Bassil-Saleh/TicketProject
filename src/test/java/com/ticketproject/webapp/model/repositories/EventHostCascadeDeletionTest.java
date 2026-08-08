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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.AddressBookContact;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.BlockedRegistration;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.PasswordResetToken;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.entities.TicketScan;
import com.ticketproject.webapp.model.enums.ClientType;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.InvitationStatus;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.services.database.BlindIndexService;
import com.ticketproject.webapp.services.database.CryptoService;
import com.ticketproject.webapp.services.database.HashingService;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EventHostCascadeDeletionTest verifies that deleting an EventHost
 * via JPA's delete() method causes all orphaned child records to be
 * automatically removed from the database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventHostCascadeDeletionTest
{
    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventAddressRepository eventAddressRepository;

    @Autowired
    private EventSigningKeyRepository eventSigningKeyRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketScanRepository ticketScanRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AddressBookContactRepository addressBookContactRepository;

    @Autowired
    private BlockedRegistrationRepository blockedRegistrationRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Long eventHostId;
    private Long eventId;
    private Long eventAddressId;
    private Long eventSigningKeyId;
    private Long ticketId;
    private Long ticketScanId;
    private Long sessionId;
    private Long passwordResetTokenId;
    private Long addressBookContactId;
    private Long blockedRegistrationId;

    @BeforeEach
    void setUp()
    {
        // 1. Create and persist the EventHost
        EventHost host = new EventHost.Builder()
            .firstName("Cascade")
            .lastName("Test")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .email("cascade-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        EventHost savedHost = eventHostRepository.saveAndFlush(host);
        eventHostId = savedHost.getId();

        // 2. Create and persist an Attendee (needed for Ticket, BlockedRegistration, AddressBookContact)
        Attendee attendee = new Attendee.Builder()
            .firstName("Cascade")
            .lastName("Attendee")
            .email("cascade-attendee-" + UUID.randomUUID() + "@example.com")
            .build();
        Attendee savedAttendee = attendeeRepository.saveAndFlush(attendee);

        // 3. Create EventAddress (will be cascaded via Event)
        EventAddress address = new EventAddress.Builder()
            .addressLine1("123 Cascade St")
            .city("Testville")
            .state("TS")
            .postalCode("12345")
            .country("Testland")
            .build();

        // 4. Create Event
        Event event = new Event.Builder()
            .publicId(UUID.randomUUID().toString())
            .eventHost(savedHost)
            .name("Cascade Test Event")
            .description("An event for testing cascade deletion")
            .startDateTime(LocalDateTime.now().plusDays(7))
            .endDateTime(LocalDateTime.now().plusDays(7).plusHours(3))
            .eventType(EventType.PUBLIC)
            .maxAttendees(100)
            .build();

        // 5. Create EventSigningKey (will be cascaded via Event)
        EventSigningKey signingKey = CryptoService.createSigningKey(event, AppConstants.Crypto.PUBLIC_PRIVATE_KEY_SIZE_TEST);

        event.setEventAddress(address);
        event.setSigningKey(signingKey);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.PUBLISHED);
        Event savedEvent = eventRepository.saveAndFlush(event);
        eventId = savedEvent.getId();
        eventAddressId = savedEvent.getEventAddress().getId();
        eventSigningKeyId = savedEvent.getSigningKey().getId();

        // 6. Create and persist a Ticket (for the event and attendee)
        Ticket ticket = new Ticket.Builder()
            .publicToken(UUID.randomUUID().toString())
            .tokenIdentifier(UUID.randomUUID().toString())
            .attendee(savedAttendee)
            .event(savedEvent)
            .invitationStatus(InvitationStatus.ACCEPTED)
            .build();
        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);
        ticketId = savedTicket.getId();

        // 7. Create and persist a TicketScan (scanned by the host)
        TicketScan ticketScan = new TicketScan.Builder()
            .ticket(savedTicket)
            .scannedBy(savedHost)
            .scannedAt(LocalDateTime.now())
            .deviceInfo("Test Device")
            .build();
        TicketScan savedTicketScan = ticketScanRepository.saveAndFlush(ticketScan);
        ticketScanId = savedTicketScan.getId();

        // 8. Create and persist a Session (for the host)
        Session session = new Session.Builder()
            .eventHost(savedHost)
            .clientType(ClientType.WEB)
            .ipAddress("192.168.1.1")
            .userAgent("TestAgent/1.0")
            .build();
        session.generateToken();
        Session savedSession = sessionRepository.saveAndFlush(session);
        sessionId = savedSession.getId();

        // 9. Create and persist a PasswordResetToken (for the host)
        PasswordResetToken resetToken = new PasswordResetToken.Builder()
            .eventHost(savedHost)
            .tokenHash(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
            .build();
        resetToken.generateToken();
        PasswordResetToken savedResetToken = passwordResetTokenRepository.saveAndFlush(resetToken);
        passwordResetTokenId = savedResetToken.getId();

        // 10. Create and persist an AddressBookContact (for the host and attendee)
        AddressBookContact contact = new AddressBookContact.Builder()
            .attendee(savedAttendee)
            .eventHost(savedHost)
            .build();
        AddressBookContact savedContact = addressBookContactRepository.saveAndFlush(contact);
        addressBookContactId = savedContact.getId();

        // 11. Create a second Attendee for the second BlockedRegistration
        Attendee attendee2 = new Attendee.Builder()
            .firstName("Cascade")
            .lastName("Attendee2")
            .email("cascade-attendee2-" + UUID.randomUUID() + "@example.com")
            .build();
        Attendee savedAttendee2 = attendeeRepository.saveAndFlush(attendee2);

        // 12. Create and persist a BlockedRegistration (blocked by the host, for the host's event)
        BlockedRegistration block = new BlockedRegistration.Builder()
            .attendee(savedAttendee2)
            .event(savedEvent)
            .blockedBy(savedHost)
            .reason("Testing cascade deletion")
            .build();
        BlockedRegistration savedBlock = blockedRegistrationRepository.saveAndFlush(block);
        blockedRegistrationId = savedBlock.getId();
    }

    /**
     * Deletes the EventHost and flushes, after clearing the persistence context.
     * This helper avoids repeating the clear/re-fetch/delete/flush pattern in every test.
     */
    private void deleteEventHost()
    {
        entityManager.clear();
        EventHost hostToDelete = eventHostRepository.findById(eventHostId).orElseThrow();
        eventHostRepository.delete(hostToDelete);
        eventHostRepository.flush();
    }

    @Nested
    @DisplayName("Direct children of EventHost")
    class DirectChildren
    {
        @Test
        @DisplayName("Deleting EventHost removes associated Events")
        void deletingEventHostRemovesEvents()
        {
            deleteEventHost();
            assertThat(eventRepository.findById(eventId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost removes associated Sessions")
        void deletingEventHostRemovesSessions()
        {
            deleteEventHost();
            assertThat(sessionRepository.findById(sessionId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost removes associated PasswordResetTokens")
        void deletingEventHostRemovesPasswordResetTokens()
        {
            deleteEventHost();
            assertThat(passwordResetTokenRepository.findById(passwordResetTokenId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost removes associated AddressBookContacts")
        void deletingEventHostRemovesAddressBookContacts()
        {
            deleteEventHost();
            assertThat(addressBookContactRepository.findById(addressBookContactId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost removes BlockedRegistrations where blockedBy is the host")
        void deletingEventHostRemovesBlockedRegistrationsViaBlockedBy()
        {
            deleteEventHost();
            assertThat(blockedRegistrationRepository.findById(blockedRegistrationId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost removes TicketScans where scannedBy is the host")
        void deletingEventHostRemovesTicketScansViaScannedBy()
        {
            deleteEventHost();
            assertThat(ticketScanRepository.findById(ticketScanId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cascade through Event to deeper children")
    class CascadeThroughEvent
    {
        @Test
        @DisplayName("Deleting EventHost cascades through Event to remove EventAddress")
        void deletingEventHostCascadesThroughEventToEventAddress()
        {
            deleteEventHost();
            assertThat(eventAddressRepository.findById(eventAddressId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost cascades through Event to remove EventSigningKey")
        void deletingEventHostCascadesThroughEventToEventSigningKey()
        {
            deleteEventHost();
            assertThat(eventSigningKeyRepository.findById(eventSigningKeyId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost cascades through Event to remove Tickets")
        void deletingEventHostCascadesThroughEventToTickets()
        {
            deleteEventHost();
            assertThat(ticketRepository.findById(ticketId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost cascades through Event and Ticket to remove TicketScans")
        void deletingEventHostCascadesThroughEventToTicketScans()
        {
            deleteEventHost();
            assertThat(ticketScanRepository.findById(ticketScanId)).isEmpty();
        }

        @Test
        @DisplayName("Deleting EventHost cascades through Event to remove BlockedRegistrations")
        void deletingEventHostCascadesThroughEventToBlockedRegistrations()
        {
            deleteEventHost();
            assertThat(blockedRegistrationRepository.findById(blockedRegistrationId)).isEmpty();
        }
    }
}
