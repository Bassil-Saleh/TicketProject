package com.ticketproject.webapp.dtos.responses;

import com.ticketproject.webapp.model.enums.EventType;

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
public record GetEventByPublicIdResponse
(
    @NotNull(message = "Event public id cannot be null")
    String publicId,

    @NotBlank(message = "Event name cannot be blank")
    String name,

    @NotBlank(message = "Event description cannot be blank")
    String description,

    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime,

    @NotNull(message = "Event type cannot be null")
    EventType eventType,

    Integer maxAttendees,

    @NotBlank(message = "Event 1st address line cannot be blank")
    String addressLine1,

    String addressLine2,

    @NotBlank(message = "Event city cannot be blank")
    String city,

    @NotBlank(message = "Event state cannot be blank")
    String state,

    @NotBlank(message = "Event postal code cannot be blank")
    String postalCode,

    @NotBlank(message = "Event country cannot be blank")
    String country,

    BigDecimal latitude,

    BigDecimal longitude
)
{
}