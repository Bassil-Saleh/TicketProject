package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.dtos.requests.LoginSessionRequest;
import com.ticketproject.webapp.dtos.responses.LoginSessionResponse;
import com.ticketproject.webapp.services.SessionService;
import com.ticketproject.webapp.constants.ApiPaths;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * SessionController is a REST controller that routes requests
 * concerning Session entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Sessions.ROOT)
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
}