package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * VerifyEventHostRequest specifies what a valid request
 * to verify a new event host account should look like.
 * @param verificationToken the verification token
 * which the user received after making a new account 
 */
public record VerifyEventHostRequest
(
    @NotBlank(message = "Account verification token is required")
    @Size
    (
        max = AppConstants.DTO.EventHosts.Sizes.MAX_ACCOUNT_VERIFICATION_TOKEN_LENGTH,
        message =
        "Account verification token length must not be greater than " +
        AppConstants.DTO.EventHosts.Sizes.MAX_ACCOUNT_VERIFICATION_TOKEN_LENGTH +
        " characters"
    )
    String verificationToken
)
{
}
