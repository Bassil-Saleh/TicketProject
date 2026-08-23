package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.model.entities.Event;
import com.ticketproject.webapp.model.entities.EventAddress;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.enums.EventStatus;
import com.ticketproject.webapp.model.enums.EventType;
import com.ticketproject.webapp.model.enums.RegistrationStatus;
import com.ticketproject.webapp.model.repositories.EventHostRepository;
import com.ticketproject.webapp.model.repositories.EventRepository;
import com.ticketproject.webapp.services.database.CryptoService;
import com.ticketproject.webapp.services.email.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AttendeeControllerTest contains integration tests for the
 * AttendeeController, covering public event registration routes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AttendeeControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private EmailService emailService;

    private static final String BASE_PATH = ApiPaths.BASE + ApiPaths.Attendees.ROOT + ApiPaths.Attendees.REGISTRATION;
    private static final String INVITATION_PATH = ApiPaths.BASE + ApiPaths.Attendees.ROOT + ApiPaths.Attendees.INVITATION;
    private static final String TEST_EMAIL = "attendeetest@example.com";
    private static final String TEST_PASSWORD = "securePassword123";

    /**
     * Ensure that each test starts from a fresh state so that
     * when multiple tests attempt to create EventHost entities
     * with the same email address, executing those tests successively
     * won't result in a unique constraint violation.
     */
    @BeforeEach
    void setUp()
    {
        eventRepository.deleteAll();
        eventHostRepository.deleteAll();
    }

    /**
     * Creates and persists a verified EventHost for use in tests.
     *
     * @param email the email address for the event host
     * @param password the plaintext password for the event host
     * @return the persisted EventHost entity
     */
    private EventHost createVerifiedEventHost(String email, String password)
    {
        EventHost host = new EventHost.Builder()
            .firstName("Attendee")
            .lastName("Tester")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .email(email)
            .plaintextPassword(password)
            .build();
        host.generateVerificationToken();
        host.setVerified(true);
        return eventHostRepository.save(host);
    }

    /**
     * Creates and persists an Event with its EventAddress and EventSigningKey.
     *
     * @param eventHost the event host who created the event
     * @param eventType the type of the event
     * @param registrationStatus the registration status of the event
     * @param maxAttendees the maximum number of attendees
     * @return the persisted Event entity
     */
    private Event createEvent
    (
        EventHost eventHost,
        EventType eventType,
        RegistrationStatus registrationStatus,
        long maxAttendees
    )
    {
        Event event = new Event.Builder()
            .name("Test Event")
            .description("A test event description")
            .startDateTime(LocalDateTime.now().plusDays(1))
            .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
            .maxAttendees(maxAttendees)
            .eventHost(eventHost)
            .eventType(eventType)
            .publicId(UUID.randomUUID().toString())
            .build();

        event.setEventStatus(EventStatus.PUBLISHED);
        event.setRegistrationStatus(registrationStatus);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("123 Test Street")
            .city("Test City")
            .state("Test State")
            .postalCode("12345")
            .country("Test Country")
            .build();

        EventSigningKey signingKey = CryptoService.createSigningKey(event);
        event.setSigningKey(signingKey);
        event.setEventAddress(address);

        return eventRepository.save(event);
    }

    /**
     * Creates and persists an Event with its EventAddress and EventSigningKey,
     * allowing custom event status and start/end date times.
     *
     * @param eventHost the event host who created the event
     * @param eventType the type of the event
     * @param registrationStatus the registration status of the event
     * @param maxAttendees the maximum number of attendees
     * @param eventStatus the event status
     * @param startDateTime the event's start date/time
     * @param endDateTime the event's end date/time
     * @return the persisted Event entity
     */
    private Event createEvent
    (
        EventHost eventHost,
        EventType eventType,
        RegistrationStatus registrationStatus,
        long maxAttendees,
        EventStatus eventStatus,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    )
    {
        Event event = new Event.Builder()
            .name("Test Event")
            .description("A test event description")
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .maxAttendees(maxAttendees)
            .eventHost(eventHost)
            .eventType(eventType)
            .publicId(UUID.randomUUID().toString())
            .build();

        event.setEventStatus(eventStatus);
        event.setRegistrationStatus(registrationStatus);

        EventAddress address = new EventAddress.Builder()
            .addressLine1("123 Test Street")
            .city("Test City")
            .state("Test State")
            .postalCode("12345")
            .country("Test Country")
            .build();

        EventSigningKey signingKey = CryptoService.createSigningKey(event);
        event.setSigningKey(signingKey);
        event.setEventAddress(address);

        return eventRepository.save(event);
    }

    /**
     * Builds a JSON request body for public event registration.
     *
     * @param publicId the event's public ID
     * @param firstName the attendee's first name
     * @param lastName the attendee's last name
     * @param email the attendee's email address
     * @return a JSON string representing the registration request
     */
    private String buildRegistrationRequestBody
    (
        String publicId,
        String firstName,
        String lastName,
        String email
    )
    {
        return """
            {
                "publicId": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s"
            }
            """.formatted(publicId, firstName, lastName, email);
    }

    /**
     * Builds a JSON request body for private event invitation.
     *
     * @param publicId the event's public ID
     * @param firstName the attendee's first name
     * @param lastName the attendee's last name
     * @param email the attendee's email address
     * @return a JSON string representing the invitation request
     */
    private String buildInvitationRequestBody
    (
        String publicId,
        String firstName,
        String lastName,
        String email
    )
    {
        return """
            {
                "publicId": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s"
            }
            """.formatted(publicId, firstName, lastName, email);
    }

    /**
     * Performs a login request and returns the JWT from the response.
     *
     * @param email the email address
     * @param password the password
     * @return the JWT string from the login response
     * @throws Exception if the request fails
     */
    private String loginAndGetJwt(String email, String password) throws Exception
    {
        String loginPath = ApiPaths.BASE + ApiPaths.Sessions.ROOT + ApiPaths.Sessions.LOGIN;
        String requestBody = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        MvcResult result = mockMvc.perform(post(loginPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.jwt").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        int jwtStart = responseBody.indexOf("\"jwt\":\"") + 7;
        int jwtEnd = responseBody.indexOf("\"", jwtStart);
        return responseBody.substring(jwtStart, jwtEnd);
    }

    @Nested
    @DisplayName("POST /api/v1/attendees/registration")
    class CreatePublicRegistrationTests
    {
        @Test
        @DisplayName("Successful registration returns 201 and sends ticket email")
        void successfulRegistrationReturns201() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "Doe", "attendee@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration completed. A ticket with a QR code has been sent to your email."));

            // Verify that the ticket email was sent with the correct arguments.
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendTicketEmail
            (
                emailCaptor.capture(),
                tokenCaptor.capture(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                anyString()
            );

            assertThat(emailCaptor.getValue()).isEqualTo("attendee@example.com");
            assertThat(tokenCaptor.getValue()).isNotBlank();
        }

        @Test
        @DisplayName("Registration for non-existent event returns 404")
        void nonExistentEventReturns404() throws Exception
        {
            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        "non-existent-public-id", "John", "Doe", "attendee@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Registration for private event returns 400")
        void privateEventReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "Doe", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration for event with closed registration returns 400")
        void closedRegistrationReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.CLOSED, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "Doe", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration for event at max capacity returns 400")
        void maxCapacityReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 0);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "Doe", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration with blank first name returns 400")
        void blankFirstNameReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "", "Doe", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration with blank last name returns 400")
        void blankLastNameReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration with invalid email returns 400")
        void invalidEmailReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        event.getPublicId(), "John", "Doe", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Registration with blank public ID returns 400")
        void blankPublicIdReturns400() throws Exception
        {
            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildRegistrationRequestBody(
                        "", "John", "Doe", "attendee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/attendees/invitation")
    class CreatePrivateEventInvitationTests
    {
        @Test
        @DisplayName("Successful invitation creation returns 201 and sends invitation email")
        void successfulInvitationReturns201() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Invitation created. A ticket with a QR code has been sent to your recipient's email."));

            // Verify that the invitation email was sent with the correct arguments.
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendInvitationEmail
            (
                emailCaptor.capture(),
                tokenCaptor.capture(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                anyString()
            );

            assertThat(emailCaptor.getValue()).isEqualTo("invitee@example.com");
            assertThat(tokenCaptor.getValue()).isNotBlank();
        }

        @Test
        @DisplayName("Invitation creation without JWT returns 401")
        void invitationWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);

            mockMvc.perform(post(INVITATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Invitation creation for non-existent event returns 404")
        void invitationNonExistentEventReturns404() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        "non-existent-public-id", "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Invitation creation for public event returns 400")
        void invitationPublicEventReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Invitation creation by different host returns 401")
        void invitationDifferentHostReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + otherJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Invitation creation for canceled event returns 409")
        void invitationCanceledEventReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent
            (
                host,
                EventType.PRIVATE,
                RegistrationStatus.OPEN,
                100,
                EventStatus.CANCELED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
            );
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Invitation creation for event that already began returns 400")
        void invitationEventAlreadyBeganReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent
            (
                host,
                EventType.PRIVATE,
                RegistrationStatus.OPEN,
                100,
                EventStatus.PUBLISHED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(2)
            );
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Invitation creation with duplicate email returns 409")
        void invitationDuplicateEmailReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            // Create the first invitation.
            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isCreated());

            // Attempt to create a second invitation with the same email.
            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Invitation creation with blank public ID returns 400")
        void invitationBlankPublicIdReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        "", "Jane", "Smith", "invitee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Invitation creation with blank first name returns 400")
        void invitationBlankFirstNameReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "", "Smith", "invitee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Invitation creation with blank last name returns 400")
        void invitationBlankLastNameReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "", "invitee@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Invitation creation with invalid email returns 400")
        void invitationInvalidEmailReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE, RegistrationStatus.OPEN, 100);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(INVITATION_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildInvitationRequestBody(
                        event.getPublicId(), "Jane", "Smith", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }
}
