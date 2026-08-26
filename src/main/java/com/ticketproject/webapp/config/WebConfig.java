package com.ticketproject.webapp.config;

import java.util.List;

import com.ticketproject.webapp.constants.ApiPaths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig is a Spring configuration class that registers global web MVC
 * configuration for the application, such as an optional Cross-Origin
 * Resource Sharing (CORS) policy.
 *
 * By default no CORS mappings are registered, which keeps the API same-origin
 * only. This is the correct setup when the API is served from behind a
 * reverse proxy (such as Caddy) on the same origin as the frontend.
 *
 * To allow cross-origin requests (for example when running a separate Vite
 * dev server against this backend), set the app.config.cors-allowed-origins
 * property to a comma-separated list of allowed origins.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer
{
    /**
     * The list of origins allowed to make cross-origin requests to the API.
     * Empty by default (same-origin only).
     */
    private final List<String> allowedOrigins;

    /**
     * Constructs a new WebConfig.
     *
     * @param allowedOrigins comma-separated list of allowed CORS origins;
     * empty by default, which disables CORS entirely (same-origin only)
     */
    public WebConfig(@Value("${app.config.cors-allowed-origins:}") List<String> allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Registers CORS mappings for all API routes when one or more allowed
     * origins are configured. When the allowed origins list is empty (the
     * default), no mappings are registered and the API remains same-origin
     * only.
     *
     * @param registry the CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        List<String> origins = allowedOrigins == null
            ? List.of()
            : allowedOrigins.stream()
                // .map(String::trim)
                .map((str) -> str.trim())
                .filter(origin -> !origin.isEmpty())
                .toList();

        if (origins.isEmpty())
        {
            return;
        }

        registry.addMapping(ApiPaths.BASE + "/**")
            .allowedOrigins(origins.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
