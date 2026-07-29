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

/**
 * BlockedRegistration is an entity representing a record on someone
 * whose registration to an event has been blocked by the event host.
 */
@Entity
@Table(name = AppConstants.Database.BlockedRegistrations.TableNames.TABLE_NAME)
public class BlockedRegistration
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * An attendee can be blocked from multiple events, but each block
     * can only be associated with a single attendee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    /**
     * An event can have multiple registration blocks, but each block
     * can only be associated with a single event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    /**
     * An event host can block multiple registrations, but each block can only be
     * authored by a single event host.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_BLOCKED_BY,
        nullable = false
    )
    private EventHost blockedBy;

    /**
     * Why someone was blocked from an event (optional).
     */
    @Column
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_REASON,
        columnDefinition = AppConstants.Database.BlockedRegistrations.Definitions.COLUMN_REASON
    )
    private String reason;

    /**
     * When the record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.BlockedRegistrations.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * When the registration block was revoked by the event host.
     */
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
        String reason
    )
    {
        this.attendee = attendee;
        this.event = event;
        this.blockedBy = blockedBy;
        this.reason = reason;
        this.created = LocalDateTime.now();
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
    public void setAttendee(Attendee attendee)    { this.attendee = attendee; }
    public void setEvent(Event event)             { this.event = event; }
    public void setBlockedBy(EventHost blockedBy) { this.blockedBy = blockedBy; }
    public void setReason(String reason)          { this.reason = reason; }
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
               ", attendeeId=" + (this.attendee != null ? this.attendee.getId() : null) +
               ", eventId=" + (this.event != null ? this.event.getId() : null) +
               ", active=" + isActive() +
               '}';
 
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private Attendee attendee;
        private Event event;
        private EventHost blockedBy;
        private String reason;

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

        public Builder blockedBy(EventHost eventHost)
        {
            this.blockedBy = eventHost;
            return this;
        }

        public Builder reason(String reason)
        {
            this.reason = reason;
            return this;
        }

        public BlockedRegistration build()
        {
            return new BlockedRegistration(attendee, event, blockedBy, reason);
        }
    }
}