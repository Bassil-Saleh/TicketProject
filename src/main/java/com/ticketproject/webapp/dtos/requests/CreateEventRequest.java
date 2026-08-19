package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.EventType;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Request body for creating a new event")
public record CreateEventRequest
(
    @Schema
    (
        description = "The name of the event",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Annual Tech Conference 2026",
        maximum = "255"
    )
    @NotBlank(message = "Event name cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.NAME_LENGTH,
        message = "Event name cannot be longer than " +
        AppConstants.Database.Events.Sizes.NAME_LENGTH +
        " characters"
    )
    String name,

    @Schema
    (
        description = "A detailed description of the event",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "A full-day conference featuring talks on the latest technology trends.",
        maximum = "5000"
    )
    @NotBlank(message = "Event description cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH,
        message = "Event description cannot be longer than " +
        AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH +
        " characters"
    )
    String description,

    @Schema
    (
        description = "The event's start date and time, must be in the future",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-09-15T09:00:00"
    )
    @Future(message = "Event start date and time must be in the future")
    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @Schema
    (
        description = "The event's end date and time, must be in the future and after the start time",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-09-15T18:00:00"
    )
    @Future(message = "Event end date and time must be in the future")
    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime,

    @Schema
    (
        description = "The type of the event: PUBLIC (open registration) or PRIVATE (invitation only)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "PUBLIC"
    )
    @NotNull(message = "Event type cannot be null")
    EventType eventType,

    @Schema
    (
        description = "The maximum number of attendees allowed to register for the event",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "100",
        minimum = "1"
    )
    @Min
    (
        value = AppConstants.Database.Events.Sizes.MIN_ATTENDEES,
        message = "Max # of attendees cannot be less than 1"
    )
    Long maxAttendees,

    @Schema
    (
        description = "The first line of the event's street address",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "123 Main Street",
        maximum = "255"
    )
    @NotBlank(message = "Event 1st address line cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine1,

    @Schema
    (
        description = "The second line of the event's street address (optional, e.g., suite or apartment number)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Suite 400",
        maximum = "255"
    )
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine2,

    @Schema
    (
        description = "The city where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "San Francisco",
        maximum = "100"
    )
    @NotBlank(message = "Event city cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.CITY_LENGTH,
        message = "Event city cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.CITY_LENGTH +
        " characters"
    )
    String city,

    @Schema
    (
        description = "The state or province where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "California",
        maximum = "100"
    )
    @NotBlank(message = "Event state cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.STATE_LENGTH,
        message = "Event state cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.STATE_LENGTH +
        " characters"
    )
    String state,

    @Schema
    (
        description = "The postal or ZIP code of the event's location",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "94105",
        maximum = "20"
    )
    @NotBlank(message = "Event postal code cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.POSTAL_CODE_LENGTH,
        message = "Event postal code cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.POSTAL_CODE_LENGTH +
        " characters"
    )
    String postalCode,

    @Schema
    (
        description = "The country where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "United States",
        maximum = "100"
    )
    @NotBlank(message = "Event country cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.COUNTRY_LENGTH,
        message = "Event country cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.COUNTRY_LENGTH +
        " characters"
    )
    String country,

    // TODO: figure out how to add constraints for BigDecimal scale and precision here.

    @Schema
    (
        description = "The latitude coordinate of the event's precise location (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "37.7749295"
    )
    BigDecimal latitude,

    @Schema
    (
        description = "The longitude coordinate of the event's precise location (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "-122.4194155"
    )
    BigDecimal longitude
)
{
}