package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventHostEmailRequest specifies what a valid request to change a
 * logged in event host's email address should look like.
 * 
 * @param email the new email address
 */
public record EditEventHostEmailRequest
(
    @NotBlank(message = "Email address is required")
    @Email
    (
        // Without this regular expression, the @Email annotation will accept
        // email addresses without a top-level domain, which isn't want I want
        // because I can't send emails to addresses without a top-level domain.
        regexp = AppConstants.DTO.EventHosts.Definitions.EMAIL_ADDRESS_REGEX,
        message = "Must be a valid email address"
    )
    @Size
    (
        max = AppConstants.DTO.EventHosts.Sizes.MAX_EMAIL_LENGTH,
        message = "Email address must not exceed " + AppConstants.DTO.EventHosts.Sizes.MAX_EMAIL_LENGTH + " characters"
    )
    String email
)
{
}
