package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * GetEventHostProfileResponse specifies what a valid response to
 * a successful request to retrieve profile info on an
 * event host account should look like.
 * 
 * @param firstName first name
 * @param middleName middle name (optional)
 * @param lastName last name
 * @param email email address
 * @param lastLogin when the user last logged in
 */
@Schema(description = "Response body containing profile information of the authenticated event host")
public record GetEventHostProfileResponse
(
    @Schema
    (
        description = "The event host's first name",
        example = "John"
    )
    @NotBlank(message = "First name is required")
    String firstName,

    @Schema
    (
        description = "The event host's middle name (may be null)",
        example = "Michael"
    )
    String middleName,

    @Schema
    (
        description = "The event host's last name",
        example = "Doe"
    )
    @NotBlank(message = "Last name is required")
    String lastName,

    @Schema
    (
        description = "The event host's email address",
        example = "john.doe@example.com"
    )
    @NotBlank(message = "Email address is required")
    String email,

    @Schema
    (
        description = "The date and time of the event host's last login",
        example = "2026-08-14T10:30:00"
    )
    @NotNull(message = "Last login time is required")
    LocalDateTime lastLogin
)
{
}