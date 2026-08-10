package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.services.model.TicketScanService;
import com.ticketproject.webapp.dtos.requests.ScanTicketRequest;
import com.ticketproject.webapp.dtos.responses.GetScannedTicketsByEventHostResponse;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.TicketScans.ROOT)
public class TicketScanController
{
    private TicketScanService ticketScanService;

    public TicketScanController(TicketScanService ticketScanService)
    {
        this.ticketScanService = ticketScanService;
    }

    /**
     * Handles a request to let a logged in user scan
     * an attendee's ticket for an event.
     * @param request the request body
     * @param servletRequest the HTTP Servlet request
     * @return a SingleMessageResponse on success
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleMessageResponse scanTicket
    (
        @Valid
        @RequestBody ScanTicketRequest request,
        HttpServletRequest servletRequest
    )
    {
        EventHost eventHost = (EventHost) servletRequest.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return ticketScanService.scanTicket(eventHost, request);
    }

    /**
     * Handles a request to let a logged in user retrieve a list of
     * all attendee tickets which they have scanned.
     * @param request the request body
     * @return a GetScannedTicketsByEventHostResponse on success
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetScannedTicketsByEventHostResponse getScannedTicketsByEventHost(HttpServletRequest request)
    {
        EventHost eventHost = (EventHost) request.getAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE);
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        return ticketScanService.getScannedTicketsByEventHost(eventHost);
    }
}
