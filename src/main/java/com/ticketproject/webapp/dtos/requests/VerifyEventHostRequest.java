package com.ticketproject.webapp.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record VerifyEventHostRequest
(
    @NotBlank(message = "Account verification token is required")
    String verificationToken
)
{
}
