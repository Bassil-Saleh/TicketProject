package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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

    // ************************************************
    // Constructors
    // ************************************************
    public EventSigningKey() {}

    public EventSigningKey
    (
        Event event,
        byte[] privateKey,
        byte[] publicKey,
        LocalDateTime created
    )
    {
        this.event = event;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.created = created;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()               { return this.id; }
    public Event getEvent()           { return this.event; }
    public byte[] getPrivateKey()     { return this.privateKey; }
    public byte[] getPublicKey()      { return this.publicKey; }
    public LocalDateTime getCreated() { return this.created; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                    { this.id = id; }
    public void setEvent(Event event)             { this.event = event; }
    public void setPrivateKey(byte[] privateKey)  { this.privateKey = privateKey; }
    public void setPublicKey(byte[] publicKey)    { this.publicKey = publicKey; }
    public void setCreated(LocalDateTime created) { this.created = created; }

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
        private byte[] privateKey;
        private byte[] publicKey;
        private LocalDateTime created;

        public Builder event(Event event)
        {
            this.event = event;
            return this;
        }

        public Builder privateKey(byte[] privateKey)
        {
            this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(byte[] publicKey)
        {
            this.publicKey = publicKey;
            return this;
        }

        public Builder created(LocalDateTime created)
        {
            this.created = created;
            return this;
        }

        public EventSigningKey build()
        {
            return new EventSigningKey(event, privateKey, publicKey, created);
        }
    }
}