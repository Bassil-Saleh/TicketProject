package com.ticketproject.webapp.dtos.responses;

import java.util.List;

public record GetScannedTicketsByEventHostResponse
(
    List<ScannedTicketInfo> scannedTickets
)
{
}

