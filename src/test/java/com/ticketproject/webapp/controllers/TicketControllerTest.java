package com.ticketproject.webapp.controllers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.ticketproject.webapp.services.email.EmailService;

/**
 * TicketControllerTest contains integration tests for the TicketController,
 * covering routes that perform work on Ticket entities.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TicketControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;
}
