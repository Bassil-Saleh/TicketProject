package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreatePublicEventRegistrationRequest specifies what a valid request
 * to register for a public event should look like.
 * 
 * @param publicId the event's public id
 * @param firstName the attendee's first name
 * @param middleName the attendee's middle name (optional)
 * @param lastName the attendee's last name
 * @param email the attendee's email address
 */
public record CreatePublicEventRegistrationRequest
(
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
        " characters"
    )
    String publicId,

    @NotBlank(message = "First name is required")
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "First name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String firstName,

    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Middle name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String middleName,

    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Last name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    @NotBlank(message = "Last name is required")
    String lastName,

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
