package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

@Entity
@Table
(
    name = AppConstants.Database.EventHosts.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.EventHosts.TableNames.COLUMN_EMAIL)
    }
)
public class EventHost
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String firstName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String middleName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String lastName;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_DATE_OF_BIRTH,
        nullable = false
    )
    private LocalDate dateOfBirth;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_EMAIL,
        nullable = false
    )
    private String email;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_PASSWORD_HASH,
        nullable = false
    )
    private String passwordHash;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_LOGIN,
        nullable = false
    )
    private LocalDateTime lastLogin;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_ACTIVE,
        nullable = false
    )
    private boolean active;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFIED,
        nullable = false
    )
    private boolean verified;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_KEY,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.VERIFICATION_KEY_LENGTH
    )
    private String verificationKey;

    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_EXPIRES,
        nullable = false
    )
    private LocalDateTime verificationExpires;

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_BLOCKED_BY)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_SCANNED_BY)
    private Set<TicketScan> ticketScans = new HashSet<>();

    // ************************************************
    // Constructors
    // ************************************************
    public EventHost() {}

    public EventHost
    (
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String passwordHash
    )
    {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                                       { return this.id; }
    public String getFirstName()                              { return this.firstName; }
    public String getMiddleName()                             { return this.middleName; }
    public String getLastName()                               { return this.lastName; }
    public LocalDate getDateOfBirth()                         { return this.dateOfBirth; }
    public String getEmail()                                  { return this.email; }
    public String getPasswordHash()                           { return this.passwordHash; }
    public LocalDateTime getCreated()                         { return this.created; }
    public LocalDateTime getLastLogin()                       { return this.lastLogin; }
    public LocalDateTime getLastUpdated()                     { return this.lastUpdated; }
    public boolean isActive()                                 { return this.active; }
    public boolean isVerified()                               { return this.verified; }
    public String getVerificationKey()                        { return this.verificationKey; }
    public LocalDateTime getVerificationExpires()             { return this.verificationExpires; }
    public Set<Event> getEvents()                             { return this.events; }
    public Set<AddressBookContact> getAddressBookContacts()   { return this.addressBookContacts; }
    public Set<PasswordResetToken> getPasswordResetTokens()   { return this.passwordResetTokens; }
    public Set<BlockedRegistration> getBlockedRegistrations() { return this.blockedRegistrations; }
    public Set<TicketScan> getTicketScans()                   { return this.ticketScans; }

    // ************************************************
    // Setters
    // ************************************************
    public void setId(Long id)                                                         { this.id = id; }
    public void setFirstName(String firstName)                                         { this.firstName = firstName; }
    public void setMiddleName(String middleName)                                       { this.middleName = middleName; }
    public void setLastName(String lastName)                                           { this.lastName = lastName; }
    public void setDateOfBirth(LocalDate dateOfBirth)                                  { this.dateOfBirth = dateOfBirth; }
    public void setEmail(String email)                                                 { this.email = email; }
    public void setPasswordHash(String passwordHash)                                   { this.passwordHash = passwordHash; }
    public void setCreated(LocalDateTime created)                                      { this.created = created; }
    public void setLastLogin(LocalDateTime lastLogin)                                  { this.lastLogin = lastLogin; }
    public void setLastUpdated(LocalDateTime lastUpdated)                              { this.lastUpdated = lastUpdated; }
    public void setActive(boolean active)                                              { this.active = active; }
    public void setVerified(boolean verified)                                          { this.verified = verified; }
    public void setVerificationKey(String verificationKey)                             { this.verificationKey = verificationKey; }
    public void setVerificationExpires(LocalDateTime verificationExpires)              { this.verificationExpires = verificationExpires; }
    public void setEvents(Set<Event> events)                                           { this.events = events; }
    public void setAddressBookContacts(Set<AddressBookContact> addressBookContacts)    { this.addressBookContacts = addressBookContacts; }
    public void setPasswordResetTokens(Set<PasswordResetToken> passwordResetTokens)    { this.passwordResetTokens = passwordResetTokens; }
    public void setBlockedRegistrations(Set<BlockedRegistration> blockedRegistrations) { this.blockedRegistrations = blockedRegistrations; }
    public void setTicketScans(Set<TicketScan> ticketScans)                            { this.ticketScans = ticketScans; }


    // ************************************************
    // equals(), hashCode(), toString()
    // ************************************************
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof EventHost that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode()
    {
        return this.getClass().hashCode();
    }

    @Override
    public String toString()
    {
        return "EventHost{" +
               "id=" + this.id +
               ", email='" + this.email + '\'' +
               ", firstName='" + this.firstName + '\'' +
               ", lastName='" + this.lastName + '\'' +
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
        private LocalDate dateOfBirth;
        private String email;
        private String passwordHash;

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

        public Builder dateOfBirth(LocalDate dateOfBirth)
        {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder email(String email)
        {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash)
        {
            this.passwordHash = passwordHash;
            return this;
        }

        public EventHost build()
        {
            return new EventHost
            (
                firstName,
                middleName,
                lastName,
                dateOfBirth,
                email,
                passwordHash
            );
        }
    }
}
