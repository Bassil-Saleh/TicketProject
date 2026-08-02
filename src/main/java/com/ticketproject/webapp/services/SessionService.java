package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.model.repositories.SessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SessionService is a service used by controllers to handel API route requests
 * involving Session entities (i.e. login and logout).
 */
@Service
@Transactional
public class SessionService
{
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository)
    {
        this.sessionRepository = sessionRepository;
    }
}
