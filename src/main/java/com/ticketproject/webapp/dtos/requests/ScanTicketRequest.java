package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanTicketRequest
(
    @NotBlank(message = "Public token of ticket cannot be blank")
    @Size
    (
        max = AppConstants.Database.Tickets.Sizes.PUBLIC_TOKEN_LENGTH,
        message =
        "Public token length cannot be longer than " +
        AppConstants.Database.Tickets.Sizes.PUBLIC_TOKEN_LENGTH +
        " characters"
    )
    String publicToken
)
{
}
