package com.ticketproject.webapp.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.dtos.requests.CreatePasswordResetTokenRequest;
import com.ticketproject.webapp.dtos.responses.CreatePasswordResetTokenResponse;
import com.ticketproject.webapp.model.repositories.PasswordResetTokenRepository;
import com.ticketproject.webapp.model.entities.PasswordResetToken;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.repositories.EventHostRepository;

/**
 * PasswordResetTokenService is a service used by controllers to handle API route requests
 * involving PasswordResetToken entities (i.e. "Forgot Password?" link).
 */
@Service
@Transactional
public class PasswordResetTokenService
{
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EventHostRepository eventHostRepository;
    private final EmailService emailService;
    private final BlindIndexService blindIndexService;

    public PasswordResetTokenService
    (
        PasswordResetTokenRepository passwordResetTokenRepository,
        EventHostRepository eventHostRepository,
        EmailService emailService,
        BlindIndexService blindIndexService
    )
    {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.eventHostRepository = eventHostRepository;
        this.emailService = emailService;
        this.blindIndexService = blindIndexService;
    }

    /**
     * Service a request to reset the password of an event host account by
     * creating a password reset token and emailing it to the address
     * specified in the request.
     * 
     * @param request the request body
     * @return a CreatePasswordResetTokenResponse on success
     * @throws EmailSendFailedException if the email cannot be sent for whatever reason
     */
    public CreatePasswordResetTokenResponse createPasswordResetToken(CreatePasswordResetTokenRequest request)
    {
        // Check if there exists an event host account with the provided email address.
        byte[] emailBlindIndex = blindIndexService.computeIndex(request.email());
        Optional<EventHost> foundEventHost = eventHostRepository.findByEmailIndex(emailBlindIndex);

        if (foundEventHost.isEmpty())
        {
            return new CreatePasswordResetTokenResponse
            ("If an account with the provided email address exists, a password reset token will be sent to that address.");
        }

        EventHost eventHost = foundEventHost.get();

        PasswordResetToken passwordResetToken = new PasswordResetToken.Builder()
            .tokenHash(new byte[]{})
            .eventHost(eventHost)
            .build();
        String rawToken = passwordResetToken.generateToken();

        // Don't persist a PasswordResetToken entity to the database unless the email has been successfully sent.
        emailService.sendPasswordResetEmail(request.email(), rawToken);

        // At this point, the email should have been sent successfully.
        passwordResetToken = passwordResetTokenRepository.save(passwordResetToken);

        return new CreatePasswordResetTokenResponse
        ("If an account with the provided email address exists, a password reset token will be sent to that address.");
    }
}
