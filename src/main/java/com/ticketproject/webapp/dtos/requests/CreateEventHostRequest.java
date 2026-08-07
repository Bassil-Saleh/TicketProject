package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.NotNull;

/**
 * CreateEventHostRequest specifies what a valid request to create
 * a new event host account should look like.
 * @param firstName first name
 * @param middleName middle name (optional)
 * @param lastName last name
 * @param email email address
 * @param password password
 * @param dateOfBirth date of birth
 */
public record CreateEventHostRequest
(
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
        regexp = AppConstants.DTO.EventHosts.Definitions.EMAIL_ADDRESS_REGEX,
        message = "Must be a valid email address"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH,
        message = "Email address must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH + " characters"
    )
    String email,

    @NotBlank(message = "Password is required")
    @Size
    (
        min = AppConstants.Database.EventHosts.Sizes.MIN_PASSWORD_LENGTH,
        max = AppConstants.Database.EventHosts.Sizes.MAX_PASSWORD_LENGTH,
        message = "Password must be between " +
        AppConstants.Database.EventHosts.Sizes.MIN_PASSWORD_LENGTH +
        " and " +
        AppConstants.Database.EventHosts.Sizes.MAX_PASSWORD_LENGTH +
        " characters long"
    )
    String password,

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth
) {}
