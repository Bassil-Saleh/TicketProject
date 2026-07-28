package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.AddressBookContact;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AddressBookContactRepositoryTest contains integration tests for the
 * AddressBookContact entity and AddressBookContactRepository, covering
 * rollback on failure, data constraints, data integrity, and commit atomicity.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AddressBookContactRepositoryTest
{
    @Autowired
    private AddressBookContactRepository addressBookContactRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private EventHostRepository eventHostRepository;

    private Attendee savedAttendee;
    private EventHost savedHost;

    /**
     * Creates and persists shared Attendee and EventHost entities used across tests.
     */
    @BeforeEach
    void setUp()
    {
        Attendee attendee = new Attendee.Builder()
            .firstName("Contact")
            .lastName("Person")
            .email("contact-" + UUID.randomUUID() + "@example.com")
            .build();
        savedAttendee = attendeeRepository.saveAndFlush(attendee);

        EventHost host = new EventHost.Builder()
            .firstName("Book")
            .lastName("Owner")
            .dateOfBirth(LocalDate.of(1988, 3, 20))
            .email("bookowner-" + UUID.randomUUID() + "@example.com")
            .plaintextPassword("password123")
            .build();
        host.generateVerificationToken();
        savedHost = eventHostRepository.saveAndFlush(host);
    }

    /**
     * Helper method to create a valid AddressBookContact entity.
     * @return a new AddressBookContact entity (not yet persisted)
     */
    private AddressBookContact createContact()
    {
        return new AddressBookContact.Builder()
            .attendee(savedAttendee)
            .eventHost(savedHost)
            .build();
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving contact with null attendee violates NOT NULL constraint")
        void nullAttendeeThrowsException()
        {
            AddressBookContact contact = createContact();
            contact.setAttendee(null);

            assertThatThrownBy(() -> addressBookContactRepository.saveAndFlush(contact))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving contact with null eventHost violates NOT NULL constraint")
        void nullEventHostThrowsException()
        {
            AddressBookContact contact = createContact();
            contact.setEventHost(null);

            assertThatThrownBy(() -> addressBookContactRepository.saveAndFlush(contact))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating contact with null attendee violates NOT NULL constraint")
        void updateNullAttendeeThrowsException()
        {
            AddressBookContact contact = createContact();
            AddressBookContact saved = addressBookContactRepository.saveAndFlush(contact);
            assertThat(saved).isNotNull();
            saved.setAttendee(null);

            assertThatThrownBy(() -> addressBookContactRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating contact with null eventHost violates NOT NULL constraint")
        void updateNullEventHostThrowsException()
        {
            AddressBookContact contact = createContact();
            AddressBookContact saved = addressBookContactRepository.saveAndFlush(contact);
            assertThat(saved).isNotNull();
            saved.setEventHost(null);

            assertThatThrownBy(() -> addressBookContactRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Duplicate composite key (attendee_id, event_host_id) violates unique constraint")
        void duplicateCompositeKeyThrowsException()
        {
            AddressBookContact contact1 = createContact();
            contact1 = addressBookContactRepository.saveAndFlush(contact1);

            AddressBookContact contact2 = createContact();

            assertThatThrownBy(() -> addressBookContactRepository.saveAndFlush(contact2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Same attendee with different hosts is allowed")
        void sameAttendeeDifferentHostsAllowed()
        {
            AddressBookContact contact1 = createContact();
            contact1 = addressBookContactRepository.saveAndFlush(contact1);

            EventHost host2 = new EventHost.Builder()
                .firstName("Other")
                .lastName("Host")
                .dateOfBirth(LocalDate.of(1992, 7, 10))
                .email("otherhost-" + UUID.randomUUID() + "@example.com")
                .plaintextPassword("password123")
                .build();
            host2.generateVerificationToken();
            EventHost savedHost2 = eventHostRepository.saveAndFlush(host2);

            AddressBookContact contact2 = new AddressBookContact.Builder()
                .attendee(savedAttendee)
                .eventHost(savedHost2)
                .build();

            AddressBookContact saved = addressBookContactRepository.saveAndFlush(contact2);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(addressBookContactRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Same host with different attendees is allowed")
        void sameHostDifferentAttendeesAllowed()
        {
            AddressBookContact contact1 = createContact();
            contact1 = addressBookContactRepository.saveAndFlush(contact1);

            Attendee attendee2 = new Attendee.Builder()
                .firstName("Another")
                .lastName("Contact")
                .email("another-" + UUID.randomUUID() + "@example.com")
                .build();
            Attendee savedAttendee2 = attendeeRepository.saveAndFlush(attendee2);

            AddressBookContact contact2 = new AddressBookContact.Builder()
                .attendee(savedAttendee2)
                .eventHost(savedHost)
                .build();

            AddressBookContact saved = addressBookContactRepository.saveAndFlush(contact2);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(addressBookContactRepository.findAll()).hasSize(2);
        }
    }
}