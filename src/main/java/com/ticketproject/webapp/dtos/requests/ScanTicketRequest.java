package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ScanTicketRequest specifies what a valid request
 * to scan an attendee's ticket should look like.
 * @param publicToken the public token which was encoded
 * into the attendee's ticket (i.e. a QR code)
 */
@Schema(description = "Request body for scanning an attendee's ticket")
public record ScanTicketRequest
(
    @Schema
    (
        description = "The public token encoded in the attendee's ticket QR code",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "eyJhbGciOiJFZDI1NTE5IiwidHlwIjoiSldUIn0...",
        maximum = "512"
    )
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