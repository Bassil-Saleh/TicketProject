package com.ticketproject.webapp.model.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
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

    @Nested
    @DisplayName("Blind index queries")
    class BlindIndexQueries
    {
        @Test
        @DisplayName("Find by email blind index returns the correct event host")
        void findByBlindIndex()
        {
            EventHost host = createHost("bob@example.com");
            eventHostRepository.save(host);

            byte[] index = blindIndexService.computeIndex("bob@example.com");

            Optional<EventHost> found = eventHostRepository.findByEmailIndex(index);

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("bob@example.com");
        }

        @Test
        @DisplayName("Normalized email lookup: whitespace and case")
        void blindIndexIsNormalized()
        {
            EventHost host = createHost("   CaRoL@eXaMpLe.cOm    ");
            eventHostRepository.save(host);

            // Should match since normalization strips trailing & leading whitespace
            // and also converts the email to lowercase.
            byte[] index = blindIndexService.computeIndex("carol@example.com");

            Optional<EventHost> found = eventHostRepository.findByEmailIndex(index);

            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Different emails produce different blind indexes")
        void uniqueBlindIndexes()
        {
            EventHost cammy = createHost("cammy@boringStupidWebsite.com");
            EventHost jerry = createHost("jerry@reallyCoolWebsite.org");

            eventHostRepository.save(cammy);
            eventHostRepository.save(jerry);

            byte[] cammyIndex = blindIndexService.computeIndex("cammy@boringStupidWebsite.com");
            byte[] jerryIndex = blindIndexService.computeIndex("jerry@reallyCoolWebsite.org");

            assertThat(cammyIndex).isNotEqualTo(jerryIndex);
        }
    }

    @Nested
    @DisplayName("Password hashing")
    class PasswordHashing
    {
        @Test
        @DisplayName("Password is hashed, not stored in plaintext")
        void passwordIsHashed()
        {
            EventHost host = createHost("darla@respectableDomain.net");
            eventHostRepository.save(host);

            EventHost loaded = eventHostRepository.findById(host.getId()).orElseThrow();

            // The stored hash should NOT be the plaintext password.
            assertThat(loaded.getPasswordHash())
                .isNotEqualTo("blahBlah123")
                .startsWith("$2a$"); // bcrypt format indicator
        }

        @Test
        @DisplayName("Password verification works after round-trip")
        void passwordVerification()
        {
            EventHost host = createHost("steve@yourNeighbor.com");
            eventHostRepository.save(host);

            EventHost loaded = eventHostRepository.findById(host.getId()).orElseThrow();

            boolean correctPassword = hashingService.verifyPassword("blahBlah123", loaded.getPasswordHash());
            boolean wrongPassword = hashingService.verifyPassword("Wrongamundo, bub!", loaded.getPasswordHash());

            assertThat(correctPassword).isTrue();
            assertThat(wrongPassword).isFalse();
        }
    }
}