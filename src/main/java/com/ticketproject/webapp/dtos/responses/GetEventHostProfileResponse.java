package com.ticketproject.webapp.dtos.responses;

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
public record GetEventHostProfileResponse
(
    @NotBlank(message = "First name is required")
    String firstName,

    String middleName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotBlank(message = "Email address is required")
    String email,

    @NotNull(message = "Last login time is required")
    LocalDateTime lastLogin
)
{
}
