package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EditEventAddressByPublicIdRequest
(
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
        " characters"
    )
    String publicId,

    @NotBlank(message = "Event 1st address line cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine1,

    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH,
        message = "Event address line cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.ADDRESS_LINE_LENGTH +
        " characters"
    )
    String addressLine2,

    @NotBlank(message = "Event city cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.CITY_LENGTH,
        message = "Event city cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.CITY_LENGTH +
        " characters"
    )
    String city,

    @NotBlank(message = "Event state cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.STATE_LENGTH,
        message = "Event state cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.STATE_LENGTH +
        " characters"
    )
    String state,

    @NotBlank(message = "Event postal code cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.POSTAL_CODE_LENGTH,
        message = "Event postal code cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.POSTAL_CODE_LENGTH +
        " characters"
    )
    String postalCode,

    @NotBlank(message = "Event country cannot be blank")
    @Size
    (
        max = AppConstants.DTO.EventAddresses.Sizes.COUNTRY_LENGTH,
        message = "Event country cannot be longer than " +
        AppConstants.DTO.EventAddresses.Sizes.COUNTRY_LENGTH +
        " characters"
    )
    String country,

    // TODO: figure out how to add constraints for BigDecimal scale and precision here.

    BigDecimal latitude,

    BigDecimal longitude
)
{
}
