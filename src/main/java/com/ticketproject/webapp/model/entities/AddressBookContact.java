package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
}