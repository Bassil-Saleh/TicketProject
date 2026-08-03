package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.LoginSessionRequest;
import com.ticketproject.webapp.dtos.responses.LoginSessionResponse;
import com.ticketproject.webapp.exceptions.AccountNotVerifiedException;
import com.ticketproject.webapp.exceptions.EventHostInactiveException;
import com.ticketproject.webapp.exceptions.InvalidCredentialsException;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.model.enums.ClientType;
import com.ticketproject.webapp.model.repositories.EventHostRepository;
import com.ticketproject.webapp.model.repositories.SessionRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SessionService is a service used by controllers to handle API route requests
 * involving Session entities (i.e. login and logout).
 */
@Service
@Transactional
public class SessionService
{
    private final SessionRepository sessionRepository;
    private final EventHostRepository eventHostRepository;
    private final BlindIndexService blindIndexService;
    private final HashingService hashingService;
    private final JwtService jwtService;

    /**
     * Constructs a new SessionService with the required dependencies.
     * @param sessionRepository the repository for Session entities
     * @param eventHostRepository the repository for EventHost entities
     * @param blindIndexService the service for computing blind indexes
     * @param hashingService the service for hashing and verifying tokens/passwords
     * @param jwtService the service for generating and validating JWTs
     */
    public SessionService
    (
        SessionRepository sessionRepository,
        EventHostRepository eventHostRepository,
        BlindIndexService blindIndexService,
        HashingService hashingService,
        JwtService jwtService
    )
    {
        this.sessionRepository = sessionRepository;
        this.eventHostRepository = eventHostRepository;
        this.blindIndexService = blindIndexService;
        this.hashingService = hashingService;
        this.jwtService = jwtService;
    }

    /**
     * Handle a login request by authenticating the event host and creating a new session.
     * 
     * This method:
     * 1. Looks up the EventHost by email (using blind index)
     * 2. Verifies the password against the stored bcrypt hash
     * 3. Checks that the account is active and verified
     * 4. Creates a new Session entity with a generated token
     * 5. Saves the Session to the database
     * 6. Generates a signed JWT containing the raw session token
     * 7. Returns the JWT to the caller
     * 
     * @param request the login request containing email and password
     * @return a LoginSessionResponse containing the signed JWT
     * @throws InvalidCredentialsException if the email or password is incorrect
     * @throws EventHostInactiveException if the event host's account is inactive
     * @throws AccountNotVerifiedException if the event host's account is not verified
     */
    public LoginSessionResponse login(LoginSessionRequest request)
    {
        // Look up the EventHost by email blind index.
        byte[] emailBlindIndex = blindIndexService.computeIndex(request.email());
        Optional<EventHost> foundEventHost = eventHostRepository.findByEmailIndex(emailBlindIndex);
        if (foundEventHost.isEmpty())
        {
            throw new InvalidCredentialsException("Invalid email address or password");
        }

        EventHost eventHost = foundEventHost.get();

        // Verify the password against the stored bcrypt hash.
        if (!hashingService.verifyPassword(request.password(), eventHost.getPasswordHash()))
        {
            throw new InvalidCredentialsException("Invalid email address or password");
        }

        // Check that the account is active.
        if (!eventHost.isActive())
        {
            throw new EventHostInactiveException("This account is no longer active");
        }

        // Check that the account has been verified.
        if (!eventHost.isVerified())
        {
            throw new AccountNotVerifiedException("Please verify your account before logging in");
        }

        // Create a new Session entity.
        Session session = new Session.Builder()
            .eventHost(eventHost)
            .clientType(ClientType.WEB)
            .build();

        // Generate the raw session token and store its hash in the Session entity.
        String rawSessionToken = session.generateToken();

        // Save the Session to the database.
        session = sessionRepository.save(session);

        // Generate a signed JWT containing the raw session token.
        String jwt = jwtService.generateToken(rawSessionToken, session.getExpires());

        return new LoginSessionResponse(jwt);
    }
}