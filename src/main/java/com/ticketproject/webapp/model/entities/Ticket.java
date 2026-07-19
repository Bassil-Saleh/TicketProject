package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.InvitationStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PUBLIC_TOKEN,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.PUBLIC_TOKEN_LENGTH
    )
    private String publicToken;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_TOKEN_IDENTIFIER,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.TOKEN_IDENTIFIER_LENGTH
    )
    private String tokenIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PRESENT,
        nullable = false
    )
    private boolean present;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_INVITATION_STATUS,
        nullable = false
    )
    private InvitationStatus invitationStatus;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Column(name = AppConstants.Database.Tickets.TableNames.COLUMN_DELETED_AT)
    private LocalDateTime deletedAt;

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
    public void setId(Long id)                                         { this.id = id; }
    public void setTicketScan(TicketScan ticketScan)                   { this.ticketScan = ticketScan; }
    public void setPublicToken(String publicToken)                     { this.publicToken = publicToken; }
    public void setTokenIdentifier(String tokenIdentifier)             { this.tokenIdentifier = tokenIdentifier; }
    public void setAttendee(Attendee attendee)                         { this.attendee = attendee; }
    public void setEvent(Event event)                                  { this.event = event; }
    public void setPresent(boolean present)                            { this.present = present; }
    public void setInvitationStatus(InvitationStatus invitationStatus) { this.invitationStatus = invitationStatus; }
    public void setCreated(LocalDateTime created)                      { this.created = created; }
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