package com.ticketproject.webapp.dtos.responses;

import java.util.List;

/**
 * GetScannedTicketsByEventHostResponse specifies what a response to
 * a successful request to retrieve info on a list of tickets scanned
 * by a logged in event host should look like.
 * @param scannedTickets a list of records containing info on tickets
 * scanned by the logged in event host
 */
public record GetScannedTicketsByEventHostResponse
(
    List<ScannedTicketInfo> scannedTickets
)
{
}

