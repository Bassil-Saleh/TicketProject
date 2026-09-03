package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * GetTicketsByEventPublicIdResponse specifies what a response to
 * a successful request to retrieve info on multiple tickets for
 * a specific event should look like.
 * @param tickets the list of tickets for a specific event
 */
@Schema(description = "Response body containing a list of tickets for a specific event")
public record GetTicketsByEventPublicIdResponse
(
    @Schema
    (
        description = "A list of tickets for a specific event",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "List of retrieved tickets can be empty, but not null")
    List<EventTicketInfo> tickets
)
{
}
