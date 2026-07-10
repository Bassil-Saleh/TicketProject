package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.ClientType;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
(
    name = AppConstants.Database.Sessions.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (
            columnNames =
            {
                AppConstants.Database.Sessions.TableNames.COLUMN_TOKEN_HASH
            }
        )
    }
)
public class Session
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_TOKEN_HASH,
        nullable = false,
        length = AppConstants.Database.Sessions.Sizes.TOKEN_HASH_LENGTH
    )
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_CLIENT_TYPE,
        nullable = false
    )
    private ClientType clientType;

    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_IP_ADDRESS,
        length = AppConstants.Database.Sessions.Sizes.IP_ADDRESS_LENGTH
    )
    private String ipAddress;

    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_USER_AGENT,
        length = AppConstants.Database.Sessions.Sizes.USER_AGENT_LENGTH
    )
    private String userAgent;

    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_EXPIRES,
        nullable = false
    )
    private LocalDateTime expires;

    @Column(name = AppConstants.Database.Sessions.TableNames.COLUMN_REVOKED)
    private LocalDateTime revoked;
}