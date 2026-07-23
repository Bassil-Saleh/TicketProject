package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
class EventHostRepositoryTest
{
    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private BlindIndexService blindIndexService;

    @Autowired
    private HashingService hashingService;

    /**
     * Helper method used to create valid EventHost entities.
     * @param email an email address
     * @return a new EventHost entity
     */
    private EventHost createHost(String email)
    {
        EventHost host = new EventHost.Builder()
            .firstName("John")
            .lastName("Doe")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .email(email)
            .build();
        
        host.setPassword("blahBlah123");
        host.setCreated(LocalDateTime.now());
        host.setLastLogin(LocalDateTime.now());
        host.setLastUpdated(LocalDateTime.now());
        host.setActive(false);
        host.generateVerificationToken();
        host.setVerified(false);
        host.setVerificationExpires(LocalDateTime.now().plusHours(1));

        return host;
    }

    @Nested
    @DisplayName("Encryption round-trip")
    class EncryptionRoundTrip
    {
        @Test
        @DisplayName("Email survives encrypt -> save -> load -> decrypt")
        void emailSurvivesRoundTrip()
        {
            // 1. Create a new host.
            EventHost host = createHost("alice@example.com");
            // 2. Save the new host into the in-memory database.
            EventHost saved = eventHostRepository.save(host);
            // 3. Load a fresh copy of the saved host,
            // bypassing Hibernate's first-level cache.
            EventHost loaded = eventHostRepository.findById(saved.getId()).orElseThrow();
            // 4. Assert that the plaintext email is correct.
            assertThat(loaded.getEmail()).isEqualTo("alice@example.com");
        }
    }
}