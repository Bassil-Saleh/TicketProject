package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    @Column
    (
        name = AppConstants.Database.AddressBookContacts.TableNames.COLUMN_CREATED,
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
    // Is a setter for the record's ID really necessary?
    // I don't expect a record's ID to change at all once it's created.
    public void setId(Long id)                    { this.id = id; }
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
               ", attendee=" + (this.attendee != null ? this.attendee.getEmail() : null) +
               ", eventHost=" + (this.eventHost != null ? this.eventHost.getEmail() : null) +
               '}';
    }

    // ************************************************
    // TODO: Builder (to make entity creation easier)
    // ************************************************
}