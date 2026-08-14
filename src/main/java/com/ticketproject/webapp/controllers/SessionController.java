package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.dtos.requests.LoginSessionRequest;
import com.ticketproject.webapp.dtos.responses.LoginSessionResponse;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.services.model.SessionService;
import com.ticketproject.webapp.exceptions.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * SessionController is a REST controller that routes requests
 * concerning Session entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Sessions.ROOT)
@Tag(name = "Sessions", description = "Endpoints for logging in and out of event host accounts")
public class SessionController
{
    private final SessionService sessionService;

    /**
     * Constructs a new SessionController with the required service.
     * @param sessionService the service for handling session-related operations
     */
    public SessionController(SessionService sessionService)
    {
        this.sessionService = sessionService;
    }

    /**
     * Handles a request to log into an event host account.
     * @param request a LoginSessionRequest containing email and password
     * @return a LoginSessionResponse containing a signed JWT
     */
    @Operation
    (
        summary = "Log into an event host account",
        description =
            "Authenticates an event host using their email address and " +
            "password. On success, returns a signed JSON Web Token (JWT) " +
            "that must be included in the Authorization header of " +
            "subsequent authenticated requests as: " +
            "`Authorization: Bearer <token>`. " +
            "This endpoint does not require authentication."
    )
    @SecurityRequirements
    @PostMapping(ApiPaths.Sessions.LOGIN)
    @ResponseStatus(HttpStatus.CREATED)
    public LoginSessionResponse login
    (
        @Valid
        @RequestBody LoginSessionRequest request
    )
    {
        return sessionService.login(request);
    }

    /**
     * Handles a request to log out an event host account from all devices.
     * @param request an HttpServletRequest containing a JWT
     * @return a SingleMessageResponse after performing the logout
     */
    @Operation
    (
        summary = "Log out from all devices",
        description =
            "Revokes all active sessions for the authenticated event host, " +
            "effectively logging them out from all devices. All previously " +
            "issued JWTs will become invalid. Requires authentication."
    )
    @PatchMapping(ApiPaths.Sessions.LOGOUT_ALL_DEVICES)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse logoutAllDevices(HttpServletRequest request)
    {
        EventHost eventHost = (EventHost) request.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return sessionService.logoutAllDevices(eventHost);
    }

    /**
     * Handles a request to log out an event host account from the given session.
     * @param request an HttpServletRequest containing a JWT
     * @return a SingleMessageResponse after performing the logout
     */
    @Operation
    (
        summary = "Log out from the current device",
        description =
            "Revokes the current session associated with the provided JWT, " +
            "logging the event host out from the current device only. " +
            "Other active sessions remain valid. Requires authentication."
    )
    @PatchMapping(ApiPaths.Sessions.LOGOUT)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse logout(HttpServletRequest request)
    {
        Session session = (Session) request.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_SESSION_ATTRIBUTE);
        if (session == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return sessionService.logout(session);
    }
}