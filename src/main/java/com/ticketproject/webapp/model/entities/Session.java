package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.ClientType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table
(
    name = AppConstants.Database.Sessions.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.Sessions.TableNames.COLUMN_TOKEN_HASH)
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
        columnDefinition = AppConstants.Database.Sessions.Definitions.COLUMN_TOKEN_HASH
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
    // ************************************************
    // Constructors
    // ************************************************
    public Session() {}

    public Session
    (
        EventHost eventHost,
        String tokenHash,
        ClientType clientType,
        String ipAddress,
        String userAgent,
        LocalDateTime created,
        LocalDateTime expires
    )
    {
        this.eventHost = eventHost;
        this.tokenHash = tokenHash;
        this.clientType = clientType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.created = created;
        this.expires = expires;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public EventHost getEventHost()   { return this.eventHost; }
    public String getTokenHash()      { return this.tokenHash; }
    public ClientType getClientType() { return this.clientType; }
    public String getIpAddress()      { return this.ipAddress; }
    public String getUserAgent()      { return this.userAgent; }
    public LocalDateTime getCreated() { return this.created; }
    public LocalDateTime getExpires() { return this.expires; }
    public LocalDateTime getRevoked() { return this.revoked; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                       { this.id = id; }
    public void setEventHost(EventHost eventHost)    { this.eventHost = eventHost; }
    public void setTokenHash(String tokenHash)       { this.tokenHash = tokenHash; }
    public void setClientType(ClientType clientType) { this.clientType = clientType; }
    public void setIpAddress(String ipAddress)       { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent)       { this.userAgent = userAgent; }
    public void setCreated(LocalDateTime created)    { this.created = created; }
    public void setExpires(LocalDateTime expires)    { this.expires = expires; }
    public void setRevoked(LocalDateTime revoked)    { this.revoked = revoked; }

    // ************************************************
    // Convenience methods
    // ************************************************
    public boolean isExpired() { return LocalDateTime.now().isAfter(this.expires); }

    public boolean isRevoked() { return this.revoked != null; }

    public boolean isActive() { return !isRevoked() && !isExpired(); }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof Session that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "Session{" +
               "id=" + this.id +
               ", eventHostId=" + (this.eventHost != null ? this.eventHost.getId() : null) +
               ", clientType=" + this.clientType +
               ", active=" + this.isActive() +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private EventHost eventHost;
        private String tokenHash;
        private ClientType clientType;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime created;
        private LocalDateTime expires;

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
            return this;
        }

        public Builder tokenHash(String tokenHash)
        {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder clientType(ClientType clientType)
        {
            this.clientType = clientType;
            return this;
        }

        public Builder ipAddress(String ipAddress)
        {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent)
        {
            this.userAgent = userAgent;
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

        public Session build()
        {
            return new Session
            (
                eventHost,
                tokenHash,
                clientType,
                ipAddress,
                userAgent,
                created,
                expires
            );
        }
    }
}