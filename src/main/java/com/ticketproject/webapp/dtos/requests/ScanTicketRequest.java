package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ScanTicketRequest specifies what a valid request
 * to scan an attendee's ticket should look like.
 * @param publicToken the public token which was encoded
 * into the attendee's ticket (i.e. a QR code)
 */
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
