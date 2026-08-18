package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.responses.GetEventByPublicIdResponse;
import com.ticketproject.webapp.dtos.responses.GetEventsResponse;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.dtos.requests.CreateEventRequest;
import com.ticketproject.webapp.dtos.requests.ChangeEventToPrivateEventRequest;
import com.ticketproject.webapp.dtos.requests.ChangeEventToPublicEventRequest;
import com.ticketproject.webapp.dtos.requests.EditEventAddressByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventDatesByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventDescriptionByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventNameByPublicIdRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventResponse;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.model.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * EventController is a REST controller that routes requests
 * concerning Event entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Events.ROOT)
@Tag(name = "Events", description = "Endpoints for creating, retrieving, editing, and deleting events")
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
    @Operation
    (
        summary = "Create a new event",
        description =
            "Creates a new event owned by the authenticated event host. " +
            "The event includes details such as name, description, dates, " +
            "type, maximum attendees, and address. Returns the newly " +
            "created event's public ID. Requires authentication."
    )
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
     * @return a GetEventByPublicIdResponse on success
     */
    @Operation
    (
        summary = "Get an event by its public ID",
        description =
            "Retrieves detailed information about a single event identified " +
            "by its public ID, including name, description, dates, type, " +
            "address, and coordinates. This endpoint does not require authentication."
    )
    @SecurityRequirements
    @GetMapping(ApiPaths.Events.BY_PUBLIC_ID)
    @ResponseStatus(HttpStatus.OK)
    public GetEventByPublicIdResponse getEventByPublicId
    (
        @Parameter
        (
            description = "The unique public identifier of the event to retrieve",
            required = true,
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
        )
        @PathVariable String publicId
    )
    {
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
    @Operation
    (
        summary = "Get events created by the authenticated event host",
        description =
            "Retrieves a list of events created by the authenticated event host. " +
            "The number of events returned is controlled by the 'count' query " +
            "parameter, which must be between 1 and 500. Requires authentication."
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetEventsResponse getEvents
    (
        @Parameter
        (
            description = "The number of events to retrieve (must be between 1 and 500)",
            required = true,
            example = "10"
        )
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
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Delete an event by its public ID",
        description =
            "Deletes an event identified by its public ID. Only the event host " +
            "who created the event is permitted to delete it. Requires authentication."
    )
    @DeleteMapping(ApiPaths.Events.BY_PUBLIC_ID)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse deleteEventByPublicId
    (
        @Parameter
        (
            description = "The unique public identifier of the event to delete",
            required = true,
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
        )
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

    /**
     * Handles a request to let a logged in user change the address
     * of an event identified by its public ID. Only the logged in user
     * who created the event should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Edit an event's address",
        description =
            "Updates the address of an event identified by its public ID. " +
            "Only the event host who created the event is permitted to edit it. " +
            "Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.ADDRESS)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse editEventAddressByPublicId
    (
        @Valid
        @RequestBody EditEventAddressByPublicIdRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.editEventAddressByPublicId(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user change the name
     * of an event identified by its public ID. Only the logged in user
     * who created the event should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Edit an event's name",
        description =
            "Updates the name of an event identified by its public ID. " +
            "Only the event host who created the event is permitted to edit it. " +
            "Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.NAME)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse editEventNameByPublicId
    (
        @Valid
        @RequestBody EditEventNameByPublicIdRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.editEventNameByPublicId(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user change the description
     * of an event identified by its public ID. Only the logged in user
     * who created the event should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Edit an event's description",
        description =
            "Updates the description of an event identified by its public ID. " +
            "Only the event host who created the event is permitted to edit it. " +
            "Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.DESCRIPTION)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse editEventDescriptionByPublicId
    (
        @Valid
        @RequestBody EditEventDescriptionByPublicIdRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.editEventDescriptionByPublicId(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user change the
     * start and end dates/times of an event identified by
     * its public ID. Only the logged in user who created
     * the event should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Edit an event's start and end dates/times",
        description =
            "Updates the start and end dates/times of an event identified by " +
            "its public ID. Only the event host who created the event is " +
            "permitted to edit it. Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.DATES)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse editEventDatesByPublicId
    (
        @Valid
        @RequestBody EditEventDatesByPublicIdRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.editEventDatesByPublicId(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user change a preexisting event
     * into a public event. Only the logged in user who created the event
     * should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Change an existing event into a public event",
        description =
            "Change an existing event identified by its public ID " +
            "into a public event. Only the event host who created the event " +
            "is permitted to edit it. Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.CHANGE_TO_PUBLIC)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse changeEventToPublicEvent
    (
        @Valid
        @RequestBody ChangeEventToPublicEventRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.changeEventToPublicEvent(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user change a preexisting event
     * into a private event. Only the logged in user who created the event
     * should be allowed to edit it.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Change an existing event into a private event",
        description =
            "Change an existing event identified by its public ID " +
            "into a private event. Only the event host who created the event " +
            "is permitted to edit it. Requires authentication."
    )
    @PatchMapping(ApiPaths.Events.CHANGE_TO_PRIVATE)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse changeEventToPrivateEvent
    (
        @Valid
        @RequestBody ChangeEventToPrivateEventRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return eventService.changeEventToPrivateEvent(eventHost, request);
    }
}