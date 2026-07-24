package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
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
}
