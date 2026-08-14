package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventHostEmailRequest specifies what a valid request to change a
 * logged in event host's email address should look like.
 * 
 * @param email the new email address
 */
@Schema(description = "Request body for editing the authenticated event host's email address")
public record EditEventHostEmailRequest
(
    @Schema
    (
        description = "The new email address for the event host account",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "new.email@example.com",
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
    String email
)
{
}