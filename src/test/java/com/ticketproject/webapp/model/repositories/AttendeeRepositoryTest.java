package com.ticketproject.webapp.model.repositories;

import java.sql.Connection;
import java.sql.SQLException;
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

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.services.database.BlindIndexService;
import com.ticketproject.webapp.services.database.CryptoService;
import com.ticketproject.webapp.services.database.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AttendeeRepositoryTest contains integration tests for the
 * Attendee entity and AttendeeRepository, covering encryption
 * round-trips, blind index queries, uniqueness constraints,
 * data constraints, data integrity, rollback on failure,
 * commit atomicity, and isolation.
 */
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

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Saving attendee with null firstName violates NOT NULL constraint")
        void nullFirstNameThrowsException()
        {
            Attendee attendee = createAttendee("nullFirst@example.com");
            attendee.setFirstName(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(attendee))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving attendee with null lastName violates NOT NULL constraint")
        void nullLastNameThrowsException()
        {
            Attendee attendee = createAttendee("nullLast@example.com");
            attendee.setLastName(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(attendee))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving attendee with null email violates NOT NULL constraint")
        void nullEmailThrowsException()
        {
            Attendee attendee = createAttendee("temp@example.com");
            attendee.setEmail(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(attendee))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving attendee with null middleName is allowed (optional field)")
        void nullMiddleNameIsAllowed()
        {
            Attendee attendee = createAttendee("noMiddle@example.com");
            attendee.setMiddleName(null);

            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMiddleName()).isNull();
        }

        @Test
        @DisplayName("Updating attendee with null firstName violates NOT NULL constraint")
        void updateNullFirstNameThrowsException()
        {
            Attendee attendee = createAttendee("updateFirst@example.com");
            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            saved.setFirstName(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating attendee with null lastName violates NOT NULL constraint")
        void updateNullLastNameThrowsException()
        {
            Attendee attendee = createAttendee("updateLast@example.com");
            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            saved.setLastName(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating attendee with null email violates NOT NULL constraint")
        void updateNullEmailThrowsException()
        {
            Attendee attendee = createAttendee("updateEmail@example.com");
            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            saved.setEmail(null);

            assertThatThrownBy(() -> attendeeRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating attendee with null middleName is allowed (optional field)")
        void updateNullMiddleNameIsAllowed()
        {
            Attendee attendee = createAttendee("updateMiddle@example.com");
            attendee.setMiddleName("Grace");
            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            assertThat(saved.getMiddleName()).isEqualTo("Grace");
            saved.setMiddleName(null);

            Attendee updated = attendeeRepository.saveAndFlush(saved);
            assertThat(updated).isNotNull();
            assertThat(updated.getMiddleName()).isNull();
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Saved attendee has valid fields after round-trip")
        void savedAttendeeHasValidFields()
        {
            Attendee attendee = new Attendee.Builder()
                .firstName("Diana")
                .middleName("Marie")
                .lastName("Johnson")
                .email("diana.johnson@example.com")
                .build();

            Attendee saved = attendeeRepository.saveAndFlush(attendee);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            Attendee loaded = attendeeRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getFirstName()).isEqualTo("Diana");
            assertThat(loaded.getMiddleName()).isEqualTo("Marie");
            assertThat(loaded.getLastName()).isEqualTo("Johnson");
            assertThat(loaded.getEmail()).isEqualTo("diana.johnson@example.com");
            assertThat(loaded.getCreated()).isNotNull();
            assertThat(loaded.getEmailBlindIndex()).isNotNull();
        }

        @Test
        @DisplayName("getFullName returns correct full name with middle name")
        void fullNameWithMiddleName()
        {
            Attendee attendee = new Attendee.Builder()
                .firstName("Diana")
                .middleName("Marie")
                .lastName("Johnson")
                .email("diana.full@example.com")
                .build();

            assertThat(attendee.getFullName()).isEqualTo("Diana Marie Johnson");
        }

        @Test
        @DisplayName("getFullName returns correct full name without middle name")
        void fullNameWithoutMiddleName()
        {
            Attendee attendee = new Attendee.Builder()
                .firstName("Diana")
                .lastName("Johnson")
                .email("diana.noMiddle@example.com")
                .build();

            assertThat(attendee.getFullName()).isEqualTo("Diana Johnson");
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
        @DisplayName("Failed attendee save does not persist the attendee")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void failedSaveDoesNotPersist()
        {
            long countBefore = attendeeRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Attendee attendee = createAttendee("rollback@example.com");
                attendee.setFirstName(null);
                attendeeRepository.saveAndFlush(attendee);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(attendeeRepository.count()).isEqualTo(countBefore);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void constraintViolationRollsBackPriorSave()
        {
            long countBefore = attendeeRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                Attendee attendee1 = createAttendee("first@example.com");
                attendee1 = attendeeRepository.saveAndFlush(attendee1);
                assertThat(attendee1).isNotNull();
                assertThat(attendee1.getId()).isNotNull();
                assertThat(attendeeRepository.findById(attendee1.getId())).isPresent();

                // Now try to save a second attendee that will force a NOT NULL violation
                Attendee attendee2 = createAttendee("second@example.com");
                attendee2.setLastName(null);
                attendeeRepository.saveAndFlush(attendee2);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(attendeeRepository.count()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful attendee save commits atomically")
        void successfulSaveCommitsAtomically()
        {
            Long attendeeId = txTemplate.execute(status ->
            {
                Attendee attendee = createAttendee("atomic@example.com");
                Attendee saved = attendeeRepository.saveAndFlush(attendee);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            assertThat(attendeeRepository.findById(attendeeId)).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back attendee is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rolledBackAttendeeNotVisible()
        {
            Long attendeeId = txTemplate.execute(status ->
            {
                Attendee attendee = createAttendee("isolation@example.com");
                Attendee saved = attendeeRepository.saveAndFlush(attendee);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                Long id = saved.getId();

                assertThat(attendeeRepository.findById(id)).isPresent();

                // Flag the transaction for rollback
                status.setRollbackOnly();
                return id;
            });

            assertThat(attendeeRepository.findById(attendeeId)).isEmpty();
        }

        @Test
        @DisplayName("Committed attendee is visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void committedAttendeeVisible()
        {
            Long attendeeId = txTemplate.execute(status ->
            {
                Attendee attendee = createAttendee("committed@example.com");
                Attendee saved = attendeeRepository.saveAndFlush(attendee);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            Optional<Attendee> loaded = attendeeRepository.findById(attendeeId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getEmail()).isEqualTo("committed@example.com");
        }
    }
}
