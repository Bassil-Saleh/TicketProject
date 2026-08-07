package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

public record CreatePublicEventRegistrationResponse
(
    @NotBlank(message = "Public token of ticket cannot be blank")
    String publicToken,

    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
