package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

public record EditEventDescriptionByPublicIdResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
