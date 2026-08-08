package com.ticketproject.webapp.dtos.responses;

import java.time.LocalDateTime;

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