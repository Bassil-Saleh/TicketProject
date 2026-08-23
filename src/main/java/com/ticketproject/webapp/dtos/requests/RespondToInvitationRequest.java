package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.InvitationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * RespondToInvitationRequest specifies what a valid request to
 * respond to an invitation to a private event should look like.
 * @param publicToken the public token which was encoded into
 * the attendee's ticket
 */
@Schema(description = "Request body for responding to an event invitation")
public record RespondToInvitationRequest
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
    String publicToken,

    @Schema
    (
        description = "The response to the invitation: PENDING, ACCEPTED, or REJECTED.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "ACCEPTED"
    )
    @NotNull(message = "Invitation response cannot be null")
    InvitationStatus invitationResponse,

    @Schema
    (
        description = "The optional response message sent to the event host.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Can't make it today, sorry!",
        maximum = "5000"
    )
    @Size
    (
        max = AppConstants.DTO.Tickets.Sizes.MAX_MESSAGE_LENGTH,
        message =
            "Message cannot be longer than " +
            AppConstants.DTO.Tickets.Sizes.MAX_MESSAGE_LENGTH +
            " characters"
    )
    String message
)
{
}
