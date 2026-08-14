package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * EditEventAddressByPublicIdRequest specifies what a valid request to
 * edit the address of an event identified by its public ID should look like.
 * @param publicId the event's public ID
 * @param addressLine1 the event's first address line
 * @param addressLine2 the event's second address line
 * @param city the event's city
 * @param state the event's state
 * @param postalCode the event's postal code
 * @param country the event's country
 * @param latitude the event's latitude (optional)
 * @param longitude the event's longitude (optional)
 */
@Schema(description = "Request body for editing an event's address")
public record EditEventAddressByPublicIdRequest
(
    @Schema
    (
        description = "The public ID of the event to edit",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        maximum = "36"
    )
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
        " characters"
    )
    String publicId,

    @Schema
    (
        description = "The first line of the event's street address",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "456 Oak Avenue",
        maximum = "255"
    )
    @NotBlank(message = "Event 1st address line cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine1,

    @Schema
    (
        description = "The second line of the event's street address (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Building B",
        maximum = "255"
    )
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine2,

    @Schema
    (
        description = "The city where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "New York",
        maximum = "100"
    )
    @NotBlank(message = "Event city cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.CITY_LENGTH,
        message = "Event city cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.CITY_LENGTH +
        " characters"
    )
    String city,

    @Schema
    (
        description = "The state or province where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "New York",
        maximum = "100"
    )
    @NotBlank(message = "Event state cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.STATE_LENGTH,
        message = "Event state cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.STATE_LENGTH +
        " characters"
    )
    String state,

    @Schema
    (
        description = "The postal or ZIP code of the event's location",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "10001",
        maximum = "20"
    )
    @NotBlank(message = "Event postal code cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.POSTAL_CODE_LENGTH,
        message = "Event postal code cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.POSTAL_CODE_LENGTH +
        " characters"
    )
    String postalCode,

    @Schema
    (
        description = "The country where the event takes place",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "United States",
        maximum = "100"
    )
    @NotBlank(message = "Event country cannot be blank")
    @Size
    (
        max = AppConstants.Database.EventAddresses.Sizes.COUNTRY_LENGTH,
        message = "Event country cannot be longer than " +
        AppConstants.Database.EventAddresses.Sizes.COUNTRY_LENGTH +
        " characters"
    )
    String country,

    // TODO: figure out how to add constraints for BigDecimal scale and precision here.

    @Schema
    (
        description = "The latitude coordinate of the event's precise location (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "40.7127753"
    )
    BigDecimal latitude,

    @Schema
    (
        description = "The longitude coordinate of the event's precise location (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "-74.0059728"
    )
    BigDecimal longitude
)
{
}