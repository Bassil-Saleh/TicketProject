package com.ticketproject.webapp.services.model;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Event;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.enums.InvitationStatus;

/**
 * TicketService is used to generate Ticket entities for attendees.
 */
@Service
@Transactional
public class TicketService
{
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
            throw new RuntimeException("Cannot generate ticket - signature algorithm not supported");
        }
        catch (InvalidKeyException e)
        {
            throw new RuntimeException("Cannot generate ticket - invalid private key");
        }
        catch (SignatureException e)
        {
            throw new RuntimeException("Cannot generate ticket - signature generator not initialized properly");
        }
    }
}