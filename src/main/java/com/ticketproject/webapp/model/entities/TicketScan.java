package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
(
    name = AppConstants.Database.TicketScans.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.TicketScans.TableNames.COLUMN_TICKET_ID)
    }
)
public class TicketScan
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.TicketScans.TableNames.COLUMN_TICKET_ID,
        nullable = false
    )
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.TicketScans.TableNames.COLUMN_SCANNED_BY,
        nullable = false
    )
    private EventHost scannedBy;

    @Column
    (
        name = AppConstants.Database.TicketScans.TableNames.COLUMN_SCANNED_AT,
        nullable = false
    )
    private LocalDateTime scannedAt;

    @Column
    (
        name = AppConstants.Database.TicketScans.TableNames.COLUMN_DEVICE_INFO,
        length = AppConstants.Database.TicketScans.Sizes.DEVICE_INFO_LENGTH
    )
    private String deviceInfo;
}