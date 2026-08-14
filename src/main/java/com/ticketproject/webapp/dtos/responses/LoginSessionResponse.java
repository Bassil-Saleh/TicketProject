package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LoginSessionResponse specifies what a valid response
 * to a successful login attempt to an event host account
 * should look like.
 * @param jwt a JSON Web Token containing the raw session token
 *            as its subject, signed with HMAC-SHA256
 */
@Schema(description = "Response body returned after successfully logging into an event host account")
public record LoginSessionResponse
(
    @Schema
    (
        description =
            "A JSON Web Token (JWT) signed with HMAC-SHA256. Include this " +
            "token in the Authorization header of subsequent authenticated " +
            "requests as: `Authorization: Bearer <token>`",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String jwt
)
{
}