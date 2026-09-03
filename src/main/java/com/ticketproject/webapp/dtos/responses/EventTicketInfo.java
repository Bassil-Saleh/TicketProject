package com.ticketproject.webapp.dtos.responses;

import com.ticketproject.webapp.model.enums.InvitationStatus;
import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * EventTicketInfo specifies what a single record of info
 * on a ticket for an event should look like. Used when
 * retrieving info on a list of people registered for
 * an event by a logged in event host.
 * @param firstName the attendee's first name
 * @param middleName the attendee's middle name
 * @param lastName the attendee's last name
 * @param email the attendee's email address
 * @param present whether or not the ticket has already been scanned
 * @param invitationStatus the attendee's response to the registration
 * @param created when the ticket was first created
 * @param deletedAt when the ticket was soft-deleted (optional)
 * @param lastUpdated when the ticket was last updated
 */
@Schema(description = "Information about a single ticket for an event")
public record EventTicketInfo
(
    @Schema
    (
        description = "The attendee's first name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Jane",
        maximum = "100"
    )
    @NotBlank(message = "First name is required")
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "First name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String firstName,

    @Schema
    (
        description = "The attendee's middle name (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Marie",
        maximum = "100"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Middle name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String middleName,

    @Schema
    (
        description = "The attendee's last name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Smith",
        maximum = "100"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Last name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String lastName,

    @Schema
    (
        description = "The attendee's email address, where the ticket was sent",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "jane.smith@example.com",
        maximum = "254"
    )
    @NotBlank(message = "Email address is required")
    @Email
    (
        // Without this regular expression, the @Email annotation will accept
        // email addresses without a top-level domain, which isn't want I want
        // because I can't send emails to addresses without a top-level domain.
        regexp = AppConstants.Database.EventHosts.Definitions.EMAIL_ADDRESS_REGEX,
        message = "Must be a valid email address"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH,
        message = "Email address must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH + " characters"
    )
    String email,

    @Schema
    (
        description = "A flag indicating whether or not the ticket has already been scanned.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "false"
    )
    @NotNull(message = "Present/not present boolean flag required")
    boolean present,

    @Schema
    (
        description = "The status of the ticket: PENDING, ACCEPTED, or REJECTED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "PENDING"
    )
    @NotNull(message = "Invitation status is required")
    InvitationStatus invitationStatus,

    @Schema
    (
        description = "When the ticket was first created",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-08-14T10:30:00"
    )
    @NotNull(message = "Creation date/time is required")
    LocalDateTime created,

    @Schema
    (
        description = "When the ticket was deleted",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "2026-08-14T10:30:00"
    )
    LocalDateTime deletedAt,

    @Schema
    (
        description = "When the ticket was last updated",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-08-14T10:30:00"
    )
    @NotNull(message = "Date/time of last update is required")
    LocalDateTime lastUpdated
)
{
}
