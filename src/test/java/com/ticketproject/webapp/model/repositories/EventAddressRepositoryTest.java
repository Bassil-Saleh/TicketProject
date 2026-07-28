package com.ticketproject.webapp.model.repositories;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventAddressRepositoryTest
{
    @Autowired
    private EventAddressRepository eventAddressRepository;

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
    }
}
