package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AddressBookContact is an entity representing a record on someone
 * which an event host has previously invited to a private event.
 * 
 * It is meant to provide event hosts a way to look up past invitees
 * for convenience, so that if the event host wants to invite the
 * same people to different events, the event host does not need to
 * manually re-type those people's information over and over again.
 */
@Entity
@Table
(
    name = AppConstants.Database.AddressBookContacts.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (
            columnNames =
            {
                AppConstants.Database.AddressBookContacts.TableNames.COLUMN_ATTENDEE_ID,
                AppConstants.Database.AddressBookContacts.TableNames.COLUMN_EVENT_HOST_ID
            }
        )
    }
)
public class AddressBookContact
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * It is possible for an attendee to be associated with many different address book records
     * (i.e. if multiple different hosts invite the same person to their own private events),
     * but every address book record should only be associated with a single attendee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    /**
     * It is possible for an event host's address book to have many address book records,
     * but each address book record must only belong to a single event host.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    /**
     * When the address book record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;
    
    // ************************************************
    // Constructors
    // ************************************************
    public AddressBookContact() {}

    public AddressBookContact(Attendee attendee, EventHost eventHost)
    {
        this.attendee = attendee;
        this.eventHost = eventHost;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public Attendee getAttendee()     { return this.attendee; }
    public EventHost getEventHost()   { return this.eventHost; }
    public LocalDateTime getCreated() { return this.created; }

    // ************************************************
    // Setters
    // ************************************************
    public void setAttendee(Attendee attendee)    { this.attendee = attendee; }
    public void setEventHost(EventHost eventHost) { this.eventHost = eventHost; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof AddressBookContact that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "AddressBookContact{" +
               "id=" + this.id +
               ", attendeeId=" + (this.attendee != null ? this.attendee.getId() : null) +
               ", eventHostId=" + (this.eventHost != null ? this.eventHost.getId() : null) +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private Attendee attendee;
        private EventHost eventHost;

        public Builder attendee(Attendee attendee)
        {
            this.attendee = attendee;
            return this;
        }

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
            return this;
        }

        public AddressBookContact build()
        {
            return new AddressBookContact(attendee, eventHost);
        }
    }
}