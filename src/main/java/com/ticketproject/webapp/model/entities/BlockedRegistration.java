package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
}