package com.ticketproject.webapp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketproject.webapp.model.repositories.AttendeeRepository;

/**
 * AttendeeService is a service used by controllers to handle
 * API route requests involving Attendee entities
 * (i.e. public event registrations, private event invitations).
 */
@Service
@Transactional
public class AttendeeService
{
    private final AttendeeRepository attendeeRepository;

    public AttendeeService(AttendeeRepository attendeeRepository)
    {
        this.attendeeRepository = attendeeRepository;
    }
}
