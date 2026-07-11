package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

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

    // ************************************************
    // Constructors
    // ************************************************
    public PasswordResetToken() {}

    public PasswordResetToken
    (
        EventHost eventHost,
        String token,
        LocalDateTime created,
        LocalDateTime expires
    )
    {
        this.eventHost = eventHost;
        this.token = token;
        this.created = created;
        this.expires = expires;
        this.used = false;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public EventHost getEventHost()   { return this.eventHost; }
    public String getToken()          { return this.token; }
    public LocalDateTime getCreated() { return this.created; }
    public boolean isUsed()           { return this.used; }
    public LocalDateTime getExpires() { return this.expires; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                    { this.id = id; }
    public void setEventHost(EventHost eventHost) { this.eventHost = eventHost; }
    public void setToken(String token)            { this.token = token; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public void setUsed(boolean used)             { this.used = used; }
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
               ", eventHost=" + (this.eventHost != null ? this.eventHost.getEmail() : null) +
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
        private String token;
        private LocalDateTime created;
        private LocalDateTime expires;

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
            return this;
        }

        public Builder token(String token)
        {
            this.token = token;
            return this;
        }

        public Builder created(LocalDateTime created)
        {
            this.created = created;
            return this;
        }

        public Builder expires(LocalDateTime expires)
        {
            this.expires = expires;
            return this;
        }

        public PasswordResetToken build()
        {
            return new PasswordResetToken(eventHost, token, created, expires);
        }
    }
}