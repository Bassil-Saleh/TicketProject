package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditEventHostPasswordRequest
(
    @NotBlank(message = "Password is required")
    @Size
    (
        min = AppConstants.DTO.EventHosts.Sizes.MIN_PASSWORD_LENGTH,
        max = AppConstants.DTO.EventHosts.Sizes.MAX_PASSWORD_LENGTH,
        message = "Password must be between " +
        AppConstants.DTO.EventHosts.Sizes.MIN_PASSWORD_LENGTH +
        " and " +
        AppConstants.DTO.EventHosts.Sizes.MAX_PASSWORD_LENGTH +
        " characters long"
    )
    String password
)
{
}
