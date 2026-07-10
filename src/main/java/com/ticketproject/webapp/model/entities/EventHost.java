package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table
(
    name = AppConstants.Database.EventHosts.TableNames.TABLE_NAME
)
public class EventHost
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String firstName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String middleName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String lastName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_DATE_OF_BIRTH,
        nullable = false
    )
    private LocalDateTime dateOfBirth;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_ACTIVE,
        nullable = false
    )
    private boolean active;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFIED,
        nullable = false
    )
    private boolean verified;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_KEY,
        length = AppConstants.Database.EventHosts.Sizes.VERIFICATION_KEY_LENGTH
    )
    private String verificationKey;

    @Column(name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_EXPIRES)
    private LocalDateTime verificationExpires;

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_BLOCKED_BY)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_SCANNED_BY)
    private Set<TicketScan> ticketScans = new HashSet<>();
}
