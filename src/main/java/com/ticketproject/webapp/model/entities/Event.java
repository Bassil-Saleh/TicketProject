package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import java.util.Objects;

/**
 * Event is an entity representing a record on either a public event
 * (where end users register through a public page for the event) or
 * a private event (where the event host manually invites people of
 * their choice).
 */
@Entity
@Table
(
    name = AppConstants.Database.Events.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.Events.TableNames.COLUMN_PUBLIC_ID)
    }
)
public class Event
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Used for constructing a URL to a page where attendees can view
     * info about the event. Note that before an event is published by
     * the event host, it is possible for the event host to change
     * a public event to a private event (or vice versa).
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_PUBLIC_ID,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH
    )
    private String publicId;

    /**
     * A single event host can create many events, but each event can
     * only be authored by a single event host.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    /**
     * When the record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * When the record was last updated.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    /**
     * The event's name.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_NAME,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.NAME_LENGTH
    )
    private String name;

    /**
     * The event's description.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_DESCRIPTION,
        nullable = false,
        columnDefinition = AppConstants.Database.Events.Definitions.COLUMN_DESCRIPTION
    )
    private String description;

    /**
     * The event's address.
     */
    @OneToOne
    (
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_ADDRESS_ID,
        nullable = false
    )
    private EventAddress eventAddress;

    /**
     * When the event begins.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_START_DATE_TIME,
        nullable = false
    )
    private LocalDateTime startDateTime;


    /**
     * When the event ends.
     */
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_END_DATE_TIME,
        nullable = false
    )
    private LocalDateTime endDateTime;

    /**
     * The event's type (i.e. public or private).
     */
    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_TYPE,
        nullable = false
    )
    private EventType eventType;

    /**
     * The maximum number of attendees which can register for a public event.
     * Note that this can be null since the number of attendees for
     * a private event are determined by how many people the event host invites.
     */
    @Column(name = AppConstants.Database.Events.TableNames.COLUMN_MAX_ATTENDEES)
    private Integer maxAttendees;

    /**
     * Whether or not people can still register for the event.
     */
    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_REGISTRATION_STATUS,
        nullable = false
    )
    private RegistrationStatus registrationStatus;

    /**
     * The event's status (draft, published, canceled).
     * 
     * When an event is in the draft status, it is still possible to change all of its details,
     * including whether it is a public or private event.
     * 
     * When an event is in the published status, it cannot be changed from public to private
     * (or vice versa), but it is still possible to edit other details about the event.
     * 
     * When an event is in the canceled status, all tickets/invitations for said event
     * should no longer work, and the event should not be changeable either.
     */
    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_STATUS,
        nullable = false
    )
    private EventStatus eventStatus;

    /**
     * There can be many tickets for a single event, but each ticket
     * can only be used for a single event.
     */
    @OneToMany(mappedBy = AppConstants.Database.Events.MappedByNames.MAPPED_BY_EVENT)
    private Set<Ticket> tickets = new HashSet<>();

    /**
     * A single event can have many registration blocks, but each block
     * can only be associated with a single event.
     */
    @OneToMany(mappedBy = AppConstants.Database.Events.MappedByNames.MAPPED_BY_EVENT)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

    /**
     * Consists of a key pair (a public key and a private key) used to
     * authenticate tickets for an event.
     * 
     * Each event must have a single key pair, and each key pair
     * must belong to only a single event.
     */
    @OneToOne
    (
        mappedBy = AppConstants.Database.Events.MappedByNames.MAPPED_BY_EVENT,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private EventSigningKey signingKey;

    // ************************************************
    // Constructors
    // ************************************************
    public Event() {}

    public Event
    (
        String publicId,
        EventHost eventHost,
        String name,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        EventType eventType,
        Integer maxAttendees
    )
    {
        this.publicId = publicId;
        this.eventHost = eventHost;
        this.name = name;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.eventType = eventType;
        this.maxAttendees = maxAttendees;
        this.created = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                                       { return this.id; }
    public String getPublicId()                               { return this.publicId; }
    public EventHost getEventHost()                           { return this.eventHost; }
    public LocalDateTime getCreated()                         { return this.created; }
    public LocalDateTime getLastUpdated()                     { return this.lastUpdated; }
    public String getName()                                   { return this.name; }
    public String getDescription()                            { return this.description; }
    public EventAddress getEventAddress()                     { return this.eventAddress; }
    public LocalDateTime getStartDateTime()                   { return this.startDateTime; }
    public LocalDateTime getEndDateTime()                     { return this.endDateTime; }
    public EventType getEventType()                           { return this.eventType; }
    public Integer getMaxAttendees()                          { return this.maxAttendees; }
    public RegistrationStatus getRegistrationStatus()         { return this.registrationStatus; }
    public EventStatus getEventStatus()                       { return this.eventStatus; }
    public Set<Ticket> getTickets()                           { return this.tickets; }
    public Set<BlockedRegistration> getBlockedRegistrations() { return this.blockedRegistrations; }
    public EventSigningKey getSigningKey()                    { return this.signingKey; }

    // ************************************************
    // Setters
    // ************************************************
    public void setPublicId(String publicId)                                           { this.publicId = publicId; }
    public void setEventHost(EventHost eventHost)                                      { this.eventHost = eventHost; }
    public void setLastUpdated(LocalDateTime lastUpdated)                              { this.lastUpdated = lastUpdated; }
    public void setName(String name)                                                   { this.name = name; }
    public void setDescription(String description)                                     { this.description = description; }
    public void setEventAddress(EventAddress eventAddress)                             { this.eventAddress = eventAddress; }
    public void setStartDateTime(LocalDateTime startDateTime)                          { this.startDateTime = startDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime)                              { this.endDateTime = endDateTime; }
    public void setEventType(EventType eventType)                                      { this.eventType = eventType; }
    public void setMaxAttendees(Integer maxAttendees)                                  { this.maxAttendees = maxAttendees; }
    public void setRegistrationStatus(RegistrationStatus registrationStatus)           { this.registrationStatus = registrationStatus; }
    public void setEventStatus(EventStatus status)                                     { this.eventStatus = status; }
    public void setTickets(Set<Ticket> tickets)                                        { this.tickets = tickets; }
    public void setBlockedRegistrations(Set<BlockedRegistration> blockedRegistrations) { this.blockedRegistrations = blockedRegistrations; }
    public void setSigningKey(EventSigningKey signingKey)                              { this.signingKey = signingKey; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof Event that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "Event{" +
               "id=" + this.id +
               ", publicId='" + this.publicId + '\'' +
               ", name='" + this.name + '\'' +
               ", eventType=" + this.eventType +
               ", eventStatus=" + this.eventStatus +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private String publicId;
        private EventHost eventHost;
        private String name;
        private String description;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private EventType eventType;
        private Integer maxAttendees;

        public Builder publicId(String publicId)
        {
            this.publicId = publicId;
            return this;
        }

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder startDateTime(LocalDateTime startDateTime)
        {
            this.startDateTime = startDateTime;
            return this;
        }

        public Builder endDateTime(LocalDateTime endDateTime)
        {
            this.endDateTime = endDateTime;
            return this;
        }

        public Builder eventType(EventType eventType)
        {
            this.eventType = eventType;
            return this;
        }

        public Builder maxAttendees(Integer maxAttendees)
        {
            this.maxAttendees = maxAttendees;
            return this;
        }

        public Event build()
        {
            return new Event
            (
                publicId,
                eventHost,
                name,
                description,
                startDateTime,
                endDateTime,
                eventType,
                maxAttendees
            );
        }
    }
}