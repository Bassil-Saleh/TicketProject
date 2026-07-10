package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
(
    name = AppConstants.Database.EventSigningKeys.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_EVENT_ID)
    }
)
public class EventSigningKey
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    @Lob
    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_PRIVATE_KEY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventSigningKeys.Definitions.COLUMN_PRIVATE_KEY
    )
    private byte[] privateKey;

    @Lob
    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_PUBLIC_KEY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventSigningKeys.Definitions.COLUMN_PUBLIC_KEY
    )
    private byte[] publicKey;

    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;
}