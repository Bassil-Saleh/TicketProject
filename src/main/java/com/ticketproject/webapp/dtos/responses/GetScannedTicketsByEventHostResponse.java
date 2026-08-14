package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * GetScannedTicketsByEventHostResponse specifies what a response to
 * a successful request to retrieve info on a list of tickets scanned
 * by a logged in event host should look like.
 * @param scannedTickets a list of records containing info on tickets
 * scanned by the logged in event host
 */
@Schema(description = "Response body containing a list of tickets scanned by the authenticated event host")
public record GetScannedTicketsByEventHostResponse
(
    @Schema
    (
        description = "A list of records containing information about each scanned ticket",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<ScannedTicketInfo> scannedTickets
)
{
}