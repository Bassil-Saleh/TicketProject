package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.InvitationStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
(
    name = AppConstants.Database.Tickets.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (
            columnNames =
            {
                AppConstants.Database.Tickets.TableNames.COLUMN_ATTENDEE_ID,
                AppConstants.Database.Tickets.TableNames.COLUMN_EVENT_ID
            }
        ),
        @UniqueConstraint
        (columnNames = AppConstants.Database.Tickets.TableNames.COLUMN_PUBLIC_TOKEN),
        @UniqueConstraint
        (columnNames = AppConstants.Database.Tickets.TableNames.COLUMN_TOKEN_IDENTIFIER),
    }
)
public class Ticket
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PUBLIC_TOKEN,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.PUBLIC_TOKEN_LENGTH
    )
    private String publicToken;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_TOKEN_IDENTIFIER,
        nullable = false,
        length = AppConstants.Database.Tickets.Sizes.TOKEN_IDENTIFIER_LENGTH
    )
    private String tokenIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_ATTENDEE_ID,
        nullable = false
    )
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_PRESENT,
        nullable = false
    )
    private boolean present;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_INVITATION_STATUS,
        nullable = false
    )
    private InvitationStatus invitationStatus;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.Tickets.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Column(name = AppConstants.Database.Tickets.TableNames.COLUMN_DELETED_AT)
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = AppConstants.Database.Tickets.MappedByNames.MAPPED_BY_TICKET)
    private TicketScan ticketScan;
}