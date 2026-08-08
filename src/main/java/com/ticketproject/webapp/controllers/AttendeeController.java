package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.dtos.requests.CreatePublicEventRegistrationRequest;
import com.ticketproject.webapp.dtos.responses.CreatePublicEventRegistrationResponse;
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

    @PostMapping(ApiPaths.Attendees.REGISTRATION)
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePublicEventRegistrationResponse createPublicEventRegistration
    (
        @Valid
        @RequestBody CreatePublicEventRegistrationRequest request
    )
    {
        return attendeeService.createPublicEventRegistration(request);
    }
    
}
