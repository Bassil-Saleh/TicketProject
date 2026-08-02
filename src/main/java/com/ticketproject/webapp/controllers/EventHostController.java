package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.dtos.requests.CreateEventHostRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventHostResponse;
import com.ticketproject.webapp.services.EventHostService;
import com.ticketproject.webapp.constants.ApiPaths;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.EventHosts.ROOT)
public class EventHostController
{
    private final EventHostService eventHostService;

    public EventHostController(EventHostService eventHostService)
    {
        this.eventHostService = eventHostService;
    }

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
}
