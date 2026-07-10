package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;

@Entity
@Table(name = AppConstants.Database.Sessions.TableNames.TABLE_NAME)
public class Session
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}