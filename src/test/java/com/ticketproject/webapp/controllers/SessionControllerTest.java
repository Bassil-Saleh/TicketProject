package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.repositories.EventHostRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SessionControllerTest contains integration tests for the
 * SessionController, covering login, logout, and logout-all-devices
 * routes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SessionControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventHostRepository eventHostRepository;

    @MockitoBean
    private EmailService emailService;

    private static final String BASE_PATH = ApiPaths.BASE + ApiPaths.Sessions.ROOT;
    private static final String TEST_EMAIL = "sessiontest@example.com";
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
        eventHostRepository.deleteAll();
    }

    /**
     * Creates and persists a verified EventHost for use in session tests.
     *
     * @param email the email address for the event host
     * @param password the plaintext password for the event host
     * @return the persisted EventHost entity
     */
    private EventHost createVerifiedEventHost(String email, String password)
    {
        EventHost host = new EventHost.Builder()
            .firstName("Session")
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
     * Builds a JSON request body for the login endpoint.
     *
     * @param email the email address
     * @param password the password
     * @return a JSON string representing the login request
     */
    private String buildLoginRequestBody(String email, String password)
    {
        return """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);
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
        MvcResult result = mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildLoginRequestBody(email, password)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.jwt").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // Extract the JWT from the JSON response.
        int jwtStart = responseBody.indexOf("\"jwt\":\"") + 7;
        int jwtEnd = responseBody.indexOf("\"", jwtStart);
        return responseBody.substring(jwtStart, jwtEnd);
    }

    @Nested
    @DisplayName("POST /api/v1/sessions/login")
    class LoginTests
    {
        @Test
        @DisplayName("Successful login returns a JWT")
        void successfulLoginReturnsJwt() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").isNotEmpty());
        }

        @Test
        @DisplayName("Login with wrong password returns 401")
        void wrongPasswordReturns401() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody(TEST_EMAIL, "wrongPassword123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Login with non-existent email returns 401")
        void nonExistentEmailReturns401() throws Exception
        {
            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody("nonexistent@example.com", TEST_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Login with unverified account returns 403")
        void unverifiedAccountReturns403() throws Exception
        {
            EventHost unverifiedHost = new EventHost.Builder()
                .firstName("Unverified")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("unverified@example.com")
                .plaintextPassword(TEST_PASSWORD)
                .build();
            unverifiedHost.generateVerificationToken();
            unverifiedHost = eventHostRepository.save(unverifiedHost);

            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody("unverified@example.com", TEST_PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Login with inactive account returns 409")
        void inactiveAccountReturns409() throws Exception
        {
            EventHost host = createVerifiedEventHost("inactive@example.com", TEST_PASSWORD);
            host.setActive(false);
            eventHostRepository.save(host);

            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody("inactive@example.com", TEST_PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Login with blank email returns 400")
        void blankEmailReturns400() throws Exception
        {
            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody("", TEST_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Login with invalid email format returns 400")
        void invalidEmailFormatReturns400() throws Exception
        {
            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody("not-an-email", TEST_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Login with blank password returns 400")
        void blankPasswordReturns400() throws Exception
        {
            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody(TEST_EMAIL, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Login with too-short password returns 400")
        void tooShortPasswordReturns400() throws Exception
        {
            mockMvc.perform(post(BASE_PATH + ApiPaths.Sessions.LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildLoginRequestBody(TEST_EMAIL, "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/sessions/logout")
    class LogoutTests
    {
        @Test
        @DisplayName("Successful logout returns 200")
        void successfulLogoutReturns200() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT)
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You have been logged out."));
        }

        @Test
        @DisplayName("Logout without JWT returns 401")
        void logoutWithoutJwtReturns401() throws Exception
        {
            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Logout with invalid JWT returns 401")
        void logoutWithInvalidJwtReturns401() throws Exception
        {
            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT)
                    .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/sessions/logout-all-devices")
    class LogoutAllDevicesTests
    {
        @Test
        @DisplayName("Successful logout from all devices returns 200")
        void successfulLogoutAllDevicesReturns200() throws Exception
        {
            createVerifiedEventHost(TEST_EMAIL, TEST_PASSWORD);
            String jwt = loginAndGetJwt(TEST_EMAIL, TEST_PASSWORD);

            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT_ALL_DEVICES)
                    .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You have been logged out from all of your devices."));
        }

        @Test
        @DisplayName("Logout all devices without JWT returns 401")
        void logoutAllDevicesWithoutJwtReturns401() throws Exception
        {
            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT_ALL_DEVICES))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Logout all devices with invalid JWT returns 401")
        void logoutAllDevicesWithInvalidJwtReturns401() throws Exception
        {
            mockMvc.perform(patch(BASE_PATH + ApiPaths.Sessions.LOGOUT_ALL_DEVICES)
                    .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }
    }
}