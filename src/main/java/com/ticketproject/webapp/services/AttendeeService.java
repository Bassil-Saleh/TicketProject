package com.ticketproject.webapp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.dtos.requests.CreatePublicEventRegistrationRequest;
import com.ticketproject.webapp.dtos.responses.CreatePublicEventRegistrationResponse;
import com.ticketproject.webapp.exceptions.EmailAlreadyExistsException;
import com.ticketproject.webapp.exceptions.EventRegistrationException;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.repositories.AttendeeRepository;
import com.ticketproject.webapp.model.repositories.EventRepository;
import com.ticketproject.webapp.model.repositories.TicketRepository;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AttendeeService is a service used by controllers to handle
 * API route requests involving Attendee entities
 * (i.e. public event registrations, private event invitations).
 */
@Service
@Transactional
public class AttendeeService
{
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final BlindIndexService blindIndexService;

    public AttendeeService
    (
        AttendeeRepository attendeeRepository,
        EventRepository eventRepository,
        TicketRepository ticketRepository,
        TicketService ticketService,
        BlindIndexService blindIndexService
    )
    {
        this.attendeeRepository = attendeeRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.blindIndexService = blindIndexService;
    }

    public CreatePublicEventRegistrationResponse createPublicEventRegistration
    (
        CreatePublicEventRegistrationRequest request
    )
    {
        Optional<Event> event = eventRepository.findByPublicId(request.eventPublicId());

        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (foundEvent.getEventType() != EventType.PUBLIC)
        {
            throw new EventRegistrationException("This is not a public event.");
        }

        if (foundEvent.getRegistrationStatus() == RegistrationStatus.CLOSED)
        {
            throw new EventRegistrationException("The registration period for this event is already closed.");
        }

        if (foundEvent.getStartDateTime().isBefore(LocalDateTime.now()))
        {
            throw new EventRegistrationException("Cannot register after the event has already begun.");
        }

        byte[] emailBlindIndex = blindIndexService.computeIndex(request.email());

        if (ticketRepository.attendeeRegistrationExistsByEventId(emailBlindIndex, foundEvent.getId()))
        {
            throw new EmailAlreadyExistsException("A registration with the same email address already exists for this event.");
        }

        Ticket signedTicket = ticketService.createSignedTicket(foundEvent);

        Attendee attendee = new Attendee.Builder()
            .firstName(request.firstName())
            .middleName(request.middleName())
            .lastName(request.lastName())
            .email(request.email())
            .build();
        
        signedTicket.setEvent(foundEvent);
        signedTicket.setAttendee(attendee);

        attendee = attendeeRepository.save(attendee);
        signedTicket = ticketRepository.save(signedTicket);

        return new CreatePublicEventRegistrationResponse(signedTicket.getPublicToken(), "Registration saved.");
    }
}
