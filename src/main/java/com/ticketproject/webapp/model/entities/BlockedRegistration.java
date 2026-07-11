package com.ticketproject.webapp.model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = AppConstants.Database.BlockedRegistrations.TableNames.TABLE_NAME)
public class BlockedRegistration
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_BLOCKED_BY,
        nullable = false
    )
    private EventHost blockedBy;

    @Column
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_REASON,
        columnDefinition = AppConstants.Database.BlockedRegistrations.Definitions.COLUMN_REASON
    )
    private String reason;

    @Column
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_REVOKED)
    private LocalDateTime revoked;

    // ************************************************
    // Constructors
    // ************************************************
    public BlockedRegistration() {}

    public BlockedRegistration
    (
        Attendee attendee,
        Event event,
        EventHost blockedBy,
        String reason,
        LocalDateTime created
    )
    {
        this.attendee = attendee;
        this.event = event;
        this.blockedBy = blockedBy;
        this.reason = reason;
        this.created = created;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public Attendee getAttendee()     { return this.attendee; }
    public Event getEvent()           { return this.event; }
    public EventHost getBlockedBy()   { return this.blockedBy; }
    public String getReason()         { return this.reason; }
    public LocalDateTime getCreated() { return this.created; }
    public LocalDateTime getRevoked() { return this.revoked; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                    { this.id = id; }
    public void setAttendee(Attendee attendee)    { this.attendee = attendee; }
    public void setEvent(Event event)             { this.event = event; }
    public void setBlockedBy(EventHost blockedBy) { this.blockedBy = blockedBy; }
    public void setReason(String reason)          { this.reason = reason; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public void setRevoked(LocalDateTime revoked) { this.revoked = revoked; }

    // ************************************************
    // Convenience methods
    // ************************************************
    public boolean isActive() { return this.revoked == null; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof BlockedRegistration that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "BlockedRegistration{" +
               "id=" + this.id +
               ", attendee=" + (this.attendee != null ? this.attendee.getEmail() : null) +
               ", event=" + (this.event != null ? this.event.getName() : null) +
               ", active=" + isActive() +
               '}';
 
    }

    // ************************************************
    // TODO: Builder (to make entity creation easier)
    // ************************************************
}