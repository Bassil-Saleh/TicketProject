package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.dtos.requests.CreateEventHostRequest;
import com.ticketproject.webapp.dtos.requests.VerifyEventHostRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventHostResponse;
import com.ticketproject.webapp.dtos.responses.GetEventHostProfileResponse;
import com.ticketproject.webapp.dtos.responses.VerifyEventHostResponse;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.services.EventHostService;
import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.EventHost;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * EventHostController is a REST controller that routes requests
 * concerning EventHost entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.EventHosts.ROOT)
public class EventHostController
{
    private final EventHostService eventHostService;

    public EventHostController(EventHostService eventHostService)
    {
        this.eventHostService = eventHostService;
    }

    /**
     * Handles a request to create a new EventHost.
     * @param request a CreateEventHostRequest from the client
     * @return a CreateEventHostResponse indicating the result of the request
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEventHostResponse createEventHost
    (
        @Valid
        @RequestBody CreateEventHostRequest request
    )
    {
        return eventHostService.createEventHost(request);
    }

    /**
     * Handles a request to verify a new EventHost.
     * @param request a VerifyEventHostRequest from the client
     * @return a VerifyEventHostResponse indicating the result of the request
     */
    @PatchMapping(ApiPaths.EventHosts.VERIFICATION)
    @ResponseStatus(HttpStatus.OK)
    public VerifyEventHostResponse verifyEventHost
    (
        @Valid
        @RequestBody VerifyEventHostRequest request
    )
    {
        return eventHostService.verifyEventHost(request);
    }

    /**
     * Handles a request to retrieve profile info on a logged in EventHost.
     * @param request an HttpServletRequest from the client
     * containing a JWT in the Authorization header
     * @return a GetEventHostProfileResponse containing profile info
     * on the logged in EventHost
     */
    @GetMapping(ApiPaths.EventHosts.PROFILE)
    public GetEventHostProfileResponse getEventHostProfile(HttpServletRequest request)
    {
        EventHost eventHost = (EventHost) request.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventHostService.getEventHostProfile(eventHost);
    }
}
