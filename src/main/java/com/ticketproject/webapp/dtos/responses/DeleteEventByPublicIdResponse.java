package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

public record DeleteEventByPublicIdResponse
(
    @NotBlank(message = "Message cannot be null")
    String message
)
{
}
