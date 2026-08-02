package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.CreateEventHostRequest;
import com.ticketproject.webapp.dtos.requests.VerifyEventHostRequest;
import com.ticketproject.webapp.dtos.responses.CreateEventHostResponse;
import com.ticketproject.webapp.dtos.responses.VerifyEventHostResponse;
import com.ticketproject.webapp.exceptions.EventHostEmailAlreadyExistsException;
import com.ticketproject.webapp.exceptions.EventHostToVerifyNotFoundException;
import com.ticketproject.webapp.exceptions.EventHostUnderageException;
import com.ticketproject.webapp.exceptions.EventHostVerificationPeriodExpiredException;
import com.ticketproject.webapp.exceptions.EventHostInactiveException;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.repositories.EventHostRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EventHostService is a service used by controllers to handle API route requests
 * involving EventHost entities (i.e. new account creation).
 */
@Service
@Transactional
public class EventHostService
{
    private final EventHostRepository eventHostRepository;
    private final BlindIndexService blindIndexService;
    private final HashingService hashingService;

    public EventHostService(EventHostRepository eventHostRepository, BlindIndexService blindIndexService, HashingService hashingService)
    {
        this.eventHostRepository = eventHostRepository;
        this.blindIndexService = blindIndexService;
        this.hashingService = hashingService;
    }

    /**
     * Service a request to create a new event host account.
     * @param request request containing info to create a new event host account
     * @return a CreateEventHostResponse on success, an ErrorResponse on failure
     * @throws EventHostEmailAlreadyExistsException if the request contains
     * an email address already used by a preexisting event host account
     * @throws EventHostUnderageException if the request contains a date of birth
     * less than 18 years old
     */
    public CreateEventHostResponse createEventHost(CreateEventHostRequest request)
    {
        // Check if there already exists an event host account
        // using that same email address.
        byte[] emailBlinxIndex = blindIndexService.computeIndex(request.email());
        if (eventHostRepository.existsByEmailIndex(emailBlinxIndex))
        {
            throw new EventHostEmailAlreadyExistsException("An account with the provided email address already exists");
        }

        // Check that the user is at least 18 years old before
        // creating a new event host account.
        LocalDate today = LocalDate.now();
        long yearsOld = ChronoUnit.YEARS.between(request.dateOfBirth(), today);
        if (yearsOld < 18)
        {
            throw new EventHostUnderageException("Must be at least 18 years old to create a new account");
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
            " hour" + 
            (AppConstants.DTO.EventHosts.Sizes.VERIFICATION_LINK_DURATION_HOURS > 1 ? "s" : "") +
            ".";

        // Return a response to the user.
        return new CreateEventHostResponse(responseMessage, accountVerificationToken);
    }

    /**
     * Service a request to verify an event host account.
     * @param request request containing a verification token to verify an account
     * @return a VerifyEventHostResponse on success, an ErrorResponse on failure
     * @throws EventHostToVerifyNotFoundException if no existing event host account
     * using the verification token included in the request was found
     * @throws EventHostVerificationPeriodExpiredException if the verification token
     * included in the request has expired
     * @throws EventHostInactiveException if the event host account associated
     * with the verification token is currently inactive
     */
    public VerifyEventHostResponse verifyEventHost(VerifyEventHostRequest request)
    {
        // Hash the verification token and search for an EventHost with a matching verification token hash.
        byte[] hashedVerificationToken = hashingService.hashToken(request.verificationToken());
        Optional<EventHost> foundEventHost = eventHostRepository.findByVerificationKeyHash(hashedVerificationToken);
        if (foundEventHost.isEmpty())
        {
            throw new EventHostToVerifyNotFoundException("No existing event host account using that verification token was found");
        }

        EventHost eventHostToVerify = foundEventHost.get();

        // Check whether the verification token has already expired.
        if (!eventHostToVerify.isVerified() && LocalDateTime.now().isAfter(eventHostToVerify.getVerificationExpires()))
        {
            throw new EventHostVerificationPeriodExpiredException("The verification token has expired");
        }

        // Check whether the event host account is inactive.
        if (!eventHostToVerify.isActive())
        {
            throw new EventHostInactiveException("The event host account is no longer active");
        }

        // Check whether the account is already verified.
        if (eventHostToVerify.isVerified())
        {
            return new VerifyEventHostResponse("Your account has already been verified.");
        }

        // Mark the account as verified.
        eventHostToVerify.setVerified(true);
        eventHostToVerify.setLastUpdated(LocalDateTime.now());
        eventHostRepository.save(eventHostToVerify);

        return new VerifyEventHostResponse("Your account has been successfully verified.");
    }
}