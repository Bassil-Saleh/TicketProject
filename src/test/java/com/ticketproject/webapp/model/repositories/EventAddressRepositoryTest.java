package com.ticketproject.webapp.model.repositories;

import java.math.BigDecimal;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EventAddressRepositoryTest contains integration tests for the
 * EventAddress entity and EventAddressRepository, covering data
 * constraints, data integrity, rollback on failure, commit atomicity,
 * and isolation.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventAddressRepositoryTest
{
    @Autowired
    private EventAddressRepository eventAddressRepository;

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
     * Helper method to create EventAddress entities.
     * @param latitude BigDecimal object representing latitude
     * @param longitude BigDecimal object representing longitude
     * @return a new EventAddress object
     */
    private EventAddress createEventAddress(BigDecimal latitude, BigDecimal longitude)
    {
        EventAddress address = new EventAddress.Builder()
            .addressLine1("123 Stone Rd")
            .addressLine2("Floor 2, Suite #62")
            .city("Astoria")
            .state("NY")
            .postalCode("11102")
            .country("USA")
            .latitude(latitude)
            .longitude(longitude)
            .build();
        return address;
    }

    @Nested
    @DisplayName("Data constraints")
    class DataConstraints
    {
        @Test
        @DisplayName("Incorrect precision/scale for latitude and longitude throws exception")
        void incorrectLatLongThrowsException()
        {
            // Correct precision, wrong scale
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("31.41592653"), new BigDecimal("314.1592653")))
            .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("314.1592653"), new BigDecimal("31.41592653")))
            .isInstanceOf(IllegalArgumentException.class);

            // Wrong precision, correct scale
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("31415.9265358"), new BigDecimal("314.1592653")))
            .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("314.1592653"), new BigDecimal("31415.9265358")))
            .isInstanceOf(IllegalArgumentException.class);

            // Wrong precision, wrong scale
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("3.14159265358979"), new BigDecimal("3.141592653")))
            .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("3.141592653"), new BigDecimal("3.14159265358979")))
            .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Passing non-null latitude and null longitude (or vice versa) to constructor throws exception")
        void nullAndNonNullConstructionThrowsException()
        {
            // Null latitude, non-null longitude
            assertThatThrownBy(() -> createEventAddress(null, new BigDecimal("3.141592653")))
            .isInstanceOf(IllegalArgumentException.class);
            // Non-null latitude, null longitude
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("3.141592653"), null))
            .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Writing a record with non-null latitude & longitude and then updating one of them to null throws exception")
        void nullAndNonNullUpdateThrowsException()
        {
            EventAddress address1 = createEventAddress(new BigDecimal("3.141592"), new BigDecimal("3.141592"));
            eventAddressRepository.saveAndFlush(address1);
            EventAddress address2 = createEventAddress(new BigDecimal("3.141592"), new BigDecimal("3.141592"));
            eventAddressRepository.saveAndFlush(address2);

            Optional<EventAddress> loaded1 = eventAddressRepository.findById(address1.getId());
            Optional<EventAddress> loaded2 = eventAddressRepository.findById(address2.getId());

            loaded1.get().setLatitude(null);
            loaded2.get().setLongitude(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(loaded1.get()))
            .isInstanceOf(InvalidDataAccessApiUsageException.class);
            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(loaded2.get()))
            .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        @DisplayName("Update a record with both null latitude & longitude to non-null latitude & longitude")
        void updateNullToNonNull()
        {
            EventAddress address = createEventAddress(null, null);
            eventAddressRepository.saveAndFlush(address);

            Optional<EventAddress> loaded = eventAddressRepository.findById(address.getId());

            loaded.get().setLatitude(new BigDecimal("3.141592"));
            loaded.get().setLongitude(new BigDecimal("3.141592"));

            assertThatNoException().isThrownBy(() -> eventAddressRepository.saveAndFlush(loaded.get()));;
        }

        @Test
        @DisplayName("Update a record with both non-null latitude & longitude to null latitude & longitude")
        void updateNonNullToNull()
        {
            EventAddress address = createEventAddress(new BigDecimal("3.141592"), new BigDecimal("3.141592"));
            eventAddressRepository.saveAndFlush(address);

            Optional<EventAddress> loaded = eventAddressRepository.findById(address.getId());

            loaded.get().setLatitude(null);
            loaded.get().setLongitude(null);

            assertThatNoException().isThrownBy(() -> eventAddressRepository.saveAndFlush(loaded.get()));
        }

        @Test
        @DisplayName("Saving address with null addressLine1 violates NOT NULL constraint")
        void nullAddressLine1ThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            address.setAddressLine1(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(address))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving address with null city violates NOT NULL constraint")
        void nullCityThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            address.setCity(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(address))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving address with null state violates NOT NULL constraint")
        void nullStateThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            address.setState(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(address))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving address with null postalCode violates NOT NULL constraint")
        void nullPostalCodeThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            address.setPostalCode(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(address))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving address with null country violates NOT NULL constraint")
        void nullCountryThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            address.setCountry(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(address))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Saving address with null addressLine2 is allowed (optional field)")
        void nullAddressLine2IsAllowed()
        {
            EventAddress address = createEventAddress(null, null);
            address.setAddressLine2(null);

            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAddressLine2()).isNull();
        }

        @Test
        @DisplayName("Updating address with null addressLine1 violates NOT NULL constraint")
        void updateNullAddressLine1ThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            saved.setAddressLine1(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating address with null city violates NOT NULL constraint")
        void updateNullCityThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            saved.setCity(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating address with null state violates NOT NULL constraint")
        void updateNullStateThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            saved.setState(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating address with null postalCode violates NOT NULL constraint")
        void updateNullPostalCodeThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            saved.setPostalCode(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating address with null country violates NOT NULL constraint")
        void updateNullCountryThrowsException()
        {
            EventAddress address = createEventAddress(null, null);
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            saved.setCountry(null);

            assertThatThrownBy(() -> eventAddressRepository.saveAndFlush(saved))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Updating address with null addressLine2 is allowed (optional field)")
        void updateNullAddressLine2IsAllowed()
        {
            EventAddress address = createEventAddress(null, null);
            address.setAddressLine2("Suite 100");
            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            assertThat(saved.getAddressLine2()).isEqualTo("Suite 100");
            saved.setAddressLine2(null);

            EventAddress updated = eventAddressRepository.saveAndFlush(saved);
            assertThat(updated).isNotNull();
            assertThat(updated.getAddressLine2()).isNull();
        }
    }

    @Nested
    @DisplayName("Data integrity")
    class DataIntegrity
    {
        @Test
        @DisplayName("Saved address has valid fields after round-trip")
        void savedAddressHasValidFields()
        {
            EventAddress address = new EventAddress.Builder()
                .addressLine1("456 Oak Ave")
                .addressLine2("Apt 7B")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62565")
                .country("USA")
                .latitude(new BigDecimal("39.4062"))
                .longitude(new BigDecimal("-89.6501"))
                .build();

            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            EventAddress loaded = eventAddressRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getAddressLine1()).isEqualTo("456 Oak Ave");
            assertThat(loaded.getAddressLine2()).isEqualTo("Apt 7B");
            assertThat(loaded.getCity()).isEqualTo("Shelbyville");
            assertThat(loaded.getState()).isEqualTo("IL");
            assertThat(loaded.getPostalCode()).isEqualTo("62565");
            assertThat(loaded.getCountry()).isEqualTo("USA");
            assertThat(loaded.getLatitude()).isEqualByComparingTo(new BigDecimal("39.4062"));
            assertThat(loaded.getLongitude()).isEqualByComparingTo(new BigDecimal("-89.6501"));
            assertThat(loaded.getCreated()).isNotNull();
            assertThat(loaded.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Saved address without optional fields has valid fields after round-trip")
        void savedAddressWithoutOptionalFields()
        {
            EventAddress address = new EventAddress.Builder()
                .addressLine1("789 Elm St")
                .city("Capital City")
                .state("CA")
                .postalCode("90001")
                .country("USA")
                .build();

            EventAddress saved = eventAddressRepository.saveAndFlush(address);
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();

            EventAddress loaded = eventAddressRepository.findById(saved.getId()).orElseThrow();
            assertThat(loaded).isNotNull();
            assertThat(loaded.getAddressLine1()).isEqualTo("789 Elm St");
            assertThat(loaded.getAddressLine2()).isNull();
            assertThat(loaded.getLatitude()).isNull();
            assertThat(loaded.getLongitude()).isNull();
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
        @DisplayName("Failed address save does not persist the address")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void failedSaveDoesNotPersist()
        {
            long countBefore = eventAddressRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                EventAddress address = createEventAddress(null, null);
                address.setAddressLine1(null);
                eventAddressRepository.saveAndFlush(address);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(eventAddressRepository.count()).isEqualTo(countBefore);
        }

        @Test
        @DisplayName("Constraint violation rolls back prior save in same transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void constraintViolationRollsBackPriorSave()
        {
            long countBefore = eventAddressRepository.count();

            assertThatThrownBy(() -> txTemplate.execute(status ->
            {
                EventAddress address1 = createEventAddress(null, null);
                address1 = eventAddressRepository.saveAndFlush(address1);
                assertThat(address1).isNotNull();
                assertThat(address1.getId()).isNotNull();
                assertThat(eventAddressRepository.findById(address1.getId())).isPresent();

                // Now try to save a second address that will force a NOT NULL violation
                EventAddress address2 = createEventAddress(null, null);
                address2.setCity(null);
                eventAddressRepository.saveAndFlush(address2);
                return null;
            })).isInstanceOf(DataIntegrityViolationException.class);

            assertThat(eventAddressRepository.count()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("Commit atomicity")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    class CommitAtomicity
    {
        @Test
        @DisplayName("Successful address save commits atomically")
        void successfulSaveCommitsAtomically()
        {
            Long addressId = txTemplate.execute(status ->
            {
                EventAddress address = createEventAddress(null, null);
                EventAddress saved = eventAddressRepository.saveAndFlush(address);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            assertThat(eventAddressRepository.findById(addressId)).isPresent();
        }
    }

    @Nested
    @DisplayName("Isolation")
    class Isolation
    {
        @Test
        @DisplayName("Rolled back address is not visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rolledBackAddressNotVisible()
        {
            Long addressId = txTemplate.execute(status ->
            {
                EventAddress address = createEventAddress(null, null);
                EventAddress saved = eventAddressRepository.saveAndFlush(address);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();

                Long id = saved.getId();

                assertThat(eventAddressRepository.findById(id)).isPresent();

                // Flag the transaction for rollback
                status.setRollbackOnly();
                return id;
            });

            assertThat(eventAddressRepository.findById(addressId)).isEmpty();
        }

        @Test
        @DisplayName("Committed address is visible in subsequent transaction")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void committedAddressVisible()
        {
            Long addressId = txTemplate.execute(status ->
            {
                EventAddress address = createEventAddress(null, null);
                EventAddress saved = eventAddressRepository.saveAndFlush(address);
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                return saved.getId();
            });

            Optional<EventAddress> loaded = eventAddressRepository.findById(addressId);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getAddressLine1()).isEqualTo("123 Stone Rd");
        }
    }
}
