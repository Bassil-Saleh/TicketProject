package com.ticketproject.webapp.dtos.responses;

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
public record ScannedTicketInfo
(
    LocalDateTime scannedAt,
    String attendeeFirstName,
    String attendeeMiddleName,
    String attendeeLastName,
    String attendeeEmail,
    String eventName,
    String eventDescription,
    LocalDateTime eventStartDateTime,
    LocalDateTime eventEndDateTime
)
{
}