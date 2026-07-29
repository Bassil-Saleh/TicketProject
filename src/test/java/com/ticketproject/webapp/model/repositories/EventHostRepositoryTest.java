package com.ticketproject.webapp.model.repositories;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventHostRepositoryTest contains integration tests for the
 * EventHost entity and EventHostRepository, covering encryption
 * round-trips, blind index queries, password hashing, uniqueness
 * constraints, data constraints, data integrity, rollback on failure,
 * commit atomicity, and isolation.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventHostRepositoryTest
{
    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private BlindIndexService blindIndexService;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    private TransactionTemplate txTemplate;

    /**
     * Initializes the TransactionTemplate used for programmatic
     * transaction management in rollback, isolation, and atomicity tests.
     */
    @BeforeEach
    void setUp()
    {
        txTemplate = new TransactionTemplate(transactionManager);
    }

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
            .plaintextPassword("blahBlah123")
            .build();

            host.generateVerificationToken();

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

    @Nested
    @DisplayName("Uniqueness constraint")
    class UniquenessConstraint
    {
        @Test
        @DisplayName("Duplicate email blind index violates unique constraint")
        void duplicateBlindIndexThrowsException()
        {
            EventHost host1 = createHost("hugo@yourBank.com");
            eventHostRepository.saveAndFlush(host1);
            EventHost host2 = createHost("hugo@yourBank.com");

            // This should throw an exception because
            // emailBlindIndex has a UNIQUE constraint.
            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(host2))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving host with null firstName violates NOT NULL constraint")
        void nullFirstNameThrowsException()
        {
            EventHost host = createHost("nullFirst@example.com");
            host.setFirstName(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(host))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving host with null lastName violates NOT NULL constraint")
        void nullLastNameThrowsException()
        {
            EventHost host = createHost("nullLast@example.com");
            host.setLastName(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(host))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving host with null dateOfBirth violates NOT NULL constraint")
        void nullDateOfBirthThrowsException()
        {
            EventHost host = createHost("nullDob@example.com");
            ReflectionTestUtils.setField(host, "dateOfBirth", null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(host))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving host with null email violates NOT NULL constraint")
        void nullEmailThrowsException()
        {
            EventHost host = createHost("temp@example.com");
            host.setEmail(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(host))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving host with null middleName is allowed (optional field)")
        void nullMiddleNameIsAllowed()
        {
            EventHost host = createHost("noMiddle@example.com");
            host.setMiddleName(null);

            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMiddleName()).isNull();
        }

        @Test
        @DisplayName("Updating host with null firstName violates NOT NULL constraint")
        void updateNullFirstNameThrowsException()
        {
            EventHost host = createHost("updateFirst@example.com");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            saved.setFirstName(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating host with null lastName violates NOT NULL constraint")
        void updateNullLastNameThrowsException()
        {
            EventHost host = createHost("updateLast@example.com");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            saved.setLastName(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating host with null dateOfBirth violates NOT NULL constraint")
        void updateNullDateOfBirthThrowsException()
        {
            EventHost host = createHost("updateDob@example.com");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            ReflectionTestUtils.setField(saved, "dateOfBirth", null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating host with null email violates NOT NULL constraint")
        void updateNullEmailThrowsException()
        {
            EventHost host = createHost("updateEmail@example.com");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            saved.setEmail(null);

            assertThatThrownBy(() -> eventHostRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating host with null middleName is allowed (optional field)")
        void updateNullMiddleNameIsAllowed()
        {
            EventHost host = createHost("updateMiddle@example.com");
            host.setMiddleName("William");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            assertThat(saved.getMiddleName()).isEqualTo("William");
            saved.setMiddleName(null);

            EventHost updated = eventHostRepository.saveAndFlush(saved);
            assertThat(updated).isNotNull();
            assertThat(updated.getMiddleName()).isNull();
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Saved host has valid fields after round-trip")
        void savedHostHasValidFields()
        {
            EventHost host = new EventHost.Builder()
                .firstName("Jane")
                .middleName("Elizabeth")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .email("jane.smith@example.com")
                .plaintextPassword("securePass456")
                .build();
            host.generateVerificationToken();

            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            EventHost loaded = eventHostRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getFirstName()).isEqualTo("Jane");
            assertThat(loaded.getMiddleName()).isEqualTo("Elizabeth");
            assertThat(loaded.getLastName()).isEqualTo("Smith");
            assertThat(loaded.getEmail()).isEqualTo("jane.smith@example.com");
            assertThat(loaded.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 6, 15));
            assertThat(loaded.getPasswordHash()).isNotNull();
            assertThat(loaded.getCreated()).isNotNull();
            assertThat(loaded.getLastLogin()).isNotNull();
            assertThat(loaded.getLastUpdated()).isNotNull();
            assertThat(loaded.isActive()).isTrue();
            assertThat(loaded.isVerified()).isFalse();
            assertThat(loaded.getVerificationKeyHash()).isNotNull();
            assertThat(loaded.getVerificationExpires()).isNotNull();
            assertThat(loaded.getEmailBlindIndex()).isNotNull();
        }

        @Test
        @DisplayName("Host active and verified flags can be toggled")
        void hostFlagsCanBeToggled()
        {
            EventHost host = createHost("toggle@example.com");
            EventHost saved = eventHostRepository.saveAndFlush(host);
            assertThat(saved).isNotNull();
            assertThat(saved.isActive()).isTrue();
            assertThat(saved.isVerified()).isFalse();

            saved.setActive(false);
            saved.setVerified(true);
            saved = eventHostRepository.saveAndFlush(saved);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            EventHost loaded = eventHostRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.isActive()).isFalse();
            assertThat(loaded.isVerified()).isTrue();
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
        @DisplayName("Failed host save does not persist the host")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void failedSaveDoesNotPersist()
        {
            long countBefore = eventHostRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                EventHost host = createHost("rollback@example.com");
                host.setFirstName(null);
                eventHostRepository.saveAndFlush(host);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(eventHostRepository.count()).isEqualTo(countBefore);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void constraintViolationRollsBackPriorSave()
        {
            long countBefore = eventHostRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                EventHost host1 = createHost("first@example.com");
                host1 = eventHostRepository.saveAndFlush(host1);
                assertThat(host1).isNotNull();
                assertThat(host1.getId()).isNotNull();
                assertThat(eventHostRepository.findById(host1.getId())).isPresent();

                // Now try to save a second host that will force a NOT NULL violation
                EventHost host2 = createHost("second@example.com");
                host2.setLastName(null);
                eventHostRepository.saveAndFlush(host2);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(eventHostRepository.count()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful host save commits atomically")
        void successfulSaveCommitsAtomically()
        {
            Long hostId = txTemplate.execute(status ->
            {
                EventHost host = createHost("atomic@example.com");
                EventHost saved = eventHostRepository.saveAndFlush(host);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            assertThat(eventHostRepository.findById(hostId)).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back host is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rolledBackHostNotVisible()
        {
            Long hostId = txTemplate.execute(status ->
            {
                EventHost host = createHost("isolation@example.com");
                EventHost saved = eventHostRepository.saveAndFlush(host);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                Long id = saved.getId();

                assertThat(eventHostRepository.findById(id)).isPresent();

                // Flag the transaction for rollback
                status.setRollbackOnly();
                return id;
            });

            assertThat(eventHostRepository.findById(hostId)).isEmpty();
        }

        @Test
        @DisplayName("Committed host is visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void committedHostVisible()
        {
            Long hostId = txTemplate.execute(status ->
            {
                EventHost host = createHost("committed@example.com");
                EventHost saved = eventHostRepository.saveAndFlush(host);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            Optional<EventHost> loaded = eventHostRepository.findById(hostId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getEmail()).isEqualTo("committed@example.com");
        }
    }
}
