package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.converters.EncryptedLocalDateConverter;
import com.ticketproject.webapp.converters.EncryptedStringConverter;
import com.ticketproject.webapp.services.BlindIndexService;
import com.ticketproject.webapp.services.HashingService;

import jakarta.persistence.*;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * EventHost is an entity representing a record on an event host,
 * who can do things such as create events and invite/block
 * end users to/from events.
 * 
 * Event hosts are uniquely identified by their email address,
 * since that is the primary method from which they verify their
 * account when creating it for the first time, receive a link
 * to reset their password, and so on.
 */
@Entity
@Table
(
    name = AppConstants.Database.EventHosts.TableNames.TABLE_NAME,
    uniqueConstraints =
    {
        @UniqueConstraint
        (columnNames = AppConstants.Database.EventHosts.TableNames.COLUMN_EMAIL_BLIND_INDEX)
    }
)
public class EventHost
{
    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The event host's first name.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_FIRST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String firstName;

    /**
     * The event host's middle name (optional).
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_MIDDLE_NAME,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String middleName;

    /**
     * The event host's last name.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_NAME,
        nullable = false,
        length = AppConstants.Database.EventHosts.Sizes.NAME_LENGTH
    )
    private String lastName;

    /**
     * Used to check that an event host is at least 18 years old when creating a new account.
     */
    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_DATE_OF_BIRTH,
        nullable = false,
        columnDefinition = AppConstants.Database.EventHosts.Definitions.COLUMN_DATE_OF_BIRTH
    )
    private LocalDate dateOfBirth;

    /**
     * Used by the event host to log in to the application, and also receive
     * important communications (account verification, password reset link, etc.).
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_EMAIL,
        nullable = false,
        columnDefinition = AppConstants.Database.EventHosts.Definitions.COLUMN_EMAIL
    )
    private String email;

    /**
     * Used to query the event host database table based on the email field,
     * such as preventing an event host record with an email address that
     * already exists in the table from getting inserted into the table.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_EMAIL_BLIND_INDEX,
        nullable = false,
        columnDefinition = AppConstants.Database.EventHosts.Definitions.COLUMN_EMAIL_BLIND_INDEX
    )
    private byte[] emailBlindIndex;

    /**
     * Stores the hashed password of the event host's account.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_PASSWORD_HASH,
        nullable = false,
        columnDefinition = AppConstants.Database.EventHosts.Definitions.COLUMN_PASSWORD_HASH
    )
    private String passwordHash;

    /**
     * When the record was first created. Should not be changed.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_CREATED,
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    /**
     * When the event host last logged in. Technically, they log in when
     * they create an account for the very first time, hence this field being non-null.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_LOGIN,
        nullable = false
    )
    private LocalDateTime lastLogin;

    /**
     * When this record was last updated.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    /**
     * Whether or not the event host's account is still active.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_ACTIVE,
        nullable = false
    )
    private boolean active;

    /**
     * Whether or not the event host's account has been successfully verified.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFIED,
        nullable = false
    )
    private boolean verified;

    /**
     * Stores a hash of the account verification key sent to the event host's
     * email address when they create an account for the first time.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_KEY_HASH,
        nullable = false,
        columnDefinition = AppConstants.Database.EventHosts.Definitions.COLUMN_VERIFICATION_KEY_HASH
    )
    private byte[] verificationKeyHash;

    /**
     * When the account verification key for an event host's account expires.
     */
    @Column
    (
        name = AppConstants.Database.EventHosts.TableNames.COLUMN_VERIFICATION_EXPIRES,
        nullable = false
    )
    private LocalDateTime verificationExpires;

    /**
     * An event host can create many events, but each event can only be authored by a single event host.
     */
    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<Event> events = new HashSet<>();

    /**
     * An event host's address book can have several records, but each address book record
     * can only belong to a single event host.
     */
    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<AddressBookContact> addressBookContacts = new HashSet<>();

    /**
     * An event host can have multiple password reset tokens, but each password reset token
     * can only belong to a single event host.
     */
    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_EVENT_HOST)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    /**
     * An event host can block multiple registrations, but each block can only
     * be authored by a single event host.
     */
    @OneToMany(mappedBy = AppConstants.Database.EventHosts.MappedByNames.MAPPED_BY_BLOCKED_BY)
    private Set<BlockedRegistration> blockedRegistrations = new HashSet<>();

    /**
     * An event host can scan multiple tickets, but each ticket can only be scanned by a single event host.
     */
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
        String plaintextPassword
    )
    {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        // There's a converter to automatically encrypt/decrypt this field.
        this.setEmail(email);
        // Just store the password's hash, NEVER the plaintext password.
        this.setPassword(plaintextPassword);
        // Should not be changed after creation.
        this.created = LocalDateTime.now();
        // Technically, an account is updated when it is first created.
        this.lastUpdated = LocalDateTime.now();
        this.active = true;
        // Technically, the user logged in when they first create their account.
        this.lastLogin = LocalDateTime.now();
        // New users need to verify their account before
        // they can start using all its features.
        this.verified = false;
        // Since the database only stores the hashed verification token, the verification token
        // should be generated outside of this constructor. That way:
        // - The raw verification token can still be sent to the user
        //   (i.e. as part of a URL for an account verification email).
        // - If for whatever reason the application fails to send the
        //   raw verification token to the user, then the application
        //   can avoid creating a database record for an account which
        //   the user would have no way to verify.
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
    public byte[] getEmailBlindIndex()                        { return this.emailBlindIndex; }
    public String getPasswordHash()                           { return this.passwordHash; }
    public LocalDateTime getCreated()                         { return this.created; }
    public LocalDateTime getLastLogin()                       { return this.lastLogin; }
    public LocalDateTime getLastUpdated()                     { return this.lastUpdated; }
    public boolean isActive()                                 { return this.active; }
    public boolean isVerified()                               { return this.verified; }
    public byte[] getVerificationKeyHash()                    { return this.verificationKeyHash; }
    public LocalDateTime getVerificationExpires()             { return this.verificationExpires; }
    public Set<Event> getEvents()                             { return this.events; }
    public Set<AddressBookContact> getAddressBookContacts()   { return this.addressBookContacts; }
    public Set<PasswordResetToken> getPasswordResetTokens()   { return this.passwordResetTokens; }
    public Set<BlockedRegistration> getBlockedRegistrations() { return this.blockedRegistrations; }
    public Set<TicketScan> getTicketScans()                   { return this.ticketScans; }

    // ************************************************
    // Setters
    // ************************************************
    public void setFirstName(String firstName)                                         { this.firstName = firstName; }
    public void setMiddleName(String middleName)                                       { this.middleName = middleName; }
    public void setLastName(String lastName)                                           { this.lastName = lastName; }
    /**
     * Given a plaintext email, encrypt it, compute its blind index,
     * and update the event host's record. (i.e. they want to change it).
     * @param email the new plaintext email
     */
    public void setEmail(String email)
    {
        this.email = email;
        this.emailBlindIndex = SpringContextBridge
            .getBean(BlindIndexService.class)
            .computeIndex(email);
    }
    /**
     * Given a new plaintext password, compute its hash and update
     * the event host's password hash (i.e. they want to change it).
     * @param plaintextPassword the new plaintext password
     */
    public void setPassword(String plaintextPassword)
    {
        HashingService hasher = SpringContextBridge.getBean(HashingService.class);
        this.passwordHash = hasher.hashPassword(plaintextPassword);
    }
    public void setLastLogin(LocalDateTime lastLogin)                                  { this.lastLogin = lastLogin; }
    public void setLastUpdated(LocalDateTime lastUpdated)                              { this.lastUpdated = lastUpdated; }
    public void setActive(boolean active)                                              { this.active = active; }
    public void setVerified(boolean verified)                                          { this.verified = verified; }
    /**
     * Generate a verification token, store its hash, then return the raw token
     * (i.e. to use in a URL in an account verification email).
     * 
     * Since the server only stores the hash of this token when constructing
     * a new EventHost object, this method should be used right after creating
     * a new event host account, so that the user can still get the raw token.
     * @return the raw verification token
     */
    public String generateVerificationToken()
    {
        HashingService hasher = SpringContextBridge.getBean(HashingService.class);
        HashingService.GeneratedToken token = hasher.generateVerificationToken();
        this.verificationKeyHash = token.tokenHash();
        this.verificationExpires = LocalDateTime
            .now()
            .plusHours(AppConstants.Database.EventHosts.Sizes.VERIFICATION_DURATION_HOURS);
        return token.rawToken();
    }
    public void setEvents(Set<Event> events)                                           { this.events = events; }
    public void setAddressBookContacts(Set<AddressBookContact> addressBookContacts)    { this.addressBookContacts = addressBookContacts; }
    public void setPasswordResetTokens(Set<PasswordResetToken> passwordResetTokens)    { this.passwordResetTokens = passwordResetTokens; }
    public void setBlockedRegistrations(Set<BlockedRegistration> blockedRegistrations) { this.blockedRegistrations = blockedRegistrations; }
    public void setTicketScans(Set<TicketScan> ticketScans)                            { this.ticketScans = ticketScans; }
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
        private String plaintextPassword;

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

        public Builder plaintextPassword(String plaintextPassword)
        {
            this.plaintextPassword = plaintextPassword;
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
                plaintextPassword
            );
        }
    }
}
