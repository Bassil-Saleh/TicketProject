package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * GetEventsRequest specifies what a valid request to fetch info
 * on events made by a logged in event host should look like.
 * 
 * @param count number of events to retrieve
 */
public record GetEventsRequest
(
    @NotNull(message = "Number of events to retrieve cannot be null")
    @Min
    (
        value = 1,
        message = "Number of events to retrieve must be at least 1"
    )
    @Max
    (
        value = AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT,
        message = "Number of events to retrieve cannot be more than " +
        AppConstants.DTO.Events.Sizes.MAX_GET_EVENTS_COUNT
    )
    Long count
)
{
}
