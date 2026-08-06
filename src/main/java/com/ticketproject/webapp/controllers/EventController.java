package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.responses.GetEventByPublicIdResponse;
import com.ticketproject.webapp.dtos.responses.GetEventsResponse;
import com.ticketproject.webapp.dtos.responses.DeleteEventByPublicIdResponse;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.dtos.requests.CreateEventRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventResponse;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.EventService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * EventController is a REST controller that routes requests
 * concerning Event entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Events.ROOT)
public class EventController
{
    private final EventService eventService;

    public EventController(EventService eventService)
    {
        this.eventService = eventService;
    }

    /**
     * Handles a request to let a logged in user create a new Event.
     * 
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a CreateEventResponse on success
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEventResponse createEvent
    (
        @Valid
        @RequestBody CreateEventRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.createEvent(eventHost, request);
    }

    /**
     * Handles a request to retrieve info on a single event based on its public ID.
     * 
     * @param publicId the event's public ID
     * @param servletRequest the HTTP Servlet request
     * @return a GetEventByPublicIdResponse on success
     */
    @GetMapping(ApiPaths.Events.BY_PUBLIC_ID)
    @ResponseStatus(HttpStatus.OK)
    public GetEventByPublicIdResponse getEventByPublicId
    (
        @PathVariable String publicId,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.getEventByPublicId(publicId);
    }

    /**
     * Handles a request to let the logged in user retrieve info
     * on multiple events they've created.
     * 
     * @param count number of events to retrieve
     * @param servletRequest the HTTP Servlet request
     * @return a GetEventsResponse on success
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetEventsResponse getEvents
    (
        @RequestParam("count")
        Long count,
        HttpServletRequest servletRequest
    )
    {
        if (count == null || count < 1)
        {
            throw new InvalidRequestException("Number of events to retrieve must be at least 1");
        }
        if (count > AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT)
        {
            throw new InvalidRequestException("Number of events to retrieve cannot be more than " + AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT);
        }
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.getEvents(eventHost, count);
    }

    /**
     * Handles a request to let a logged in user delete an event
     * identified by its public ID. Only the logged in user who
     * created the event should be allowed to delete that event.
     * 
     * @param publicId the event's public ID
     * @param servletRequest the HTTP Servlet request
     * @return a DeleteEventByPublicIdResponse on success
     */
    @DeleteMapping(ApiPaths.Events.BY_PUBLIC_ID)
    @ResponseStatus(HttpStatus.OK)
    public DeleteEventByPublicIdResponse deleteEventByPublicId
    (
        @PathVariable String publicId,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.deleteEventByPublicId(eventHost, publicId);
    }
}
