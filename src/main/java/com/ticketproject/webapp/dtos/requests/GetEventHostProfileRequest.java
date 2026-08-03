package com.ticketproject.webapp.dtos.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * GetEventHostProfileRequest specifies what a valid request to retrieve
 * the profile info on a single event host account should look like.
 * 
 * @param jwt a JSON Web Token containing the raw session token
 *            as its subject, signed with HMAC-SHA256
 */
public record GetEventHostProfileRequest
(
    @NotBlank(message = "JSON Web Token is required for authentication")
    String jwt
)
{
}
