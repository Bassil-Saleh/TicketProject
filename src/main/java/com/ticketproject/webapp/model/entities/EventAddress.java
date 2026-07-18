package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

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

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_ADDRESS_LINE_1,
        nullable = false,
        length = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH
    )
    private String addressLine1;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_ADDRESS_LINE_2,
        length = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH
    )
    private String addressLine2;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_CITY,
        nullable = false,
        length = AppConstants.Database.EventAddresses.Sizes.CITY_LENGTH
    )
    private String city;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_STATE,
        nullable = false,
        length = AppConstants.Database.EventAddresses.Sizes.STATE_LENGTH
    )
    private String state;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_POSTAL_CODE,
        nullable = false,
        length = AppConstants.Database.EventAddresses.Sizes.POSTAL_CODE_LENGTH
    )
    private String postalCode;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_COUNTRY,
        nullable = false,
        length = AppConstants.Database.EventAddresses.Sizes.COUNTRY_LENGTH
    )
    private String country;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_LATITUDE,
        precision = AppConstants.Database.EventAddresses.Sizes.LATITUDE_PRECISION,
        scale = AppConstants.Database.EventAddresses.Sizes.LATITUDE_SCALE
    )
    private BigDecimal latitude;

    @Column
    (
        name = AppConstants.Database.EventAddresses.TableNames.COLUMN_LONGITUDE,
        precision = AppConstants.Database.EventAddresses.Sizes.LONGITUDE_PRECISION,
        scale = AppConstants.Database.EventAddresses.Sizes.LONGITUDE_SCALE
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
        LocalDateTime created,
        LocalDateTime lastUpdated,
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
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
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
    public void setId(Long id)                            { this.id = id; }
    public void setCreated(LocalDateTime created)         { this.created = created; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setAddressLine1(String addressLine1)      { this.addressLine1 = addressLine1; }
    public void setAddressLine2(String addressLine2)      { this.addressLine2 = addressLine2; }
    public void setCity(String city)                      { this.city = city; }
    public void setState(String state)                    { this.state = state; }
    public void setPostalCode(String postalCode)          { this.postalCode = postalCode; }
    public void setCountry(String country)                { this.country = country; }
    public void setLatitude(BigDecimal latitude)          { this.latitude = latitude; }
    public void setLongitude(BigDecimal longitude)        { this.longitude = longitude; }


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
               ", addressLine1=" + this.addressLine1 +
               ", addressLine2=" + (this.addressLine2 == null ? "null" : this.addressLine2) +
               ", city=" + this.city +
               ", state=" + this.state +
               ", postalCode=" + this.postalCode +
               ", country=" + this.country +
               ", latitude=" + (this.latitude == null ? "null" : this.latitude) +
               ", longitude=" + (this.longitude == null ? "null" : this.longitude) +
               '}';
    }

    // ************************************************
    // Builder (to make entity creation easier)
    // ************************************************
    public static class Builder
    {
        private LocalDateTime created;
        private LocalDateTime lastUpdated;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;

        public Builder created(LocalDateTime created)
        {
            this.created = created;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated)
        {
            this.lastUpdated = lastUpdated;
            return this;
        }

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
                created,
                lastUpdated,
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
