package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.services.HashingService;
import com.ticketproject.webapp.bridges.SpringContextBridge;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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
        name = AppConstants.Database.PasswordResetTokens.TableNames.COLUMN_TOKEN_HASH,
        nullable = false,
        columnDefinition = AppConstants.Database.PasswordResetTokens.Definitions.COLUMN_TOKEN_HASH
    )
    private byte[] tokenHash;

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

    // ************************************************
    // Constructors
    // ************************************************
    public PasswordResetToken() {}

    public PasswordResetToken
    (
        EventHost eventHost,
        byte[] tokenHash
    )
    {
        this.eventHost = eventHost;
        this.tokenHash = tokenHash;
        this.created = LocalDateTime.now();
        this.used = false;
        // Since the database only stores the hashed token, the token
        // should be generated outside of this constructor. That way:
        // - The raw token can still be sent to the user
        //   (i.e. as part of a URL for a password reset form).
        // - If for whatever reason the application fails to send the
        //   raw token to the user, then the application can avoid
        //   creating a database record for a password reset token
        //   which the user cannot obtain.
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public EventHost getEventHost()   { return this.eventHost; }
    public byte[] getTokenHash()      { return this.tokenHash; }
    public LocalDateTime getCreated() { return this.created; }
    public boolean isUsed()           { return this.used; }
    public LocalDateTime getExpires() { return this.expires; }

    // ************************************************
    // Setters
    // ************************************************
    public void setEventHost(EventHost eventHost) { this.eventHost = eventHost; }
    public void setTokenHash(byte[] tokenHash)    { this.tokenHash = tokenHash; }
    public void setUsed(boolean used)             { this.used = used; }
    /**
     * Generate a password reset token, store its hash, then return the raw token
     * (i.e. to use in a URL to a password reset page).
     * 
     * Since the server only stores the hash of this token when constructing
     * a new PasswordResetToken object, this method should be used right after creating
     * a new password reset token, so that the user can still get the raw token.
     * @return the raw password reset token
     */
    public String generateToken()
    {
        HashingService hasher = SpringContextBridge.getBean(HashingService.class);
        HashingService.GeneratedToken token = hasher.generateVerificationToken();
        this.tokenHash = token.tokenHash();
        this.expires = LocalDateTime
            .now()
            .plusHours(AppConstants.Database.PasswordResetTokens.Sizes.TOKEN_DURATION_HOURS);
        return token.rawToken();
    }
    public void setExpires(LocalDateTime expires) { this.expires = expires; }

    // ************************************************
    // Convenience methods
    // ************************************************
    public boolean isExpired()
    {
        return LocalDateTime.now().isAfter(this.expires);
    }

    public boolean isValid()
    {
        return !this.used && !this.isExpired();
    }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof PasswordResetToken that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "PasswordResetToken{" +
               "id=" + this.id +
               ", eventHostId=" + (this.eventHost != null ? this.eventHost.getId() : null) +
               ", used=" + this.used +
               ", expires=" + this.expires +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private EventHost eventHost;
        private byte[] tokenHash;

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
            return this;
        }

        public Builder tokenHash(byte[] tokenHash)
        {
            this.tokenHash = tokenHash;
            return this;
        }

        public PasswordResetToken build()
        {
            return new PasswordResetToken(eventHost, tokenHash);
        }
    }
}