package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * GetEventsResponse specifies what a response to a successful request
 * to retrieve info on multiple events created by the logged in
 * event host should look like.
 * 
 * @param events list of records of info on each event
 * @param message the detail message
 */
public record GetEventsResponse
(
    @NotNull(message = "List of retrieved events can be empty, but not null")
    List<GetEventByPublicIdResponse> events,

    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
