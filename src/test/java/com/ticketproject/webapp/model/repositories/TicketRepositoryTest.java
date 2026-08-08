package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.DataSource;

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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

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
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TicketRepositoryTest contains integration tests for the Ticket entity
 * and TicketRepository, covering rollback on failure, data constraints,
 * data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Attendee savedAttendee;
    private Event savedEvent;
    private TransactionTemplate txTemplate;

    @BeforeEach
    void setUp()
    {
        // Initialize a TransactionTemplate to programmatically manage transactions
        txTemplate = new TransactionTemplate(transactionManager);

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

        EventSigningKey signingKey = CryptoService.createSigningKey(event, AppConstants.Crypto.PUBLIC_PRIVATE_KEY_SIZE_TEST);

        event.setSigningKey(signingKey);
        event.setEventAddress(address);
        event.setRegistrationStatus(RegistrationStatus.OPEN);
        event.setEventStatus(EventStatus.PUBLISHED);
        savedEvent = eventRepository.saveAndFlush(event);
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

        @Test
        @DisplayName("Duplicate tokenIdentifier violates unique constraint")
        void duplicateTokenIdentifierThrowsException()
        {
            String identifier = UUID.randomUUID().toString();
            Ticket ticket1 = createTicket();
            ticket1.setTokenIdentifier(identifier);
            ticket1 = ticketRepository.saveAndFlush(ticket1);

            Attendee attendee2 = new Attendee.Builder()
                .firstName("Carol")
                .lastName("Jones")
                .email("carol-" + UUID.randomUUID() + "@example.com")
                .build();
            Attendee savedAttendee2 = attendeeRepository.saveAndFlush(attendee2);

            Ticket ticket2 = createTicket();
            ticket2.setTokenIdentifier(identifier);
            ticket2.setAttendee(savedAttendee2);
            ticket2.setPublicToken(UUID.randomUUID().toString());

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate composite key (attendee_id, event_id) violates unique constraint")
        void duplicateCompositeKeyThrowsException()
        {
            Ticket ticket1 = createTicket();
            ticket1 = ticketRepository.saveAndFlush(ticket1);

            Ticket ticket2 = createTicket();
            // Same attendee and event, different tokens
            ticket2.setPublicToken(UUID.randomUUID().toString());
            ticket2.setTokenIdentifier(UUID.randomUUID().toString());

            assertThatThrownBy(() -> ticketRepository.saveAndFlush(ticket2))
                .isInstanceOf(DataIntegrityViolationException.class);
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
        @DisplayName("Failed ticket save does not persist the ticket")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void failedSaveDoesNotPersist()
        {
            Ticket ticket = createTicket();
            ticket.setPublicToken(null);

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                ticketRepository.saveAndFlush(ticket);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // An exception in the lambda should cause an automatic rollback.
            // Then this repository call should run in a new, short-lived read-only transaction.
            assertThat(ticketRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void constraintViolationRollsBackPriorSave()
        {
            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Ticket ticket1 = createTicket();
                ticket1 = ticketRepository.saveAndFlush(ticket1);
                assertThat(ticket1).isNotNull();
                assertThat(ticket1.getId()).isNotNull();
                assertThat(ticketRepository.findById(ticket1.getId())).isPresent();

                // Attempt to save a ticket with the same composite key
                Ticket ticket2 = createTicket();
                ticket2.setPublicToken(UUID.randomUUID().toString());
                ticket2.setTokenIdentifier(UUID.randomUUID().toString());

                // This should throw an exception, bubble out of the lambda,
                // and trigger a rollback.
                ticketRepository.saveAndFlush(ticket2);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            // There should be no Ticket entities persisted in the database.
            assertThat(ticketRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful ticket save commits atomically")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void successfulSaveCommitsAtomically()
        {
            Ticket ticket = createTicket();
            Ticket saved = txTemplate.execute(status -> ticketRepository.saveAndFlush(ticket));

            assertThat(ticketRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("Multiple ticket saves in one transaction all commit")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void multipleSavesCommitAtomic()
        {
            txTemplate.execute(status ->
            {
                Attendee attendee2 = new Attendee.Builder()
                    .firstName("Multi")
                    .lastName("Test")
                    .email("multi-" + UUID.randomUUID() + "@example.com")
                    .build();
                Attendee savedAttendee2 = attendeeRepository.saveAndFlush(attendee2);

                Ticket ticket1 = createTicket();
                ticket1 = ticketRepository.saveAndFlush(ticket1);

                Ticket ticket2 = new Ticket.Builder()
                    .publicToken(UUID.randomUUID().toString())
                    .tokenIdentifier(UUID.randomUUID().toString())
                    .attendee(savedAttendee2)
                    .event(savedEvent)
                    .invitationStatus(InvitationStatus.PENDING)
                    .build();
                ticket2 = ticketRepository.saveAndFlush(ticket2);

                return null;
            });

            assertThat(ticketRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back ticket is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rolledBackTicketNotVisible()
        {
            Long ticketId = txTemplate.execute(status ->
            {
                Ticket ticket = createTicket();
                Ticket saved = ticketRepository.saveAndFlush(ticket);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                Long id = saved.getId();

                assertThat(ticketRepository.findById(id)).isPresent();

                // Manually flag the transaction for rollback
                status.setRollbackOnly();
                return id;
            });

            assertThat(ticketRepository.findById(ticketId)).isEmpty();
        }

        @Test
        @DisplayName("Committed ticket is visible in subsequent transaction")
        void committedTicketVisible()
        {
            Ticket ticket = createTicket();
            String publicToken = ticket.getPublicToken();

            // First transaction: save and commit
            Long ticketId = txTemplate.execute(status ->
            {
                Ticket saved = ticketRepository.saveAndFlush(ticket);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });
            // Second transaction (implicit via Spring Data JPA read-only):
            // should see the committed data
            Optional<Ticket> loaded = ticketRepository.findById(ticketId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getPublicToken()).isEqualTo(publicToken);
        }
    }
}