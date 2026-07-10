package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table
(
    name = AppConstants.Database.Attendees.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL)
    }
)
public class Attendee
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String firstName;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String middleName;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String lastName;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.EMAIL_LENGTH
    )
    private String email;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<BlockedRegistration> blockedRegistration = new HashSet<>();
}