package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.CreateEventRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventResponse;
import com.ticketproject.webapp.dtos.responses.GetEventByPublicIdResponse;
import com.ticketproject.webapp.dtos.responses.GetEventsResponse;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.repositories.EventRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * EventService is a service used by controllers to handle
 * API route requests involving Event entities
 * (i.e. creating/editing/deleting events).
 */
@Service
@Transactional
public class EventService
{
    private final EventRepository eventRepository;

    public EventService (EventRepository eventRepository)
    {
        this.eventRepository = eventRepository;
    }

    public CreateEventResponse createEvent(EventHost eventHost, CreateEventRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }
        // If a precise location was provided, make sure that both
        // latitude and longitude coordinates are present.
        if ((request.latitude() == null) != (request.longitude() == null))
        {
            throw new InvalidRequestException
            ("If the event has a precise location, both latitude and longitude coordinates must be provided.");
        }

        try
        {
            Event newEvent = buildNewEvent(eventHost, request);
            newEvent = eventRepository.save(newEvent);
            return new CreateEventResponse(newEvent.getPublicId(), "Event successfully created.");
        }
        catch (InvalidRequestException e)
        {
            throw new InvalidRequestException("Invalid request - " + e.getMessage());
        }
    }

    private Event buildNewEvent(EventHost eventHost, CreateEventRequest request)
    {
        Event newEvent = new Event.Builder()
            .name(request.name())
            .description(request.description())
            .startDateTime(request.startDateTime())
            .endDateTime(request.endDateTime())
            .maxAttendees(request.maxAttendees())
            .eventHost(eventHost)
            .eventType(request.eventType())
            // .publicId(hashingService.generateRandomToken())
            .publicId(UUID.randomUUID().toString())
            .build();
        
        newEvent.setEventStatus(EventStatus.DRAFT);
        newEvent.setRegistrationStatus(RegistrationStatus.OPEN);
        
        EventAddress address = new EventAddress.Builder()
            .addressLine1(request.addressLine1())
            .addressLine2(request.addressLine2())
            .city(request.city())
            .state(request.state())
            .postalCode(request.postalCode())
            .country(request.country())
            .build();

        try
        {
            address.setLatitude(request.latitude());
            address.setLongitude(request.longitude());
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidRequestException("Invalid scale/precision for latitude/longitude coordinates.");
        }

        EventSigningKey signingKey = createSigningKey(newEvent);
        newEvent.setSigningKey(signingKey);
        newEvent.setEventAddress(address);

        return newEvent;
    }

    /**
     * Generates a fresh key pair for constructing an EventSigningKey.
     * @return a KeyPair
     * @throws RuntimeException if key pair generation fails
     */
    private KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator
                .getInstance(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_ALGORITHM);
            generator.initialize(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_SIZE_TEST);
            return generator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot generate keypair - algorithm not supported", e);
        }
        catch (InvalidParameterException e)
        {
            throw new RuntimeException("Cannot generate keypair - key size not supported", e);
        }
    }

    /**
     * Helper method to create a valid EventSigningKey for a given event.
     * @param event the event to associate the signing key with
     * @return a new EventSigningKey entity (not yet persisted)
     */
    private EventSigningKey createSigningKey(Event event)
    {
        KeyPair keyPair = generateKeyPair();
        return new EventSigningKey.Builder()
            .event(event)
            .privateKey(keyPair.getPrivate())
            .publicKey(keyPair.getPublic())
            .build();
    }

    public GetEventByPublicIdResponse getEventByPublicId(String publicId)
    {
        if (publicId == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (publicId.length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(publicId);
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();
        EventAddress foundEventAddress = foundEvent.getEventAddress();

        return new GetEventByPublicIdResponse
        (
            foundEvent.getPublicId(),
            foundEvent.getName(),
            foundEvent.getDescription(),
            foundEvent.getStartDateTime(),
            foundEvent.getEndDateTime(),
            foundEvent.getMaxAttendees(),
            foundEventAddress.getAddressLine1(),
            foundEventAddress.getAddressLine2(),
            foundEventAddress.getCity(),
            foundEventAddress.getState(),
            foundEventAddress.getPostalCode(),
            foundEventAddress.getCountry(),
            foundEventAddress.getLatitude(),
            foundEventAddress.getLongitude()
        );
    }

    public GetEventsResponse getEvents(EventHost eventHost, Long count)
    {
        if (count == null || count < 1)
        {
            throw new InvalidRequestException("Number of events to retrieve must be at least 1");
        }
        if (count > AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT)
        {
            throw new InvalidRequestException("Number of events to retrieve cannot be more than " + AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT);
        }
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        List<Event> foundEvents = eventRepository.findAllByEventHostId(eventHost.getId());

        List<GetEventByPublicIdResponse> formattedEvents = foundEvents
            .stream()
            .map
            (event ->
            {
                EventAddress address = event.getEventAddress();
                GetEventByPublicIdResponse record =
                    new GetEventByPublicIdResponse
                        (
                            event.getPublicId(),
                            event.getName(),
                            event.getDescription(),
                            event.getStartDateTime(),
                            event.getEndDateTime(),
                            event.getMaxAttendees(),
                            address.getAddressLine1(),
                            address.getAddressLine2(),
                            address.getCity(),
                            address.getState(),
                            address.getPostalCode(),
                            address.getCountry(),
                            address.getLatitude(),
                            address.getLongitude()
                        );
                return record;
            })
            .limit(count)
            .toList();
        
        if (formattedEvents.size() == 0)
        {
            return new GetEventsResponse
            (
                formattedEvents,
                "You currently have no events."
            );
        }
        return new GetEventsResponse
        (
            formattedEvents,
            "Retrieved " +
            formattedEvents.size() +
            " event" +
            (formattedEvents.size() > 1 ? "s." : ".")
        );
    }
}
