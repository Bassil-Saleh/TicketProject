package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GetEventsResponse
(
    @NotNull(message = "List of retrieved events can be empty, but not null")
    List<GetEventByPublicIdResponse> events,

    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
