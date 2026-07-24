package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.converters.EncryptedBigDecimalConverter;
import com.ticketproject.webapp.converters.EncryptedStringConverter;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = AppConstants.Database.EventAddresses.TableNames.TABLE_NAME)
public class EventAddress
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_CREATED,
        nullable = false
    )
    private LocalDateTime created;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_LAST_UPDATED,
        nullable = false
    )
    private LocalDateTime lastUpdated;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_ADDRESS_LINE_1,
        nullable = false,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_ADDRESS_LINE
    )
    private String addressLine1;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_ADDRESS_LINE_2,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_ADDRESS_LINE
    )
    private String addressLine2;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_CITY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_CITY
    )
    private String city;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_STATE,
        nullable = false,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_STATE
    )
    private String state;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_POSTAL_CODE,
        nullable = false,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_POSTAL_CODE
    )
    private String postalCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_COUNTRY,
        nullable = false,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_COUNTRY
    )
    private String country;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_LATITUDE,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_LATITUDE
    )
    private BigDecimal latitude;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_LONGITUDE,
        columnDefinition = AppConstants.Database.EventAddresses.Definitions.COLUMN_LONGITUDE
    )
    private BigDecimal longitude;

    @OneToOne
    (mappedBy = AppConstants.Database.EventAddresses.MappedByNames.MAPPED_BY_EVENT_ADDRESS)
    private Event event;

    // ************************************************
    // Constructors
    // ************************************************
    public EventAddress() {}

    public EventAddress
    (
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        BigDecimal latitude,
        BigDecimal longitude
    )
    {
        this.created = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        // If the user is going to supply latitude/longitude coordinates
        // to the constructor, then I don't think it makes sense to have
        // one of them null and the other non-null.
        if ((latitude == null && longitude != null) || (latitude != null && longitude == null))
            throw new IllegalArgumentException("If either latitude or longitude is supplied, then both of them must be non-null");
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    // ************************************************
    // Getters
    // ************************************************
    public Long getId()                   { return this.id; }
    public LocalDateTime getCreated()     { return this.created; }
    public LocalDateTime getLastUpdated() { return this.lastUpdated; }
    public String getAddressLine1()       { return this.addressLine1; }
    public String getAddressLine2()       { return this.addressLine2; }
    public String getCity()               { return this.city; }
    public String getState()              { return this.state; }
    public String getPostalCode()         { return this.postalCode; }
    public String getCountry()            { return this.country; }
    public BigDecimal getLatitude()       { return this.latitude; }
    public BigDecimal getLongitude()      { return this.longitude; }

    // ************************************************
    // Setters
    // ************************************************
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setAddressLine1(String addressLine1)      { this.addressLine1 = addressLine1; }
    public void setAddressLine2(String addressLine2)      { this.addressLine2 = addressLine2; }
    public void setCity(String city)                      { this.city = city; }
    public void setState(String state)                    { this.state = state; }
    public void setPostalCode(String postalCode)          { this.postalCode = postalCode; }
    public void setCountry(String country)                { this.country = country; }
    public void setLatitude(BigDecimal latitude)
    {
        if
        (
            latitude != null &&
            latitude.scale() > AppConstants.Database.EventAddresses.Sizes.LATITUDE_SCALE
        )
        {
            throw new IllegalArgumentException
            (
                "Latitude must not exceed " +
                AppConstants.Database.EventAddresses.Sizes.LATITUDE_SCALE +
                " decimal places"
            );
        }
        if
        (
            latitude != null &&
            latitude.precision() > AppConstants.Database.EventAddresses.Sizes.LATITUDE_PRECISION
        )
        {
            throw new IllegalArgumentException
            (
                "Latitude precision must not exceed " +
                AppConstants.Database.EventAddresses.Sizes.LATITUDE_PRECISION +
                " digits"
            );
        }
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude)
    {
        if
        (
            longitude != null &&
            longitude.scale() > AppConstants.Database.EventAddresses.Sizes.LONGITUDE_SCALE
        )
        {
            throw new IllegalArgumentException
            (
                "Longitude must not exceed " +
                AppConstants.Database.EventAddresses.Sizes.LONGITUDE_SCALE +
                " decimal places"
            );
        }
        if
        (
            longitude != null &&
            longitude.precision() > AppConstants.Database.EventAddresses.Sizes.LONGITUDE_PRECISION
        )
        {
            throw new IllegalArgumentException
            (
                "Longitude precision must not exceed " +
                AppConstants.Database.EventAddresses.Sizes.LONGITUDE_PRECISION +
                " digits"
            );
        }
        this.longitude = longitude;
    }
    @PostLoad
    private void validateCoordinatesOnLoad()
    {
        this.validateCoordinates();
    }
    @PrePersist
    @PreUpdate
    private void validateCoordinatesBeforeWrite()
    {
        this.validateCoordinates();
    }
    /**
     * Used to detect if coordinate data in the database has become corrupted.
     */
    private void validateCoordinates()
    {
        // I don't think it makes sense if the latitude is null
        // but the longitude is null (or vice versa).
        if (latitude == null && longitude != null)
            throw new IllegalStateException("If longitude is not null, then latitude cannot be null");
        if (latitude != null && longitude == null)
            throw new IllegalStateException("If latitude is not null, then longitude cannot be null");

        int latitudeScale = AppConstants.Database.EventAddresses.Sizes.LATITUDE_SCALE;
        int latitudePrecision = AppConstants.Database.EventAddresses.Sizes.LATITUDE_PRECISION;
        int longitudeScale = AppConstants.Database.EventAddresses.Sizes.LONGITUDE_SCALE;
        int longitudePrecision = AppConstants.Database.EventAddresses.Sizes.LONGITUDE_PRECISION;

        if (this.latitude != null && this.latitude.scale() > latitudeScale)
        {
            throw new IllegalStateException("Latitude scale exceeds maximum " + latitudeScale + ": " + this.latitude.toPlainString());
        }
        if (this.latitude != null && this.latitude.precision() > latitudePrecision)
        {
            throw new IllegalStateException("Latitude precision exceeds maximum " + latitudePrecision + ": " + this.latitude.toPlainString());
        }

        if (this.longitude != null && this.longitude.scale() > longitudeScale)
        {
            throw new IllegalStateException("Longitude scale exceeds maximum " + longitudeScale + ": " + this.longitude.toPlainString());
        }
        if (this.longitude != null && this.longitude.precision() > longitudePrecision)
        {
            throw new IllegalStateException("Longitude precision exceeds maximum " + longitudePrecision + ": " + this.longitude.toPlainString());
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
        if (!(other instanceof EventAddress that))
            return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() { return this.getClass().hashCode(); }

    public String toString()
    {
        return "EventAddress{" +
               "id=" + this.id +
               ", addressLine1=[ENCRYPTED]" +
               ", addressLine2=[ENCRYPTED]" +
               ", city=[ENCRYPTED]" +
               ", state=[ENCRYPTED]" +
               ", postalCode=[ENCRYPTED]" +
               ", country=[ENCRYPTED]" +
               ", latitude=[ENCRYPTED]" +
               ", longitude=[ENCRYPTED]" +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;

        public Builder addressLine1(String addressLine1)
        {
            this.addressLine1 = addressLine1;
            return this;
        }

        public Builder addressLine2(String addressLine2)
        {
            this.addressLine2 = addressLine2;
            return this;
        }

        public Builder city(String city)
        {
            this.city = city;
            return this;
        }

        public Builder state(String state)
        {
            this.state = state;
            return this;
        }

        public Builder postalCode(String postalCode)
        {
            this.postalCode = postalCode;
            return this;
        }

        public Builder country(String country)
        {
            this.country = country;
            return this;
        }

        public Builder latitude(BigDecimal latitude)
        {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(BigDecimal longitude)
        {
            this.longitude = longitude;
            return this;
        }

        public EventAddress build()
        {
            return new EventAddress
            (
                addressLine1,
                addressLine2,
                city,
                state,
                postalCode,
                country,
                latitude,
                longitude
            );
        }
    }
}
