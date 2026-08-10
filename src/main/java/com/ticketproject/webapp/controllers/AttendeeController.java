package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.dtos.requests.CreatePublicEventRegistrationRequest;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.services.model.AttendeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * AttendeeController is a REST controller that routes requests
 * concerning Attendee entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Attendees.ROOT)
public class AttendeeController
{
    private final AttendeeService attendeeService;

    public AttendeeController(AttendeeService attendeeService)
    {
        this.attendeeService = attendeeService;
    }

    /**
     * Handles a request to let a user register for a public event.
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    @PostMapping(ApiPaths.Attendees.REGISTRATION)
    @ResponseStatus(HttpStatus.CREATED)
    public SingleMessageResponse createPublicEventRegistration
    (
        @Valid
        @RequestBody CreatePublicEventRegistrationRequest request
    )
    {
        return attendeeService.createPublicEventRegistration(request);
    }
    
}
