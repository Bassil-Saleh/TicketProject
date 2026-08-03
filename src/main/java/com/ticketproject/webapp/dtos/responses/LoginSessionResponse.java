package com.ticketproject.webapp.dtos.responses;

/**
 * LoginSessionResponse specifies what a valid response
 * to a successful login attempt to an event host account
 * should look like.
 * @param jwt a JSON Web Token containing the raw session token
 *            as its subject, signed with HMAC-SHA256
 */
public record LoginSessionResponse
(
    String jwt
)
{
}