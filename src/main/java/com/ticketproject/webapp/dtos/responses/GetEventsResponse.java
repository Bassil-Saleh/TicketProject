package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Response body containing a list of events created by the authenticated event host")
public record GetEventsResponse
(
    @Schema
    (
        description = "A list of events created by the authenticated event host",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "List of retrieved events can be empty, but not null")
    List<GetEventByPublicIdResponse> events,

    @Schema
    (
        description = "A human-readable message describing the result of the operation",
        example = "Successfully retrieved 5 events"
    )
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}