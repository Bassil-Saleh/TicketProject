package com.ticketproject.webapp.model.repositories;

import java.util.Optional;

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
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AttendeeRepositoryTest
{
    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private BlindIndexService blindIndexService;

    /**
     * Helper method use to create valid Attendee entities.
     * @param email an email address
     * @return a new Attendee entity
     */
    private Attendee createAttendee(String email)
    {
        Attendee attendee = new Attendee.Builder()
            .firstName("Ariel")
            .lastName("Pearl")
            .email(email)
            .build();
        return attendee;
    }

    @Nested
    @DisplayName("Encryption round-trip")
    class EncryptionRoundTrip
    {
        @Test
        @DisplayName("Email survives encrypt -> save -> load -> decrypt")
        void emailSurvivesRoundTrip()
        {
            // 1. Create a new attendee.
            Attendee attendee = createAttendee("ariel@coralReef.net");
            // 2. Save the new attendee into the in-memory database.
            Attendee saved = attendeeRepository.save(attendee);
            // 3. Load a fresh copy of the saved attendee,
            // bypassing Hibernate's first-level cache.
            Attendee loaded = attendeeRepository.findById(saved.getId()).orElseThrow();
            // 4. Assert that the plaintext email is correct.
            assertThat(loaded.getEmail()).isEqualTo("ariel@coralReef.net");
        }
    }

    @Nested
    @DisplayName("Blind index queries")
    class BlindIndexQueries
    {
        @Test
        @DisplayName("Find by email blind index returns the correct event host")
        void findByBlindIndex()
        {
            Attendee attendee = createAttendee("sid@yourBackyard.com");
            attendeeRepository.save(attendee);

            byte[] index = blindIndexService.computeIndex("sid@yourBackyard.com");

            Optional<Attendee> found = attendeeRepository.findByEmailIndex(index);

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("sid@yourBackyard.com");
        }

        @Test
        @DisplayName("Normalized email lookup: whitespace and case")
        void blindIndexIsNormalized()
        {
            Attendee attendee = createAttendee("   CaRoL@eXaMpLe.cOm    ");
            attendeeRepository.save(attendee);

            // Should match since normalization strips trailing & leading whitespace
            // and also converts the email to lowercase.
            byte[] index = blindIndexService.computeIndex("carol@example.com");

            Optional<Attendee> found = attendeeRepository.findByEmailIndex(index);

            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Different emails produce different blind indexes")
        void uniqueBlindIndexes()
        {
            Attendee cammy = createAttendee("cammy@boringStupidWebsite.com");
            Attendee jerry = createAttendee("jerry@reallyCoolWebsite.org");

            attendeeRepository.save(cammy);
            attendeeRepository.save(jerry);

            byte[] cammyIndex = blindIndexService.computeIndex("cammy@boringStupidWebsite.com");
            byte[] jerryIndex = blindIndexService.computeIndex("jerry@reallyCoolWebsite.org");

            assertThat(cammyIndex).isNotEqualTo(jerryIndex);
        }
    }

    @Nested
    @DisplayName("Uniqueness constraint")
    class UniquenessConstraint
    {
        @Test
        @DisplayName("Duplicate email blind index violates unique constraint")
        void duplicateBlindIndexThrowsException()
        {
            Attendee attendee1 = createAttendee("hugo@yourBank.com");
            attendeeRepository.saveAndFlush(attendee1);
            Attendee attendee2 = createAttendee("hugo@yourBank.com");

            // This should throw an exception because
            // emailBlindIndex has a UNIQUE constraint.
            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(attendee2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
