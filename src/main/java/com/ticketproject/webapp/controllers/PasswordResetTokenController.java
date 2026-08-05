package com.ticketproject.webapp.controllers;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.dtos.requests.CreatePasswordResetTokenRequest;
import com.ticketproject.webapp.dtos.requests.UsePasswordResetTokenRequest;
import com.ticketproject.webapp.dtos.responses.CreatePasswordResetTokenResponse;
import com.ticketproject.webapp.dtos.responses.UsePasswordResetTokenResponse;
import com.ticketproject.webapp.services.PasswordResetTokenService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * PasswordResetTokenController is a REST controller that routes requests
 * concerning PasswordResetToken entities to different API routes.
 */
@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.PasswordResetTokens.ROOT)
public class PasswordResetTokenController
{
    private final PasswordResetTokenService passwordResetTokenService;

    public PasswordResetTokenController(PasswordResetTokenService passwordResetTokenService)
    {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    /**
     * Handles a request to reset the password of an event host account.
     * 
     * @param request the request body
     * @return a CreatePasswordResetTokenResponse on success
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePasswordResetTokenResponse createPasswordResetToken
    (
        @Valid
        @RequestBody CreatePasswordResetTokenRequest request
    )
    {
        return passwordResetTokenService.createPasswordResetToken(request);
    }

    /**
     * Handles a request to use a password reset token.
     * 
     * @param request the request body
     * @return a UsePasswordResetTokenResponse on success
     */
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public UsePasswordResetTokenResponse usePasswordResetToken
    (
        @Valid
        @RequestBody UsePasswordResetTokenRequest request
    )
    {
        return passwordResetTokenService.usePasswordResetToken(request);
    }
}
