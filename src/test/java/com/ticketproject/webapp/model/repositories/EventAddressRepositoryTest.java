package com.ticketproject.webapp.model.repositories;

import java.math.BigDecimal;
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
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.CryptoService;
import com.ticketproject.webapp.services.HashingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({SpringContextBridge.class, CryptoService.class, BlindIndexService.class, HashingService.class})
@ActiveProfiles("test")
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
    @DisplayName("Data range constraints")
    class DataRangeConstraints
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
        @DisplayName("Passing non-null latitude and null longitude (or vice versa) throws exception")
        void nullAndNonNullThrowsException()
        {
            // Null latitude, non-null longitude
            assertThatThrownBy(() -> createEventAddress(null, new BigDecimal("3.141592653")))
            .isInstanceOf(IllegalArgumentException.class);
            // Non-null latitude, null longitude
            assertThatThrownBy(() -> createEventAddress(new BigDecimal("3.141592653"), null))
            .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
