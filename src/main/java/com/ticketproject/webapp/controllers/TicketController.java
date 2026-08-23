package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.services.model.TicketService;
import com.ticketproject.webapp.dtos.requests.RespondToInvitationRequest;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TicketController is a REST controller that routes requests
 * concerning Ticket entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.Tickets.ROOT)
@Tag
(
    name = "Tickets",
    description = "Endpoints for managing and modifying ticket records"
)
public class TicketController
{
    private TicketService ticketService;

    public TicketController(TicketService ticketService)
    {
        this.ticketService = ticketService;
    }

    /**
     * Handles a request to respond to an invitation to a private event.
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    @Operation
    (
        summary = "Respond to an invitation for a private event",
        description =
            "Records a response to an invitation for a private event. " +
            "This endpoint does not require authentication."
    )
    @SecurityRequirements
    @PatchMapping(ApiPaths.Tickets.INVITATION)
    @ResponseStatus(HttpStatus.OK)
    public SingleMessageResponse respondToInvitation
    (
        @Valid
        @RequestBody RespondToInvitationRequest request
    )
    {
        return ticketService.respondToInvitation(request);
    }
}
