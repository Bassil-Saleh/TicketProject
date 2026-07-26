package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.converters.EncryptedPrivateKeyConverter;
import com.ticketproject.webapp.converters.PublicKeyConverter;

import jakarta.persistence.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * EventSigningKey is an entity representing a record that stores a key pair
 * (a public key and a private key) for each event, used to authenticate
 * and verify scanned tickets.
 */
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
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Every event must have an event signing key pair, and
     * every key pair must be associated with a single event.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_EVENT_ID,
        nullable = false
    )
    private Event event;

    /**
     * The event key pair's private key. Used to generate digital signatures to validate
     * whether a scanned ticket actually originated from the server.
     */
    @Convert(converter = EncryptedPrivateKeyConverter.class)
    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_PRIVATE_KEY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventSigningKeys.Definitions.COLUMN_PRIVATE_KEY
    )
    private PrivateKey privateKey;

    /**
     * The event key pair's public key. When an event host logs into the Android app
     * (which is used to scan tickets and communicate with the back-end of the web application)
     * and pulls up an event they've created, the event's public key gets sent to their device.
     * 
     * That way, the event host can quickly validate scanned tickets (as opposed to having each
     * individual scanned ticket take an entire round-trip from their device to the back-end),
     * and then the app can send a queue of those scanned tickets back to the back-end later
     * for further validation.
     */
    @Convert(converter = PublicKeyConverter.class)
    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_PUBLIC_KEY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventSigningKeys.Definitions.COLUMN_PUBLIC_KEY
    )
    private PublicKey publicKey;

    /**
     * When the record was created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.EventSigningKeys.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    // ************************************************
    // Constructors
    // ************************************************
    public EventSigningKey() {}

    public EventSigningKey
    (
        Event event,
        PrivateKey privateKey,
        PublicKey publicKey
    )
    {
        this.event = event;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.created = LocalDateTime.now();
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public Event getEvent()           { return this.event; }
    public PrivateKey getPrivateKey() { return this.privateKey; }
    public PublicKey getPublicKey()   { return this.publicKey; }
    public LocalDateTime getCreated() { return this.created; }

    // ************************************************
    // Setters
    // ************************************************
    public void setEvent(Event event)                { this.event = event; }
    public void setPrivateKey(PrivateKey privateKey) { this.privateKey = privateKey; }
    public void setPublicKey(PublicKey publicKey)    { this.publicKey = publicKey; }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof EventSigningKey that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "EventSigningKey{" +
               "id=" + this.id +
               ", event=" + (this.event != null ? this.event.getName() : null) +
               ", created=" + this.created +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private Event event;
        private PrivateKey privateKey;
        private PublicKey publicKey;

        public Builder event(Event event)
        {
            this.event = event;
            return this;
        }

        public Builder privateKey(PrivateKey privateKey)
        {
            this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(PublicKey publicKey)
        {
            this.publicKey = publicKey;
            return this;
        }

        public EventSigningKey build()
        {
            return new EventSigningKey(event, privateKey, publicKey);
        }
    }
}