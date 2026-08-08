package com.ticketproject.webapp.services.model;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.dtos.requests.ScanTicketRequest;
import com.ticketproject.webapp.dtos.responses.GetScannedTicketsByEventHostResponse;
import com.ticketproject.webapp.dtos.responses.ScannedTicketInfo;
import com.ticketproject.webapp.dtos.responses.SingleMessageResponse;
import com.ticketproject.webapp.exceptions.EventEndedException;
import com.ticketproject.webapp.exceptions.InvalidRequestException;
import com.ticketproject.webapp.exceptions.InvalidSignatureException;
import com.ticketproject.webapp.exceptions.SigningKeyNotFoundException;
import com.ticketproject.webapp.exceptions.TicketAlreadyScannedException;
import com.ticketproject.webapp.exceptions.TicketScanFailedException;
import com.ticketproject.webapp.exceptions.UnauthorizedException;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.Ticket;
import com.ticketproject.webapp.model.entities.TicketScan;
import com.ticketproject.webapp.model.repositories.EventSigningKeyRepository;
import com.ticketproject.webapp.model.repositories.TicketRepository;
import com.ticketproject.webapp.model.repositories.TicketScanRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

@Service
@Transactional
public class TicketScanService
{
    private final TicketScanRepository ticketScanRepository;
    private final TicketRepository ticketRepository;
    private final EventSigningKeyRepository eventSigningKeyRepository;

    public TicketScanService
    (
        TicketScanRepository ticketScanRepository,
        TicketRepository ticketRepository,
        EventSigningKeyRepository eventSigningKeyRepository
    )
    {
        this.ticketScanRepository = ticketScanRepository;
        this.ticketRepository = ticketRepository;
        this.eventSigningKeyRepository = eventSigningKeyRepository;
    }

    public SingleMessageResponse scanTicket(EventHost eventHost, ScanTicketRequest request)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        String[] tokenParts = request.publicToken().split(AppConstants.Crypto.TICKET_PAYLOAD_ENCODED_SEPARATOR_REGEX);
        if (tokenParts.length != 2)
        {
            throw new InvalidRequestException("Scanned ticket is in an invalid format.");
        }

        LocalDateTime scannedAt = LocalDateTime.now();

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

        try
        {
            Signature signatureInstance = Signature.getInstance(AppConstants.Crypto.TICKET_SIGNATURE_ALGORITHM);
            signatureInstance.initVerify(foundSigningKeys.getPublicKey());
            signatureInstance.update(payloadBytes);
            boolean isValidSignature = signatureInstance.verify(signatureBytes);

            if (!isValidSignature)
            {
                throw new InvalidSignatureException("Signature of scanned ticket is either invalid or forged.");
            }

            foundTicket.setPresent(true);

            TicketScan ticketScan = new TicketScan.Builder()
                .ticket(foundTicket)
                .scannedAt(scannedAt)
                .scannedBy(eventHost)
                .build();
            
            ticketScanRepository.save(ticketScan);

            return new SingleMessageResponse("Your ticket has been scanned.");
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
        {
            throw new TicketScanFailedException("Could not scan ticket");
        }
        catch (InvalidSignatureException e)
        {
            throw new InvalidSignatureException("Signature of scanned ticket is invalid or forged.");
        }
    }

    public GetScannedTicketsByEventHostResponse getScannedTicketsByEventHost(EventHost eventHost)
    {
        if (eventHost == null)
        {
            throw new UnauthorizedException("Authentication required");
        }

        List<TicketScan> scannedTickets = ticketScanRepository.findAllByEventHostId(eventHost.getId());

        List<ScannedTicketInfo> scannedTicketInfo = scannedTickets
            .stream()
            .map
            (ticket ->
            {
                ScannedTicketInfo record = new ScannedTicketInfo
                (
                    ticket.getScannedAt(),
                    ticket.getTicket().getAttendee().getFirstName(),
                    ticket.getTicket().getAttendee().getMiddleName(),
                    ticket.getTicket().getAttendee().getLastName(),
                    ticket.getTicket().getAttendee().getEmail(),
                    ticket.getTicket().getEvent().getName(),
                    ticket.getTicket().getEvent().getDescription(),
                    ticket.getTicket().getEvent().getStartDateTime(),
                    ticket.getTicket().getEvent().getEndDateTime()
                );
                return record;
            })
            .toList();

        return new GetScannedTicketsByEventHostResponse(scannedTicketInfo);
    }
}
