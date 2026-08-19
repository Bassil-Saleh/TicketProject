package com.ticketproject.webapp.dtos.responses;

import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GetEventByPublicIdResponse specifies what a response to a successful
 * request to retrieve info on an Event entity should look like.
 * 
 * @param publicId the event's public ID
 * @param name the event's name
 * @param description the event's description
 * @param startDateTime the event's start date and time
 * @param endDateTime the event's end date and time
 * @param eventType the event's type
 * @param eventStatus the event's status
 * @param numberOfRegisteredAttendees the current number of attendees registered for the event
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
@Schema(description = "Response body containing detailed information about a single event")
public record GetEventByPublicIdResponse
(
    @Schema
    (
        description = "The unique public identifier of the event",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    @NotNull(message = "Event public id cannot be null")
    String publicId,

    @Schema
    (
        description = "The name of the event",
        example = "Annual Tech Conference 2026"
    )
    @NotBlank(message = "Event name cannot be blank")
    String name,

    @Schema
    (
        description = "A detailed description of the event",
        example = "A full-day conference featuring talks on the latest technology trends."
    )
    @NotBlank(message = "Event description cannot be blank")
    String description,

    @Schema
    (
        description = "The event's start date and time",
        example = "2026-09-15T09:00:00"
    )
    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @Schema
    (
        description = "The event's end date and time",
        example = "2026-09-15T18:00:00"
    )
    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime,

    @Schema
    (
        description = "The type of the event: PUBLIC (open registration) or PRIVATE (invitation only)",
        example = "PUBLIC"
    )
    @NotNull(message = "Event type cannot be null")
    EventType eventType,

    @Schema
    (
        description = "The status of the event: DRAFT (not published yet), PUBLISHED (already published), or CANCELED (canceled by event host).",
        example = "PUBLISHED"
    )
    @NotNull(message = "Event status cannot be null")
    EventStatus eventStatus,

    @Schema
    (
        description = "The current number of attendees who are registered under the event (either via public registrations or private invitations).",
        example = "12"
    )
    @NotNull(message = "Number of attendees cannot be null")
    Long numberOfRegisteredAttendees,

    @Schema
    (
        description = "The maximum number of attendees allowed to register for the event",
        example = "100"
    )
    Integer maxAttendees,

    @Schema
    (
        description = "The first line of the event's street address",
        example = "123 Main Street"
    )
    @NotBlank(message = "Event 1st address line cannot be blank")
    String addressLine1,

    @Schema
    (
        description = "The second line of the event's street address (may be null)",
        example = "Suite 400"
    )
    String addressLine2,

    @Schema
    (
        description = "The city where the event takes place",
        example = "San Francisco"
    )
    @NotBlank(message = "Event city cannot be blank")
    String city,

    @Schema
    (
        description = "The state or province where the event takes place",
        example = "California"
    )
    @NotBlank(message = "Event state cannot be blank")
    String state,

    @Schema
    (
        description = "The postal or ZIP code of the event's location",
        example = "94105"
    )
    @NotBlank(message = "Event postal code cannot be blank")
    String postalCode,

    @Schema
    (
        description = "The country where the event takes place",
        example = "United States"
    )
    @NotBlank(message = "Event country cannot be blank")
    String country,

    @Schema
    (
        description = "The latitude coordinate of the event's precise location (may be null)",
        example = "37.7749295"
    )
    BigDecimal latitude,

    @Schema
    (
        description = "The longitude coordinate of the event's precise location (may be null)",
        example = "-122.4194155"
    )
    BigDecimal longitude
)
{
}