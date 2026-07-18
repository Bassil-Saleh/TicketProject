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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_PUBLIC_ID,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH
    )
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_NAME,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.NAME_LENGTH
    )
    private String name;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_DESCRIPTION,
        nullable = false,
        columnDefinition = AppConstants.Database.Events.Definitions.COLUMN_DESCRIPTION
    )
    private String description;

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

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_START_DATE_TIME,
        nullable = false
    )
    private LocalDateTime startDateTime;


    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_END_DATE_TIME,
        nullable = false
    )
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_TYPE,
        nullable = false
    )
    private EventType eventType;

    @Column(name = AppConstants.Database.Events.TableNames.COLUMN_MAX_ATTENDEES)
    private Integer maxAttendees;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_REGISTRATION_STATUS,
        nullable = false
    )
    private RegistrationStatus registrationStatus;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_EVENT_STATUS,
        nullable = false
    )
    private EventStatus eventStatus;

    @OneToMany(mappedBy = AppConstants.Database.Events.MappedByNames.MAPPED_BY_EVENT)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.Events.MappedByNames.MAPPED_BY_EVENT)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

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
    public void setId(Long id)                                                         { this.id = id; }
    public void setPublicId(String publicId)                                           { this.publicId = publicId; }
    public void setEventHost(EventHost eventHost)                                      { this.eventHost = eventHost; }
    public void setCreated(LocalDateTime created)                                      { this.created = created; }
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