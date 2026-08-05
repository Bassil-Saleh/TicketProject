package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UsePasswordResetTokenRequest specifies what a valid request
 * to use a password reset token should look like.
 * 
 * @param passwordResetToken the password reset token
 * @param password the new password
 */
public record UsePasswordResetTokenRequest
(
    @NotBlank(message = "Password reset token is required")
    @Size
    (
        max = AppConstants.DTO.PasswordResetTokens.Sizes.MAX_PASSWORD_RESET_TOKEN_LENGTH,
        message = "Password reset token cannot be longer than " +
        AppConstants.DTO.PasswordResetTokens.Sizes.MAX_PASSWORD_RESET_TOKEN_LENGTH +
        " characters long"
    )
    String passwordResetToken,

    @NotBlank(message = "New password is required")
    @Size
    (
        min = AppConstants.DTO.EventHosts.Sizes.MIN_PASSWORD_LENGTH,
        max = AppConstants.DTO.EventHosts.Sizes.MAX_PASSWORD_LENGTH,
        message = "New password must be between " +
        AppConstants.DTO.EventHosts.Sizes.MIN_PASSWORD_LENGTH +
        " and " +
        AppConstants.DTO.EventHosts.Sizes.MAX_PASSWORD_LENGTH +
        " characters long"
    )
    String password
)
{
}
