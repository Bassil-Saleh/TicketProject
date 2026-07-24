package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.converters.EncryptedStringConverter;
import com.ticketproject.webapp.services.BlindIndexService;

import jakarta.persistence.*;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * Attendee is an entity representing a record on someone who has either
 * registered for a public event, or who has been invited to a private event.
 * 
 * Since attendees receive all communications on events (tickets, invitations, etc.)
 * via email, each attendee record should have a unique email address.
 */
@Entity
@Table
(
    name = AppConstants.Database.Attendees.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL_BLIND_INDEX)
    }
)
public class Attendee
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The attendee's first name.
     */
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String firstName;

    /**
     * The attendee's middle name (optional).
     */
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String middleName;

    /**
     * The attendee's last name.
     */
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String lastName;

    /**
     * The attendee's email address.
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL,
        nullable = false,
        columnDefinition = AppConstants.Database.Attendees.Definitions.COLUMN_EMAIL
    )
    private String email;

    /**
     * Used to query for attendee records based on the email address field.
     */
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL_BLIND_INDEX,
        nullable = false,
        columnDefinition = AppConstants.Database.Attendees.Definitions.COLUMN_EMAIL_BLIND_INDEX
    )
    private byte[] emailBlindIndex;

    /**
     * When the attendee record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * It is possible for an attendee to have many different tickets and invitations,
     * but each ticket/invitation should only belong to a single attendee.
     */
    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<Ticket> tickets = new HashSet<>();

    /**
     * It is possible for an attendee to be associated with many different address book records
     * (i.e. if multiple different hosts invite the same person to their own private events),
     * but every address book record should only be associated with a single attendee.
     */
    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

    /**
     * Multiple of an attendee's event registrations can be blocked, but each
     * registration block can only be associated with a single attendee.
     */
    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

    // ************************************************
    // Constructors
    // ************************************************
    public Attendee() {}

    public Attendee
    (
        String firstName,
        String middleName,
        String lastName,
        String email
    )
    {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.setEmail(email);
        this.created = LocalDateTime.now();
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                                       { return this.id; }
    public String getFirstName()                              { return this.firstName; }
    public String getMiddleName()                             { return this.middleName; }
    public String getLastName()                               { return this.lastName; }
    public String getEmail()                                  { return this.email; }
    public byte[] getEmailBlindIndex()                        { return this.emailBlindIndex; }
    public LocalDateTime getCreated()                         { return this.created; }
    public Set<Ticket> getTickets()                           { return this.tickets; }
    public Set<AddressBookContact> getAddressBookContacts()   { return this.addressBookContacts; }
    public Set<BlockedRegistration> getBlockedRegistrations() { return this.blockedRegistrations; }

    // ************************************************
    // Setters
    // ************************************************
    public void setFirstName(String firstName)                                         { this.firstName = firstName; }
    public void setMiddleName(String middleName)                                       { this.middleName = middleName; }
    public void setLastName(String lastName)                                           { this.lastName = lastName; }
    /**
     * Given a plaintext email, use a converter to encrypt it,
     * and use a service to compute a blind index for it,
     * and save the ciphertext email and blind index.
     * @param email a plaintext email
     */
    public void setEmail(String email)
    {
        this.email = email;
        this.emailBlindIndex = SpringContextBridge
            .getBean(BlindIndexService.class)
            .computeIndex(email);
    }
    public void setTickets(Set<Ticket> tickets)                                        { this.tickets = tickets; }
    public void setAddressBookContacts(Set<AddressBookContact> addressBookContacts)    { this.addressBookContacts = addressBookContacts; }
    public void setBlockedRegistrations(Set<BlockedRegistration> blockedRegistrations) { this.blockedRegistrations = blockedRegistrations; }
    /**
     * To handle edge cases where JPA loads a partially-constructed
     * entity or when someone uses reflection.
     */
    @PrePersist
    @PreUpdate
    private void syncBlindIndex()
    {
        if (this.email != null)
        {
            byte[] computed = SpringContextBridge
                .getBean(BlindIndexService.class)
                .computeIndex(this.email);
            if (this.emailBlindIndex == null || !MessageDigest.isEqual(this.emailBlindIndex, computed))
            {
                this.emailBlindIndex = computed;
            }
        }
    }

    // ************************************************
    // Convenience methods
    // ************************************************
    public String getFullName()
    {
        StringBuilder fullName = new StringBuilder(this.firstName);

        if (this.middleName != null && !this.middleName.isBlank())
            fullName.append(" ").append(this.middleName);

        fullName.append(" ").append(this.lastName);

        return fullName.toString();
    }

    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof Attendee that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    @Override
    public String toString()
    {
        return "Attendee{" +
               "id=" + this.id +
               ", name='" + this.getFullName() + '\'' +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private String firstName;
        private String middleName;
        private String lastName;
        private String email;

        public Builder firstName(String firstName)
        {
            this.firstName = firstName;
            return this;
        }

        public Builder middleName(String middleName)
        {
            this.middleName = middleName;
            return this;
        }

        public Builder lastName(String lastName)
        {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email)
        {
            this.email = email;
            return this;
        }

        public Attendee build()
        {
            return new Attendee
            (
                firstName,
                middleName,
                lastName,
                email
            );
        }
    }
}