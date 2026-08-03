package com.ticketproject.webapp.config;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.filters.JwtAuthenticationFilter;
import com.ticketproject.webapp.model.repositories.SessionRepository;
import com.ticketproject.webapp.services.HashingService;
import com.ticketproject.webapp.services.JwtService;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FilterConfig is a Spring configuration class that registers
 * servlet filters with the application context.
 */
@Configuration
public class FilterConfig
{
    /**
     * Registers the JwtAuthenticationFilter to intercept all API requests.
     * 
     * The filter is configured to run on all paths under the API base path
     * (e.g., /api/v1/*). It will attempt to authenticate requests using
     * JWTs from the Authorization header.
     * 
     * @param jwtService the service for validating JWTs
     * @param hashingService the service for hashing session tokens
     * @param sessionRepository the repository for looking up Session entities
     * @return a FilterRegistrationBean configured with the JwtAuthenticationFilter
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter
    (
        JwtService jwtService,
        HashingService hashingService,
        SessionRepository sessionRepository
    )
    {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtService, hashingService, sessionRepository));
        registrationBean.addUrlPatterns(ApiPaths.BASE + "/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}