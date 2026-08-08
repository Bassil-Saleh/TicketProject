package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.converters.EncryptedStringConverter;
import com.ticketproject.webapp.model.enums.ClientType;
import com.ticketproject.webapp.services.database.HashingService;
import com.ticketproject.webapp.bridges.SpringContextBridge;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Session is an entity representing a record of info used
 * to maintain an event host's login session.
 */
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
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * An event host can have multiple login sessions over the lifespan of their account,
     * but each session can only belong to a single event host.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_EVENT_HOST_ID,
        nullable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventHost eventHost;

    /**
     * The hashed token of the login session. After logging in, the event host's device
     * receives the raw token, and the application compares the hash of the raw token
     * it receives to the hash stored in the database.
     */
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_TOKEN_HASH,
        nullable = false,
        columnDefinition = AppConstants.Database.Sessions.Definitions.COLUMN_TOKEN_HASH
    )
    private byte[] tokenHash;

    /**
     * How the event host is accessing the application (through the Web or through the Android app).
     */
    @Enumerated(EnumType.STRING)
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_CLIENT_TYPE,
        nullable = false
    )
    private ClientType clientType;

    /**
     * The IP address where the log in originated from.
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_IP_ADDRESS,
        length = AppConstants.Database.Sessions.Sizes.IP_ADDRESS_LENGTH
    )
    private String ipAddress;

    /**
     * Stores info about the device which the event host is logged in from.
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_USER_AGENT,
        length = AppConstants.Database.Sessions.Sizes.USER_AGENT_LENGTH
    )
    private String userAgent;

    /**
     * When the record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * When the login session expires.
     */
    @Column
    (
        name = AppConstants.Database.Sessions.TableNames.COLUMN_EXPIRES,
        nullable = false
    )
    private LocalDateTime expires;

    /**
     * When the login session was revoked.
     */
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
        // Since the database only stores the hashed session token, the token
        // should be generated outside of this constructor. That way:
        // - The raw token can still be sent to the user.
        // - If for whatever reason the application fails to send the
        //   raw token to the user, then the application can avoid
        //   creating a database record for a login session token
        //   which the user cannot obtain.
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