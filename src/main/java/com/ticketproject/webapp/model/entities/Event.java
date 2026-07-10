package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
(
    name = AppConstants.Database.Events.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (
            columnNames = AppConstants.Database.Events.TableNames.COLUMN_PUBLIC_ID
        )
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
        columnDefinition = AppConstants.Database.Events.Definitions.COLUMN_DESCRIPTION
    )
    private String description;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_ADDRESS_LINE_1,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.ADDRESS_LINE_LENGTH
    )
    private String addressLine1;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_ADDRESS_LINE_2,
        length = AppConstants.Database.Events.Sizes.ADDRESS_LINE_LENGTH
    )
    private String addressLine2;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_CITY,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.CITY_LENGTH
    )
    private String city;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_STATE,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.STATE_LENGTH
    )
    private String state;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_POSTAL_CODE,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.POSTAL_CODE_LENGTH
    )
    private String postalCode;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_COUNTRY,
        nullable = false,
        length = AppConstants.Database.Events.Sizes.COUNTRY_LENGTH
    )
    private String country;

    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_LATITUDE,
        precision = AppConstants.Database.Events.Sizes.LATITUDE_PRECISION,
        scale = AppConstants.Database.Events.Sizes.LATITUDE_SCALE
    )
    private BigDecimal latitude;


    @Column
    (
        name = AppConstants.Database.Events.TableNames.COLUMN_LONGITUDE,
        precision = AppConstants.Database.Events.Sizes.LONGITUDE_PRECISION,
        scale = AppConstants.Database.Events.Sizes.LONGITUDE_SCALE
    )
    private BigDecimal longitude;

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
}