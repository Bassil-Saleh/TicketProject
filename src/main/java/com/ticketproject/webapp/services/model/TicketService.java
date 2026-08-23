package com.ticketproject.webapp.services.model;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.RespondToInvitationRequest;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.exceptions.TicketGenerationException;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.TicketAlreadyScannedException;
import com.ticketproject.webapp.exceptions.TicketScanFailedException;
import com.ticketproject.webapp.exceptions.EventEndedException;
import com.ticketproject.webapp.exceptions.SigningKeyNotFoundException;
import com.ticketproject.webapp.exceptions.InvalidSignatureException;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.Ticket;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.model.enums.InvitationStatus;
import com.ticketproject.webapp.model.repositories.EventSigningKeyRepository;
import com.ticketproject.webapp.model.repositories.TicketRepository;

/**
 * TicketService is used to generate Ticket entities for attendees.
 */
@Service
@Transactional
public class TicketService
{
    private final TicketRepository ticketRepository;
    private final EventSigningKeyRepository eventSigningKeyRepository;

    public TicketService
    (
        TicketRepository ticketRepository,
        EventSigningKeyRepository eventSigningKeyRepository
    )
    {
        this.ticketRepository = ticketRepository;
        this.eventSigningKeyRepository = eventSigningKeyRepository;
    }

    /**
     * Create a Ticket with a public token generated using
     * the EventSigningKey of the supplied Event.
     * 
     * @return a Ticket entity
     */
    public Ticket createSignedTicket(Event event)
    {
        try
        {
            String tokenIdentifier = UUID.randomUUID().toString();
            LocalDateTime created = LocalDateTime.now();
            String payload =
                tokenIdentifier +
                AppConstants.Crypto.TICKET_PAYLOAD_DECODED_SEPARATOR +
                created.toString();

            Signature signatureBuilder = Signature
                .getInstance(AppConstants.Crypto.TICKET_SIGNATURE_ALGORITHM);
            signatureBuilder.initSign(event.getSigningKey().getPrivateKey());
            signatureBuilder.update(payload.getBytes(StandardCharsets.UTF_8));

            byte[] signature = signatureBuilder.sign();

            String publicToken =
                Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes()) +
                AppConstants.Crypto.TICKET_PAYLOAD_ENCODED_SEPARATOR +
                Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
            
            Ticket signedTicket = new Ticket.Builder()
                .publicToken(publicToken)
                .tokenIdentifier(tokenIdentifier)
                .event(event)
                .invitationStatus(InvitationStatus.PENDING)
                .build();
            
            signedTicket.setPresent(false);
            return signedTicket;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new TicketGenerationException("Cannot generate ticket - signature algorithm not supported");
        }
        catch (InvalidKeyException e)
        {
            throw new TicketGenerationException("Cannot generate ticket - invalid private key");
        }
        catch (SignatureException e)
        {
            throw new TicketGenerationException("Cannot generate ticket - signature generator not initialized properly");
        }
    }

    /**
     * Used to check that a given public token from a digital ticket
     * has a structurally valid payload and contains a valid signature.
     * @param publicToken the public token
     * @returns the Ticket entity associated with the public token
     * @throws InvalidRequestException if the public token is not in
     * a valid format
     * @throws EntityNotFoundException if the public token is not associated
     * with any existing event
     * @throws TicketAlreadyScannedException if the public token has already
     * been used to scan an attendee into an event
     * @throws EventEndedException if the public token is for an event that
     * has already ended
     * @throws SigningKeyNotFoundException if the signing key for the
     * related event could not be found
     * @throws TicketScanFailedException if the signature could not be verified
     * @throws InvalidSignatureException if the signature is either invalid or forged
     */
    public Ticket validateTokenIdentifier(String publicToken)
    throws InvalidRequestException, EntityNotFoundException,
    TicketAlreadyScannedException, EventEndedException,
    SigningKeyNotFoundException, TicketScanFailedException,
    InvalidSignatureException
    {
        String[] tokenParts = publicToken.split(AppConstants.Crypto.TICKET_PAYLOAD_ENCODED_SEPARATOR_REGEX);
        if (tokenParts.length != 2)
        {
            throw new InvalidRequestException("Scanned ticket is in an invalid format.");
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(tokenParts[0]);
        byte[] signatureBytes = Base64.getUrlDecoder().decode(tokenParts[1]);

        String payload = new String(payloadBytes);
        String tokenIdentifier = payload
            .split(AppConstants.Crypto.TICKET_PAYLOAD_DECODED_SEPARATOR_REGEX)[0];
        
        Optional<Ticket> ticket = ticketRepository.findByTokenIdentifier(tokenIdentifier);

        if (ticket.isEmpty())
        {
            throw new EntityNotFoundException("The scanned ticket is not associated with any existing event.");
        }

        Ticket foundTicket = ticket.get();

        if (foundTicket.isPresent())
        {
            throw new TicketAlreadyScannedException("The ticket has already been scanned.");
        }

        Event foundEvent = foundTicket.getEvent();

        if (foundEvent.getEndDateTime().isBefore(LocalDateTime.now()))
        {
            throw new EventEndedException("The event which this ticket is for has already ended.");
        }

        Optional<EventSigningKey> signingKeys = eventSigningKeyRepository.findByEventId(foundEvent.getId());
        if (signingKeys.isEmpty())
        {
            throw new SigningKeyNotFoundException("Could not verify scanned ticket. Please try again later.");
        }

        EventSigningKey foundSigningKeys = signingKeys.get();

        boolean isValidSignature = false;
        try
        {
            Signature signatureInstance = Signature.getInstance(AppConstants.Crypto.TICKET_SIGNATURE_ALGORITHM);
            signatureInstance.initVerify(foundSigningKeys.getPublicKey());
            signatureInstance.update(payloadBytes);
            isValidSignature = signatureInstance.verify(signatureBytes);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
        {
            throw new TicketScanFailedException("Could not scan ticket.");
        }
        if (!isValidSignature)
        {
            throw new InvalidSignatureException("Signature of scanned ticket is either invalid or forged.");
        }

        return foundTicket;
    }

    /**
     * Services a request to respond to an invitation to a private event.
     * @param request the request body
     * @return a SingleMessageResponse on success
     */
    public SingleMessageResponse respondToInvitation(RespondToInvitationRequest request)
    {
        Ticket foundTicket;
        try
        {
            foundTicket = validateTokenIdentifier(request.publicToken());
        }
        catch
        (
            InvalidRequestException |
            EntityNotFoundException |
            TicketAlreadyScannedException |
            EventEndedException |
            SigningKeyNotFoundException |
            TicketScanFailedException e
        )
        {
            throw e;
        }

        // I don't think it makes sense to change an invitation status back into PENDING,
        // since that's the state that invitations are initialized to.
        if (request.invitationResponse() == InvitationStatus.PENDING)
        {
            throw new InvalidRequestException("Cannot respond to an invitation with PENDING.");
        }

        // TODO: Send an email to the event host based on the
        // invitation response (accepted or rejected).
        // - If the invitation response includes a message,
        // then include it in the email sent to the event host.
        // - If the invitation response is the same as its
        // previous state, then no email should be sent.

        foundTicket.setInvitationStatus(request.invitationResponse());
        foundTicket.setLastUpdated(LocalDateTime.now());

        ticketRepository.save(foundTicket);

        return new SingleMessageResponse("Responded to invitation with response: " + request.invitationResponse());
    }
}