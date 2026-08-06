package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetEventByPublicIdResponse
(
    @NotNull(message = "Event public id cannot be null")
    String publicId,

    @NotBlank(message = "Event name cannot be blank")
    String name,

    @NotBlank(message = "Event description cannot be blank")
    String description,

    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime,

    Integer maxAttendees,

    @NotBlank(message = "Event 1st address line cannot be blank")
    String addressLine1,

    String addressLine2,

    @NotBlank(message = "Event city cannot be blank")
    String city,

    @NotBlank(message = "Event state cannot be blank")
    String state,

    @NotBlank(message = "Event postal code cannot be blank")
    String postalCode,

    @NotBlank(message = "Event country cannot be blank")
    String country,

    BigDecimal latitude,

    BigDecimal longitude
)
{
}