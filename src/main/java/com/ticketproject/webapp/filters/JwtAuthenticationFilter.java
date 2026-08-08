package com.ticketproject.webapp.filters;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.EventHost;
import com.ticketproject.webapp.model.entities.Session;
import com.ticketproject.webapp.model.repositories.SessionRepository;
import com.ticketproject.webapp.services.database.HashingService;
import com.ticketproject.webapp.services.jwt.JwtService;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Optional;

/**
 * JwtAuthenticationFilter is a Jakarta EE servlet filter that intercepts
 * incoming HTTP requests and attempts to authenticate them using a JWT
 * provided in the Authorization header.
 * 
 * If a valid JWT is found:
 * - The JWT signature is verified
 * - The raw session token is extracted from the JWT subject
 * - The token is hashed and used to look up the Session entity
 * - If the session is active, the associated EventHost is set as a request attribute
 * 
 * If no JWT is found or the JWT is invalid, the request proceeds without
 * authentication. Controllers that require authentication should check for
 * the "authenticatedEventHost" request attribute and return 401 if absent.
 */
public class JwtAuthenticationFilter implements Filter
{
    private final JwtService jwtService;
    private final HashingService hashingService;
    private final SessionRepository sessionRepository;

    /**
     * Constructs a new JwtAuthenticationFilter with the required dependencies.
     * @param jwtService the service for validating JWTs
     * @param hashingService the service for hashing session tokens
     * @param sessionRepository the repository for looking up Session entities
     */
    public JwtAuthenticationFilter
    (
        JwtService jwtService,
        HashingService hashingService,
        SessionRepository sessionRepository
    )
    {
        this.jwtService = jwtService;
        this.hashingService = hashingService;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Filters incoming requests by attempting to authenticate them using a JWT.
     * 
     * @param request the incoming servlet request
     * @param response the servlet response
     * @param chain the filter chain to continue processing
     * @throws IOException if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter
    (
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest httpRequest)
        {
            authenticateRequest(httpRequest);
        }
        chain.doFilter(request, response);
    }

    /**
     * Attempts to authenticate the request by extracting and validating
     * the JWT from the Authorization header.
     * 
     * @param httpRequest the HTTP servlet request to authenticate
     */
    private void authenticateRequest(HttpServletRequest httpRequest)
    {
        String authHeader = httpRequest.getHeader(AppConstants.Jwt.Filter.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(AppConstants.Jwt.Filter.BEARER_PREFIX))
        {
            return;
        }

        String jwt = authHeader.substring(AppConstants.Jwt.Filter.BEARER_PREFIX.length());

        try
        {
            // Validate the JWT and extract the raw session token.
            String rawSessionToken = jwtService.validateToken(jwt);

            // Hash the raw session token and look up the Session entity.
            byte[] tokenHash = hashingService.hashToken(rawSessionToken);
            Optional<Session> foundSession = sessionRepository.findByTokenHash(tokenHash);

            if (foundSession.isEmpty())
            {
                return;
            }

            Session session = foundSession.get();

            // Check that the session is still active (not expired or revoked).
            if (!session.isActive())
            {
                return;
            }

            // Set the authenticated EventHost and Session as request attributes.
            EventHost eventHost = session.getEventHost();
            httpRequest.setAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_EVENT_HOST_ATTRIBUTE, eventHost);
            httpRequest.setAttribute(AppConstants.Jwt.Filter.AUTHENTICATED_SESSION_ATTRIBUTE, session);
        }
        catch (JwtException e)
        {
            // JWT is invalid, expired, or tampered with.
            // Let the request proceed without authentication.
        }
    }
}