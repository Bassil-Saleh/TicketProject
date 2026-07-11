package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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

    // ************************************************
    // Constructors
    // ************************************************
    public TicketScan() {}

    public TicketScan
    (
        Ticket ticket,
        EventHost scannedBy,
        LocalDateTime scannedAt,
        String deviceInfo
    )
    {
        this.ticket = ticket;
        this.scannedBy = scannedBy;
        this.scannedAt = scannedAt;
        this.deviceInfo = deviceInfo;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                 { return this.id; }
    public Ticket getTicket()           { return this.ticket; }
    public EventHost getScannedBy()     { return this.scannedBy; }
    public LocalDateTime getScannedAt() { return this.scannedAt; }
    public String getDeviceInfo()       { return this.deviceInfo; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                        { this.id = id; }
    public void setTicket(Ticket ticket)              { this.ticket = ticket; }
    public void setScannedBy(EventHost scannedBy)     { this.scannedBy = scannedBy; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
    public void setDeviceInfo(String deviceInfo)      { this.deviceInfo = deviceInfo; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof TicketScan that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "TicketScan{" +
               "id=" + this.id +
               ", ticketId=" + (this.ticket != null ? this.ticket.getId() : null) +
               ", scannedBy=" + (this.scannedBy != null ? this.scannedBy.getEmail() : null) +
               ", scannedAt=" + this.scannedAt +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private Ticket ticket;
        private EventHost scannedBy;
        private LocalDateTime scannedAt;
        private String deviceInfo;

        public Builder ticket(Ticket ticket)
        {
            this.ticket = ticket;
            return this;
        }

        public Builder scannedBy(EventHost eventHost)
        {
            this.scannedBy = eventHost;
            return this;
        }

        public Builder scannedAt(LocalDateTime scannedAt)
        {
            this.scannedAt = scannedAt;
            return this;
        }

        public Builder deviceInfo(String deviceInfo)
        {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public TicketScan build()
        {
            return new TicketScan(ticket, scannedBy, scannedAt, deviceInfo);
        }
    }
}