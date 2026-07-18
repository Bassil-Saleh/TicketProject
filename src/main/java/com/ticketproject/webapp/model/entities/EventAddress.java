package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
