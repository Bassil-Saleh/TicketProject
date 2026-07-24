package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.InvitationStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ticket is an entity representing a record that stores info
 * on a digital ticket for use by an attendee.
 * 
 * The ticket can either be for a public event which the attendee registered for,
 * or a private event which the attendee was invited to.
 */
@Entity
@Table
(
    name = AppConstants.Database.Tickets.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (
            columnNames =
            {
                AppConstants.Database.Tickets.TableNames.COLUMN_ATTENDEE_ID,
                AppConstants.Database.Tickets.TableNames.COLUMN_EVENT_ID
            }
        ),
        @UniqueConstraint
        (columnNames = AppConstants.Database.Tickets.TableNames.COLUMN_PUBLIC_TOKEN),
        @UniqueConstraint
        (columnNames = AppConstants.Database.Tickets.TableNames.COLUMN_TOKEN_IDENTIFIER),
    }
)
public class Ticket
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A value which gets encoded into a QR code for the attendee's ticket.
     * This gets scanned by the Android app and is used to check in the attendee.
     */
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PUBLIC_TOKEN,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.PUBLIC_TOKEN_LENGTH
    )
    private String publicToken;

    /**
     * When the application's back-end receives the public token scanned from a QR code,
     * it extracts an identifier from the payload and compares it to the identifier in the database.
     */
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_TOKEN_IDENTIFIER,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.TOKEN_IDENTIFIER_LENGTH
    )
    private String tokenIdentifier;

    /**
     * An attendee can have many tickets, but each ticket can only belong to a single attendee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    /**
     * An event can have many tickets, but each ticket can only belong to a single event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    /**
     * Whether or not the scanned ticket has been successfully validated
     * (in other words, whether the attendee is present).
     */
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PRESENT,
        nullable = false
    )
    private boolean present;

    /**
     * The attendee's response to their invitation for a private event
     * (pending, accepted, rejected).
     */
    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_INVITATION_STATUS,
        nullable = false
    )
    private InvitationStatus invitationStatus;

    /**
     * When the record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * When the record was last updated.
     */
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    /**
     * When the ticket was soft-deleted (meaning the ticket is no longer active
     * or usable, but its information remains in the database to allow for bookkeeping).
     */
    @Column(name = AppConstants.Database.Tickets.TableNames.COLUMN_DELETED_AT)
    private LocalDateTime deletedAt;

    /**
     * When a ticket gets scanned, it can only be associated with a single ticket scan record.
     * Also, every single ticket scan record can only be associated with a single ticket.
     */
    @OneToOne(mappedBy = AppConstants.Database.Tickets.MappedByNames.MAPPED_BY_TICKET)
    private TicketScan ticketScan;
    // ************************************************
    // Constructors
    // ************************************************
    public Ticket() {}

    public Ticket
    (
        String publicToken,
        String tokenIdentifier,
        Attendee attendee,
        Event event,
        InvitationStatus invitationStatus
    )
    {
        this.publicToken = publicToken;
        this.tokenIdentifier = tokenIdentifier;
        this.attendee = attendee;
        this.event = event;
        this.invitationStatus = invitationStatus;
        this.present = false;
        this.created = LocalDateTime.now();
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                           { return this.id; }
    public String getPublicToken()                { return this.publicToken; }
    public String getTokenIdentifier()            { return this.tokenIdentifier; }
    public Attendee getAttendee()                 { return this.attendee; }
    public Event getEvent()                       { return this.event; }
    public boolean isPresent()                    { return this.present; }
    public InvitationStatus getInvitationStatus() { return this.invitationStatus; }
    public LocalDateTime getCreated()             { return this.created; }
    public LocalDateTime getLastUpdated()         { return this.lastUpdated; }
    public LocalDateTime getDeletedAt()           { return this.deletedAt; }
    public TicketScan getTicketScan()             { return this.ticketScan; }

    // ************************************************
    // Setters
    // ************************************************
    public void setTicketScan(TicketScan ticketScan)                   { this.ticketScan = ticketScan; }
    public void setPublicToken(String publicToken)                     { this.publicToken = publicToken; }
    public void setTokenIdentifier(String tokenIdentifier)             { this.tokenIdentifier = tokenIdentifier; }
    public void setAttendee(Attendee attendee)                         { this.attendee = attendee; }
    public void setEvent(Event event)                                  { this.event = event; }
    public void setPresent(boolean present)                            { this.present = present; }
    public void setInvitationStatus(InvitationStatus invitationStatus) { this.invitationStatus = invitationStatus; }
    public void setLastUpdated(LocalDateTime lastUpdated)              { this.lastUpdated = lastUpdated; }
    public void setDeletedAt(LocalDateTime deletedAt)                  { this.deletedAt = deletedAt; }

    // ************************************************
    // Convenience methods
    // ************************************************
    public boolean isDeleted() { return this.deletedAt != null; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof Ticket that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "Ticket{" +
               "id=" + this.id +
               ", tokenIdentifier='" + this.tokenIdentifier + '\'' +
               ", attendeeId=" + (this.attendee != null ? this.attendee.getId() : null) +
               ", event=" + (this.event != null ? this.event.getName() : null) +
               ", present=" + this.present +
               ", invitationStatus=" + this.invitationStatus +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private String publicToken;
        private String tokenIdentifier;
        private Attendee attendee;
        private Event event;
        private InvitationStatus invitationStatus;

        public Builder publicToken(String publicToken)
        {
            this.publicToken = publicToken;
            return this;
        }

        public Builder tokenIdentifier(String tokenIdentifier)
        {
            this.tokenIdentifier = tokenIdentifier;
            return this;
        }

        public Builder attendee(Attendee attendee)
        {
            this.attendee = attendee;
            return this;
        }

        public Builder event(Event event)
        {
            this.event = event;
            return this;
        }

        public Builder invitationStatus(InvitationStatus invitationStatus)
        {
            this.invitationStatus = invitationStatus;
            return this;
        }

        public Ticket build()
        {
            return new Ticket
            (
                publicToken,
                tokenIdentifier,
                attendee,
                event,
                invitationStatus
            );
        }
    }
}