package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * ScannedTicketInfo specifies what a single record of info on a ticket
 * scanned by an event host should look like. Used when retrieving info
 * on a list of tickets scanned by a logged in event host.
 * @param scannedAt when the ticket was scanned
 * @param attendeeFirstName the attendee's first name
 * @param attendeeMiddleName the attendee's middle name (optional)
 * @param attendeeLastName the attendee's last name
 * @param attendeeEmail the attendee's email address
 * @param eventName the event's name
 * @param eventDescription the event's description
 * @param eventStartDateTime the event's start date and time
 * @param eventEndDateTime the event's end date and time
 */
@Schema(description = "Information about a single ticket scanned by an event host")
public record ScannedTicketInfo
(
    @Schema
    (
        description = "The date and time when the ticket was scanned",
        example = "2026-09-15T09:15:30"
    )
    LocalDateTime scannedAt,

    @Schema
    (
        description = "The attendee's first name",
        example = "Jane"
    )
    String attendeeFirstName,

    @Schema
    (
        description = "The attendee's middle name (may be null)",
        example = "Marie"
    )
    String attendeeMiddleName,

    @Schema
    (
        description = "The attendee's last name",
        example = "Smith"
    )
    String attendeeLastName,

    @Schema
    (
        description = "The attendee's email address",
        example = "jane.smith@example.com"
    )
    String attendeeEmail,

    @Schema
    (
        description = "The name of the event the ticket is for",
        example = "Annual Tech Conference 2026"
    )
    String eventName,

    @Schema
    (
        description = "The description of the event the ticket is for",
        example = "A full-day conference featuring talks on the latest technology trends."
    )
    String eventDescription,

    @Schema
    (
        description = "The event's start date and time",
        example = "2026-09-15T09:00:00"
    )
    LocalDateTime eventStartDateTime,

    @Schema
    (
        description = "The event's end date and time",
        example = "2026-09-15T18:00:00"
    )
    LocalDateTime eventEndDateTime
)
{
}