package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.CreateEventHostRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventHostResponse;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.repositories.EventHostRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventHostService
{
    private final EventHostRepository eventHostRepository;
    private final BlindIndexService blindIndexService;

    public EventHostService(EventHostRepository eventHostRepository, BlindIndexService blindIndexService)
    {
        this.eventHostRepository = eventHostRepository;
        this.blindIndexService = blindIndexService;
    }

    public CreateEventHostResponse createEventHost(CreateEventHostRequest request)
    {
        // Check if there already exists an event host account
        // using that same email address.
        byte[] emailBlinxIndex = blindIndexService.computeIndex(request.email());
        if (eventHostRepository.existsByEmailIndex(emailBlinxIndex))
        {
            throw new IllegalArgumentException("An account with the provided email address already exists");
        }

        // Check that the user is at least 18 years old before
        // creating a new event host account.
        LocalDate today = LocalDate.now();
        long yearsOld = ChronoUnit.YEARS.between(request.dateOfBirth(), today);
        if (yearsOld < 18)
        {
            throw new IllegalArgumentException("Must be at least 18 years old to create a new account");
        }

        // Create a new EventHost entity.
        // The EventHost constructor should already handle
        // encryption and hashing of sensitive fields.
        EventHost newEventHost = new EventHost.Builder()
            .firstName(request.firstName())
            .middleName(request.middleName())
            .lastName(request.lastName())
            .dateOfBirth(request.dateOfBirth())
            .email(request.email())
            .plaintextPassword(request.password())
            .build();
        String accountVerificationToken = newEventHost.generateVerificationToken();
        // TODO: instead of returning the account verification token
        // directly in the response, call a service to send an email
        // which includes a URL with the account verification token in it.
        //
        // Also, do not commit anything to the database unless
        // the email has been successfully sent.
        newEventHost = eventHostRepository.save(newEventHost);

        String responseMessage = 
            "An account verification link has been sent to your email address. " +
            "Please click it to verify your account. " +
            "The link expires in " +
            AppConstants.DTO.EventHosts.Sizes.VERIFICATION_LINK_DURATION_HOURS +
            "hour" + 
            (AppConstants.DTO.EventHosts.Sizes.VERIFICATION_LINK_DURATION_HOURS > 1 ? "s" : "") +
            ".";

        // Return a response to the user.
        return new CreateEventHostResponse(responseMessage, accountVerificationToken);
    }
}