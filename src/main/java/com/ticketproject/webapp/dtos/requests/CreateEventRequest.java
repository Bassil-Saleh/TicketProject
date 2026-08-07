package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * CreateEventRequest specifies what a valid request to
 * create a new event should look like.
 * 
 * @param name the event's name
 * @param description the event's description
 * @param startDateTime the event's start date and time
 * @param endDateTime the event's end date and time
 * @param eventType the event's type
 * @param maxAttendees the maximum number of attendees that can register for the event
 * @param addressLine1 the event's 1st address line
 * @param addressLine2 the event's 2nd address line
 * @param city the event's city
 * @param state the event's state
 * @param postalCode the event's postal code
 * @param country the event's country
 * @param latitude the latitude coordinates of the event's precise location
 * @param longitude the longitude coordinates of the event's precise location
 */
public record CreateEventRequest
(
    @NotBlank(message = "Event name cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.NAME_LENGTH,
        message = "Event name cannot be longer than " +
        AppConstants.Database.Events.Sizes.NAME_LENGTH +
        " characters"
    )
    String name,

    @NotBlank(message = "Event description cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH,
        message = "Event description cannot be longer than " +
        AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH +
        " characters"
    )
    String description,

    @Future(message = "Event start date and time must be in the future")
    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @Future(message = "Event end date and time must be in the future")
    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime,

    @NotNull(message = "Event type cannot be null")
    EventType eventType,

    @Min
    (
        value = AppConstants.Database.Events.Sizes.MIN_ATTENDEES,
        message = "Max # of attendees cannot be less than 1"
    )
    Integer maxAttendees,

    @NotBlank(message = "Event 1st address line cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine1,

    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine2,

    @NotBlank(message = "Event city cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.CITY_LENGTH,
        message = "Event city cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.CITY_LENGTH +
        " characters"
    )
    String city,

    @NotBlank(message = "Event state cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.STATE_LENGTH,
        message = "Event state cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.STATE_LENGTH +
        " characters"
    )
    String state,

    @NotBlank(message = "Event postal code cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.POSTAL_CODE_LENGTH,
        message = "Event postal code cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.POSTAL_CODE_LENGTH +
        " characters"
    )
    String postalCode,

    @NotBlank(message = "Event country cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.COUNTRY_LENGTH,
        message = "Event country cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.COUNTRY_LENGTH +
        " characters"
    )
    String country,

    // TODO: figure out how to add constraints for BigDecimal scale and precision here.

    BigDecimal latitude,

    BigDecimal longitude
)
{
}
