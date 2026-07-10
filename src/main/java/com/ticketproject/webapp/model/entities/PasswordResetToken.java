package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = AppConstants.Database.PasswordResetTokens.TableNames.TABLE_NAME)
public class PasswordResetToken
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    private EventHost eventHost;

    @Column
    (
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_TOKEN,
        nullable = false,
        length = AppConstants.Database.PasswordResetTokens.Sizes.TOKEN_LENGTH
    )
    private String token;

    @Column
    (
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_USED,
        nullable = false
    )
    private boolean used;

    @Column
    (
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_EXPIRES,
        nullable = false
    )
    private LocalDateTime expires;
}