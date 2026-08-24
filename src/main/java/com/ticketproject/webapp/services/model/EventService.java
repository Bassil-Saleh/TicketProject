package com.ticketproject.webapp.services.model;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.ChangeEventToPrivateEventRequest;
import com.ticketproject.webapp.dtos.requests.ChangeEventToPublicEventRequest;
import com.ticketproject.webapp.dtos.requests.CreateEventRequest;
import com.ticketproject.webapp.dtos.requests.EditEventAddressByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventDatesByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventDescriptionByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventMaxAttendeesByPublicIdRequest;
import com.ticketproject.webapp.dtos.requests.EditEventNameByPublicIdRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventResponse;
import com.ticketproject.webapp.dtos.responses.GetEventByPublicIdResponse;
import com.ticketproject.webapp.dtos.responses.GetEventsResponse;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.EventAlreadyCanceledException;
import com.ticketproject.webapp.exceptions.EventAlreadyPublishedException;
import com.ticketproject.webapp.exceptions.InvalidCredentialsException;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.repositories.EventAddressRepository;
import com.ticketproject.webapp.model.repositories.EventRepository;
import com.ticketproject.webapp.model.repositories.TicketRepository;
import com.ticketproject.webapp.services.database.CryptoService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final EventAddressRepository eventAddressRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    public EventService
    (
        EventRepository eventRepository,
        EventAddressRepository eventAddressRepository,
        TicketRepository ticketRepository
    )
    {
        this.eventRepository = eventRepository;
        this.eventAddressRepository = eventAddressRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Services a request to let a logged in event host create a new event.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a CreateEventResponse on success
     */
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

    /**
     * Helper method used when creating a new event.
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a new Event entity
     */
    private Event buildNewEvent(EventHost eventHost, CreateEventRequest request)
    {
        if (request.startDateTime().isEqual(request.endDateTime()))
        {
            throw new InvalidRequestException("Event start date/time cannot be the same as the event end date/time.");
        }

        if (request.startDateTime().isAfter(request.endDateTime()))
        {
            throw new InvalidRequestException("Event start date/time cannot come after the event end date/time.");
        }

        Duration eventDuration = Duration.between(request.startDateTime(), request.endDateTime());
        if (eventDuration.toMinutes() < AppConstants.Database.Events.Sizes.MIN_EVENT_DURATION_MINUTES)
        {
            throw new InvalidRequestException
            (
                "Event duration must be at least " +
                AppConstants.Database.Events.Sizes.MIN_EVENT_DURATION_MINUTES +
                " minutes long."
            );
        }

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

        EventSigningKey signingKey = CryptoService.createSigningKey(newEvent);
        newEvent.setSigningKey(signingKey);
        newEvent.setEventAddress(address);

        return newEvent;
    }

    /**
     * Services a request to retrieve info on an event identified by its public ID.
     * 
     * @param publicId the event's public ID
     * @return a GetEventByPublicIdResponse on success
     */
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
        long numberOfRegisteredAttendees = ticketRepository.getRegistrationCountByEventId(foundEvent.getId());

        return new GetEventByPublicIdResponse
        (
            foundEvent.getPublicId(),
            foundEvent.getName(),
            foundEvent.getDescription(),
            foundEvent.getStartDateTime(),
            foundEvent.getEndDateTime(),
            foundEvent.getEventType(),
            foundEvent.getEventStatus(),
            numberOfRegisteredAttendees,
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

    /**
     * Services a request to retrieve multiple events created by the logged in event host.
     * 
     * @param eventHost the logged in event host
     * @param count number of events to retrieve
     * @return a GetEventsResponse on success
     */
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
                long numberOfRegisteredAttendees = ticketRepository.getRegistrationCountByEventId(event.getId());
                GetEventByPublicIdResponse record =
                    new GetEventByPublicIdResponse
                        (
                            event.getPublicId(),
                            event.getName(),
                            event.getDescription(),
                            event.getStartDateTime(),
                            event.getEndDateTime(),
                            event.getEventType(),
                            event.getEventStatus(),
                            numberOfRegisteredAttendees,
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

    /**
     * Services a request to delete an event created by the logged in event host.
     * Note that an event should only be deletable by the event host who created the event.
     * 
     * @param eventHost the logged in event host
     * @param publicId the event's public ID
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse deleteEventByPublicId(EventHost eventHost, String publicId)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

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

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can delete it.");
        }

        eventRepository.delete(foundEvent);

        return new SingleMessageResponse("Event deleted.");
    }

    /**
     * Services a request to let a logged in event host edit the address of an event they've created.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse editEventAddressByPublicId(EventHost eventHost, EditEventAddressByPublicIdRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        EventAddress foundEventAddress = foundEvent.getEventAddress();
        foundEventAddress.setAddressLine1(request.addressLine1());
        foundEventAddress.setAddressLine2(request.addressLine2());
        foundEventAddress.setCity(request.city());
        foundEventAddress.setCountry(request.country());
        foundEventAddress.setState(request.state());
        foundEventAddress.setPostalCode(request.postalCode());

        try
        {
            foundEventAddress.setLatitude(request.latitude());
            foundEventAddress.setLongitude(request.longitude());
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidRequestException("Invalid request - Invalid scale/precision for latitude/longitude coordinates.");
        }
        LocalDateTime updatedAt = LocalDateTime.now();

        foundEventAddress.setLastUpdated(updatedAt);
        foundEvent.setEventAddress(foundEventAddress);
        foundEvent.setLastUpdated(updatedAt);

        foundEventAddress = eventAddressRepository.save(foundEventAddress);
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("Event address updated.");
    }

    /**
     * Services a request to let a logged in event host edit the name of an event they've created.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse editEventNameByPublicId(EventHost eventHost, EditEventNameByPublicIdRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        foundEvent.setName(request.name());
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("Event name changed.");
    }

    /**
     * Services a request to let a logged in event host edit the description of an event they've created.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse editEventDescriptionByPublicId(EventHost eventHost, EditEventDescriptionByPublicIdRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        foundEvent.setDescription(request.description());
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("Event description changed.");
    }

    /**
     * Services a request to let a logged in event host edit
     * the start and end date/times of an event they've created.
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse editEventDatesByPublicId(EventHost eventHost, EditEventDatesByPublicIdRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        if (foundEvent.getEventStatus() == EventStatus.PUBLISHED)
        {
            throw new EventAlreadyPublishedException("Event dates cannot be changed because the event is already published.");
        }

        if (request.startDateTime().isEqual(request.endDateTime()))
        {
            throw new InvalidRequestException("Event start date/time cannot be the same as the event end date/time.");
        }

        if (request.startDateTime().isAfter(request.endDateTime()))
        {
            throw new InvalidRequestException("Event start date/time cannot come after the event end date/time.");
        }

        Duration eventDuration = Duration.between(request.startDateTime(), request.endDateTime());
        if (eventDuration.toMinutes() < AppConstants.Database.Events.Sizes.MIN_EVENT_DURATION_MINUTES)
        {
            throw new InvalidRequestException
            (
                "Event duration must be at least " +
                AppConstants.Database.Events.Sizes.MIN_EVENT_DURATION_MINUTES +
                " minutes long."
            );
        }

        foundEvent.setStartDateTime(request.startDateTime());
        foundEvent.setEndDateTime(request.endDateTime());
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("Event dates updated.");
    }

    /**
     * Services a request to let a logged in event host change
     * one of their preexisting events into a public event.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse changeEventToPublicEvent(EventHost eventHost, ChangeEventToPublicEventRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        if (foundEvent.getEventStatus() == EventStatus.PUBLISHED)
        {
            throw new EventAlreadyPublishedException("Event cannot be changed into a public event because the event is already published.");
        }

        if (foundEvent.getEventType() == EventType.PUBLIC)
        {
            return new SingleMessageResponse("This event is already a public event.");
        }

        foundEvent.setEventType(EventType.PUBLIC);
        foundEvent.setMaxAttendees(request.maxAttendees());
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("The event has been changed into a public event.");
    }

    /**
     * Services a request to let a logged in event host change
     * one of their preexisting events into a private event.
     * 
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse changeEventToPrivateEvent(EventHost eventHost, ChangeEventToPrivateEventRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        if (foundEvent.getEventStatus() == EventStatus.PUBLISHED)
        {
            throw new EventAlreadyPublishedException("Event cannot be changed into a private event because the event is already published.");
        }

        if (foundEvent.getEventType() == EventType.PRIVATE)
        {
            return new SingleMessageResponse("This event is already a private event.");
        }

        foundEvent.setEventType(EventType.PRIVATE);
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("The event has been changed into a private event.");
    }

    /**
     * Services a request to let a logged in event host change
     * the max number of attendees for a preexisting event.
     * @param eventHost the logged in event host
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse editEventMaxAttendeesByPublicId(EventHost eventHost, EditEventMaxAttendeesByPublicIdRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        if (request.publicId() == null)
        {
            throw new InvalidRequestException("Event public id cannot be null.");
        }

        if (request.publicId().length() > AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH)
        {
            throw new InvalidRequestException
            (
                "Event public id cannot be longer than " +
                AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
                " characters."
            );
        }

        Optional<Event> event = eventRepository.findByPublicId(request.publicId());
        if (event.isEmpty())
        {
            throw new EntityNotFoundException("Could not find an event with the provided public id.");
        }

        Event foundEvent = event.get();

        if (Long.compare(foundEvent.getEventHost().getId(), eventHost.getId()) != 0)
        {
            throw new InvalidCredentialsException("Only the event host who created the event can edit it.");
        }

        if (foundEvent.getEventStatus() == EventStatus.CANCELED)
        {
            throw new EventAlreadyCanceledException("Max number of attendees cannot be changed because the event is already canceled.");
        }

        long currentNumberOfAttendees = ticketRepository.getRegistrationCountByEventId(foundEvent.getId());

        if (request.maxAttendees() <= currentNumberOfAttendees)
        {
            // It is possible to request setting the new max number of attendees
            // less than or equal to the current number of registrations, although
            // this will mean no further registrations will be possible unless
            // the event host decides to later raise the max number of attendees
            // to an even higher number.
            foundEvent.setMaxAttendees(currentNumberOfAttendees);
        }
        else
        {
            foundEvent.setMaxAttendees(request.maxAttendees());
        }
        foundEvent.setLastUpdated(LocalDateTime.now());
        foundEvent = eventRepository.save(foundEvent);

        return new SingleMessageResponse("Max number of attendees has been updated.");
    }
}
