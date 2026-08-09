package com.ticketproject.webapp.services.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.dtos.requests.CreatePublicEventRegistrationRequest;
import com.ticketproject.webapp.dtos.responses.CreatePublicEventRegistrationResponse;
import com.ticketproject.webapp.exceptions.EmailAlreadyExistsException;
import com.ticketproject.webapp.exceptions.EventRegistrationException;
import com.ticketproject.webapp.model.entities.Attendee;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.repositories.AttendeeRepository;
import com.ticketproject.webapp.model.repositories.EventRepository;
import com.ticketproject.webapp.model.repositories.TicketRepository;
import com.ticketproject.webapp.services.database.BlindIndexService;
import com.ticketproject.webapp.services.email.EmailService;

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
    private final EmailService emailService;

    public AttendeeService
    (
        AttendeeRepository attendeeRepository,
        EventRepository eventRepository,
        TicketRepository ticketRepository,
        TicketService ticketService,
        BlindIndexService blindIndexService,
        EmailService emailService
    )
    {
        this.attendeeRepository = attendeeRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.blindIndexService = blindIndexService;
        this.emailService = emailService;
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

        byte[] emailBlindIndex = blindIndexService.computeIndex(request.email());

        if (ticketRepository.attendeeRegistrationExistsByEventId(emailBlindIndex, foundEvent.getId()))
        {
            throw new EmailAlreadyExistsException("A registration with the same email address already exists for this event.");
        }

        if (foundEvent.getRegistrationStatus() == RegistrationStatus.CLOSED)
        {
            throw new EventRegistrationException("The registration period for this event is already closed.");
        }

        if (foundEvent.getMaxAttendees() != null && ticketRepository.getRegistrationCountByEventId(foundEvent.getId()) >= foundEvent.getMaxAttendees())
        {
            throw new EventRegistrationException("The event is currently at maximum capacity, so no further registrations are possible.");
        }

        if (foundEvent.getStartDateTime().isBefore(LocalDateTime.now()))
        {
            throw new EventRegistrationException("Cannot register after the event has already begun.");
        }

        Ticket signedTicket = ticketService.createSignedTicket(foundEvent);

        // If the attendee isn't already in the database,
        // create a new Attendee record for them,
        // otherwise use a pre-existing Attendee record.
        Optional<Attendee> attendee = attendeeRepository.findByEmailIndex(emailBlindIndex);
        if (attendee.isEmpty())
        {
            Attendee newAttendee = new Attendee.Builder()
                .firstName(request.firstName())
                .middleName(request.middleName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
            
            signedTicket.setEvent(foundEvent);
            signedTicket.setAttendee(newAttendee);

            newAttendee = attendeeRepository.save(newAttendee);
            signedTicket = ticketRepository.save(signedTicket);
        }
        else
        {
            Attendee foundAttendee = attendee.get();

            signedTicket.setEvent(foundEvent);
            signedTicket.setAttendee(foundAttendee);

            foundAttendee = attendeeRepository.save(foundAttendee);
            signedTicket = ticketRepository.save(signedTicket);
        }

        // Send the ticket email with a QR code to the attendee.
        // If the email fails to send, the @Transactional annotation
        // will roll back the entire transaction (no DB persistence).
        EventHost eventHost = foundEvent.getEventHost();
        String eventHostName = eventHost.getFirstName() + " " + eventHost.getLastName();
        EventAddress eventAddress = foundEvent.getEventAddress();

        emailService.sendTicketEmail
        (
            request.email(),
            signedTicket.getPublicToken(),
            foundEvent.getName(),
            foundEvent.getStartDateTime(),
            foundEvent.getEndDateTime(),
            eventHostName,
            eventAddress.getCity(),
            eventAddress.getState(),
            eventAddress.getCountry()
        );

        return new CreatePublicEventRegistrationResponse("Registration completed. A ticket with a QR code has been sent to your email.");
    }
}
