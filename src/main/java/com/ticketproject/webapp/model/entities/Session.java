package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.ClientType;
import com.ticketproject.webapp.services.HashingService;
import com.ticketproject.webapp.bridges.SpringContextBridge;

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
    private byte[] tokenHash;

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
        ClientType clientType,
        String ipAddress,
        String userAgent
    )
    {
        this.eventHost = eventHost;
        this.clientType = clientType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.created = LocalDateTime.now();
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public EventHost getEventHost()   { return this.eventHost; }
    public byte[] getTokenHash()      { return this.tokenHash; }
    public ClientType getClientType() { return this.clientType; }
    public String getIpAddress()      { return this.ipAddress; }
    public String getUserAgent()      { return this.userAgent; }
    public LocalDateTime getCreated() { return this.created; }
    public LocalDateTime getExpires() { return this.expires; }
    public LocalDateTime getRevoked() { return this.revoked; }

    // ************************************************
    // Setters
    // ************************************************
    public void setEventHost(EventHost eventHost)    { this.eventHost = eventHost; }
    /**
     * Generate a user session token, store its hash, then return the raw token
     * (i.e. to be maintained by the user's web browser).
     * 
     * Since the server only stores the hash of this token when constructing
     * a new Session object, this method should be used right after creating
     * a new user session token, so that the user can still get the raw token.
     * @return the raw user session token
     */
    public String generateToken()
    {
        HashingService hasher = SpringContextBridge.getBean(HashingService.class);
        HashingService.GeneratedToken token = hasher.generateVerificationToken();
        this.tokenHash = token.tokenHash();
        this.expires = LocalDateTime
            .now()
            .plusHours(AppConstants.Database.Sessions.Sizes.LOGIN_SESSION_DURATION_HOURS);
        return token.rawToken();
    }
    public void setClientType(ClientType clientType) { this.clientType = clientType; }
    public void setIpAddress(String ipAddress)       { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent)       { this.userAgent = userAgent; }
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
        private ClientType clientType;
        private String ipAddress;
        private String userAgent;

        public Builder eventHost(EventHost eventHost)
        {
            this.eventHost = eventHost;
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

        public Session build()
        {
            return new Session
            (
                eventHost,
                clientType,
                ipAddress,
                userAgent
            );
        }
    }
}