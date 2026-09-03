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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EventControllerTest contains integration tests for the
 * EventController, covering event creation, retrieval, editing,
 * and deletion routes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventHostRepository eventHostRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private EmailService emailService;

    private static final String BASE_PATH = ApiPaths.BASE + ApiPaths.Events.ROOT;
    private static final String TEST_EMAIL = "eventtest@example.com";
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
            .firstName("Event")
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
     * @return the persisted Event entity
     */
    private Event createEvent(EventHost eventHost, EventType eventType)
    {
        Event event = new Event.Builder()
            .name("Test Event")
            .description("A test event description")
            .startDateTime(LocalDateTime.now().plusDays(1))
            .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
            .maxAttendees(100L)
            .eventHost(eventHost)
            .eventType(eventType)
            .publicId(UUID.randomUUID().toString())
            .build();

        event.setEventStatus(EventStatus.DRAFT);
        event.setRegistrationStatus(RegistrationStatus.OPEN);

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

    /**
     * Builds a JSON request body for creating a new event.
     *
     * @param name the event name
     * @param eventType the event type
     * @return a JSON string representing the create event request
     */
    private String buildCreateEventRequestBody(String name, String eventType)
    {
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).plusHours(2);

        return """
            {
                "name": "%s",
                "description": "A test event description",
                "startDateTime": "%s",
                "endDateTime": "%s",
                "eventType": "%s",
                "maxAttendees": 100,
                "addressLine1": "123 Test Street",
                "city": "Test City",
                "state": "Test State",
                "postalCode": "12345",
                "country": "Test Country"
            }
            """.formatted(name, startDateTime, endDateTime, eventType);
    }

    @Nested
    @DisplayName("POST /api/v1/events")
    class CreateEventTests
    {
        @Test
        @DisplayName("Successful public event creation returns 201")
        void successfulPublicEventCreationReturns201() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildCreateEventRequestBody("My Public Event", "PUBLIC")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Event successfully created."));
        }

        @Test
        @DisplayName("Successful private event creation returns 201")
        void successfulPrivateEventCreationReturns201() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildCreateEventRequestBody("My Private Event", "PRIVATE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Event successfully created."));
        }

        @Test
        @DisplayName("Event creation without JWT returns 401")
        void eventCreationWithoutJwtReturns401() throws Exception
        {
            mockMvc.perform(post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildCreateEventRequestBody("My Event", "PUBLIC")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Event creation with blank name returns 400")
        void blankNameReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildCreateEventRequestBody("", "PUBLIC")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Event creation with same start and end date returns 400")
        void sameStartAndEndReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime sameTime = LocalDateTime.now().plusDays(1);

            String requestBody = """
                {
                    "name": "My Event",
                    "description": "A test event description",
                    "startDateTime": "%s",
                    "endDateTime": "%s",
                    "eventType": "PUBLIC",
                    "maxAttendees": 100,
                    "addressLine1": "123 Test Street",
                    "city": "Test City",
                    "state": "Test State",
                    "postalCode": "12345",
                    "country": "Test Country"
                }
                """.formatted(sameTime, sameTime);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Event creation with start after end returns 400")
        void startAfterEndReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);

            String requestBody = """
                {
                    "name": "My Event",
                    "description": "A test event description",
                    "startDateTime": "%s",
                    "endDateTime": "%s",
                    "eventType": "PUBLIC",
                    "maxAttendees": 100,
                    "addressLine1": "123 Test Street",
                    "city": "Test City",
                    "state": "Test State",
                    "postalCode": "12345",
                    "country": "Test Country"
                }
                """.formatted(startDateTime, endDateTime);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Event creation with too-short duration returns 400")
        void tooShortDurationReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).plusMinutes(15);

            String requestBody = """
                {
                    "name": "My Event",
                    "description": "A test event description",
                    "startDateTime": "%s",
                    "endDateTime": "%s",
                    "eventType": "PUBLIC",
                    "maxAttendees": 100,
                    "addressLine1": "123 Test Street",
                    "city": "Test City",
                    "state": "Test State",
                    "postalCode": "12345",
                    "country": "Test Country"
                }
                """.formatted(startDateTime, endDateTime);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Event creation with latitude but no longitude returns 400")
        void latitudeWithoutLongitudeReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).plusHours(2);

            String requestBody = """
                {
                    "name": "My Event",
                    "description": "A test event description",
                    "startDateTime": "%s",
                    "endDateTime": "%s",
                    "eventType": "PUBLIC",
                    "maxAttendees": 100,
                    "addressLine1": "123 Test Street",
                    "city": "Test City",
                    "state": "Test State",
                    "postalCode": "12345",
                    "country": "Test Country",
                    "latitude": 40.7128
                }
                """.formatted(startDateTime, endDateTime);

            mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/events/{publicId}")
    class GetEventByPublicIdTests
    {
        @Test
        @DisplayName("Successful retrieval of published event returns 200")
        void successfulRetrievalReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            event.setEventStatus(EventStatus.PUBLISHED);
            eventRepository.save(event);

            mockMvc.perform(get(BASE_PATH + "/" + event.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(event.getPublicId()))
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.description").value("A test event description"));
        }

        @Test
        @DisplayName("Retrieval of draft event without authentication returns 404")
        void draftEventRetrievalWithoutAuthenticationReturns404() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            mockMvc.perform(get(BASE_PATH + "/" + event.getPublicId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Retrieval of draft event by its owner returns 200")
        void draftEventRetrievalByOwnerReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(get(BASE_PATH + "/" + event.getPublicId())
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(event.getPublicId()))
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.description").value("A test event description"));
        }

        @Test
        @DisplayName("Retrieval of draft event by another event host returns 404")
        void draftEventRetrievalByAnotherEventHostReturns404() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            mockMvc.perform(get(BASE_PATH + "/" + event.getPublicId())
                    .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Retrieval of canceled event without authentication returns 200")
        void canceledEventRetrievalWithoutAuthenticationReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            event.setEventStatus(EventStatus.CANCELED);
            eventRepository.save(event);

            mockMvc.perform(get(BASE_PATH + "/" + event.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(event.getPublicId()))
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.description").value("A test event description"));
        }

        @Test
        @DisplayName("Retrieval of non-existent event returns 404")
        void nonExistentEventReturns404() throws Exception
        {
            mockMvc.perform(get(BASE_PATH + "/non-existent-public-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/events")
    class GetEventsTests
    {
        @Test
        @DisplayName("Successful events retrieval returns 200")
        void successfulRetrievalReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(get(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("Events retrieval without JWT returns 401")
        void retrievalWithoutJwtReturns401() throws Exception
        {
            mockMvc.perform(get(BASE_PATH)
                    .param("count", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Events retrieval with count less than 1 returns 400")
        void countLessThanOneReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(get(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .param("count", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Events retrieval with count exceeding max returns 400")
        void countExceedingMaxReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(get(BASE_PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .param("count", "501"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/events/{publicId}")
    class DeleteEventByPublicIdTests
    {
        @Test
        @DisplayName("Successful event deletion returns 200")
        void successfulDeletionReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(delete(BASE_PATH + "/" + event.getPublicId())
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted."));
        }

        @Test
        @DisplayName("Event deletion without JWT returns 401")
        void deletionWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            mockMvc.perform(delete(BASE_PATH + "/" + event.getPublicId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Deleting non-existent event returns 404")
        void nonExistentEventReturns404() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(delete(BASE_PATH + "/non-existent-public-id")
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Deleting another host's event returns 401")
        void deletingAnotherHostsEventReturns401() throws Exception
        {
            EventHost host1 = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host1, EventType.PUBLIC);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            mockMvc.perform(delete(BASE_PATH + "/" + event.getPublicId())
                    .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/{publicId}/name")
    class EditEventNameTests
    {
        @Test
        @DisplayName("Successful name edit returns 200")
        void successfulNameEditReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "name": "Updated Event Name"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.NAME)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event name changed."));

            // Verify that the event name changed email was sent.
            verify(emailService).sendEventNameChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq("Updated Event Name")
            );
        }

        @Test
        @DisplayName("Name edit without JWT returns 401")
        void nameEditWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            String requestBody = """
                {
                    "publicId": "%s",
                    "name": "Updated Event Name"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.NAME)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Name edit with blank name returns 400")
        void blankNameReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "name": ""
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.NAME)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/{publicId}/description")
    class EditEventDescriptionTests
    {
        @Test
        @DisplayName("Successful description edit returns 200")
        void successfulDescriptionEditReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "description": "Updated event description"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DESCRIPTION)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event description changed."));

            // Verify that the event description changed email was sent.
            verify(emailService).sendEventDescriptionChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq("Updated event description")
            );
        }

        @Test
        @DisplayName("Description edit without JWT returns 401")
        void descriptionEditWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            String requestBody = """
                {
                    "publicId": "%s",
                    "description": "Updated event description"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DESCRIPTION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Description edit with blank description returns 400")
        void blankDescriptionReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "description": ""
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DESCRIPTION)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/{publicId}/address")
    class EditEventAddressTests
    {
        @Test
        @DisplayName("Successful address edit returns 200")
        void successfulAddressEditReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "addressLine1": "456 New Street",
                    "city": "New City",
                    "state": "New State",
                    "postalCode": "54321",
                    "country": "New Country"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.ADDRESS)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event address updated."));

            // Verify that the event address changed email was sent.
            verify(emailService).sendEventAddressChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq("456 New Street"),
                isNull(),
                eq("New City"),
                eq("New State"),
                eq("New Country"),
                eq("54321")
            );
        }

        @Test
        @DisplayName("Address edit without JWT returns 401")
        void addressEditWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            String requestBody = """
                {
                    "publicId": "%s",
                    "addressLine1": "456 New Street",
                    "city": "New City",
                    "state": "New State",
                    "postalCode": "54321",
                    "country": "New Country"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.ADDRESS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Address edit with blank address line returns 400")
        void blankAddressLineReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "addressLine1": "",
                    "city": "New City",
                    "state": "New State",
                    "postalCode": "54321",
                    "country": "New Country"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.ADDRESS)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/{publicId}/dates")
    class EditEventDatesTests
    {
        @Test
        @DisplayName("Successful dates edit returns 200")
        void successfulDatesEditReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime newStart = LocalDateTime.now().plusDays(5);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(5).plusHours(3);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), newStart, newEnd);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event dates updated."));

            // Verify that the event dates changed email was sent.
            verify(emailService).sendEventDatesChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("Dates edit without JWT returns 401")
        void datesEditWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            LocalDateTime newStart = LocalDateTime.now().plusDays(5);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(5).plusHours(3);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), newStart, newEnd);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Dates edit with same start and end returns 400")
        void sameStartAndEndReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime sameTime = LocalDateTime.now().plusDays(5);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), sameTime, sameTime);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Dates edit with start after end returns 400")
        void startAfterEndReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime newStart = LocalDateTime.now().plusDays(6);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(5);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), newStart, newEnd);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Dates edit with too-short duration returns 400")
        void tooShortDurationReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime newStart = LocalDateTime.now().plusDays(5);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(5).plusMinutes(15);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), newStart, newEnd);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Dates edit for published event returns 200")
        void publishedEventReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            event.setEventStatus(EventStatus.PUBLISHED);
            eventRepository.save(event);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            LocalDateTime newStart = LocalDateTime.now().plusDays(5);
            LocalDateTime newEnd = LocalDateTime.now().plusDays(5).plusHours(3);

            String requestBody = """
                {
                    "publicId": "%s",
                    "startDateTime": "%s",
                    "endDateTime": "%s"
                }
                """.formatted(event.getPublicId(), newStart, newEnd);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.DATES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event dates updated."));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/change-to-public")
    class ChangeEventToPublicEventTests
    {
        @Test
        @DisplayName("Successful change to public event returns 200")
        void successfulChangeToPublicReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 50
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("The event has been changed into a public event."));

            // Verify that the event type changed email was sent.
            verify(emailService).sendEventTypeChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq(EventType.PRIVATE),
                eq(EventType.PUBLIC)
            );
        }

        @Test
        @DisplayName("Change to public without JWT returns 401")
        void changeToPublicWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 50
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Change to public of non-existent event returns 404")
        void changeToPublicOfNonExistentEventReturns404() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "non-existent-public-id",
                    "maxAttendees": 50
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Change to public of another host's event returns 401")
        void changeToPublicOfAnotherHostsEventReturns401() throws Exception
        {
            EventHost host1 = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host1, EventType.PRIVATE);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 50
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + otherJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Change to public of published event returns 409")
        void changeToPublicOfPublishedEventReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);
            event.setEventStatus(EventStatus.PUBLISHED);
            eventRepository.save(event);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 50
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Change to public of already public event returns 200")
        void changeToPublicOfAlreadyPublicEventReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 50
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This event is already a public event."));
        }

        @Test
        @DisplayName("Change to public with blank publicId returns 400")
        void changeToPublicWithBlankPublicIdReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "",
                    "maxAttendees": 50
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Change to public with maxAttendees less than 1 returns 400")
        void changeToPublicWithMaxAttendeesLessThanOneReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 0
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Change to public with null maxAttendees returns 400")
        void changeToPublicWithNullMaxAttendeesReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": null
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PUBLIC)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/change-to-private")
    class ChangeEventToPrivateEventTests
    {
        @Test
        @DisplayName("Successful change to private event returns 200")
        void successfulChangeToPrivateReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("The event has been changed into a private event."));

            // Verify that the event type changed email was sent.
            verify(emailService).sendEventTypeChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq(EventType.PUBLIC),
                eq(EventType.PRIVATE)
            );
        }

        @Test
        @DisplayName("Change to private without JWT returns 401")
        void changeToPrivateWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            String requestBody = """
                {
                    "publicId": "%s"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Change to private of non-existent event returns 404")
        void changeToPrivateOfNonExistentEventReturns404() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "non-existent-public-id"
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Change to private of another host's event returns 401")
        void changeToPrivateOfAnotherHostsEventReturns401() throws Exception
        {
            EventHost host1 = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host1, EventType.PUBLIC);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + otherJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Change to private of published event returns 409")
        void changeToPrivateOfPublishedEventReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            event.setEventStatus(EventStatus.PUBLISHED);
            eventRepository.save(event);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Change to private of already private event returns 200")
        void changeToPrivateOfAlreadyPrivateEventReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PRIVATE);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s"
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This event is already a private event."));
        }

        @Test
        @DisplayName("Change to private with blank publicId returns 400")
        void changeToPrivateWithBlankPublicIdReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": ""
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.CHANGE_TO_PRIVATE)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/events/max-attendees")
    class EditEventMaxAttendeesTests
    {
        @Test
        @DisplayName("Successful max attendees edit returns 200")
        void successfulMaxAttendeesEditReturns200() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 200
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Max number of attendees has been updated."));

            // Verify that the event max attendees changed email was sent.
            verify(emailService).sendEventMaxAttendeesChangedEmail
            (
                anyList(),
                eq("Event Tester"),
                eq("Test Event"),
                eq(200L)
            );
        }

        @Test
        @DisplayName("Max attendees edit without JWT returns 401")
        void maxAttendeesEditWithoutJwtReturns401() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 200
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Max attendees edit of non-existent event returns 404")
        void maxAttendeesEditOfNonExistentEventReturns404() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "non-existent-public-id",
                    "maxAttendees": 200
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Max attendees edit of another host's event returns 401")
        void maxAttendeesEditOfAnotherHostsEventReturns401() throws Exception
        {
            EventHost host1 = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host1, EventType.PUBLIC);

            createVerifiedEventHost("other@example.com", TEST_PASSWORD);
            String otherJwt = loginAndGetJwt("other@example.com", TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 200
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + otherJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Max attendees edit of canceled event returns 409")
        void maxAttendeesEditOfCanceledEventReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            event.setEventStatus(EventStatus.CANCELED);
            eventRepository.save(event);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 200
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Max attendees edit with blank publicId returns 400")
        void maxAttendeesEditWithBlankPublicIdReturns400() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "",
                    "maxAttendees": 200
                }
                """;

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Max attendees edit with maxAttendees less than 1 returns 400")
        void maxAttendeesEditWithMaxAttendeesLessThanOneReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": 0
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Max attendees edit with null maxAttendees returns 400")
        void maxAttendeesEditWithNullMaxAttendeesReturns400() throws Exception
        {
            EventHost host = createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            Event event = createEvent(host, EventType.PUBLIC);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            String requestBody = """
                {
                    "publicId": "%s",
                    "maxAttendees": null
                }
                """.formatted(event.getPublicId());

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Events.MAX_ATTENDEES)
                    .header("Authorization", "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }
}
