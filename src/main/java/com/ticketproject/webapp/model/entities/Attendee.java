package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.converters.BlindIndexService;
import com.ticketproject.webapp.model.converters.EncryptedStringConverter;
import com.ticketproject.webapp.model.converters.SpringContextBridge;

import jakarta.persistence.*;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String firstName;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String middleName;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.Attendees.Sizes.NAME_LENGTH
    )
    private String lastName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL,
        nullable = false,
        columnDefinition = AppConstants.Database.Attendees.Definitions.COLUMN_EMAIL
    )
    private String email;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_EMAIL_BLIND_INDEX,
        nullable = false,
        columnDefinition = AppConstants.Database.Attendees.Definitions.COLUMN_EMAIL_BLIND_INDEX
    )
    private byte[] emailBlindIndex;

    @Column
    (
        name = AppConstants.Database.Attendees.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.Attendees.MappedByNames.MAPPED_BY_ATTENDEE)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

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
    public void setId(Long id)                                                         { this.id = id; }
    public void setFirstName(String firstName)                                         { this.firstName = firstName; }
    public void setMiddleName(String middleName)                                       { this.middleName = middleName; }
    public void setLastName(String lastName)                                           { this.lastName = lastName; }
    public void setEmail(String email)
    {
        this.email = email;
        this.emailBlindIndex = SpringContextBridge
            .getBean(BlindIndexService.class)
            .computeIndex(email);
    }
    public void setCreated(LocalDateTime created)                                      { this.created = created; }
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