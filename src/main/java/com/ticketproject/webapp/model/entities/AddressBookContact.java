package com.ticketproject.webapp.model.entities;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.*;

@Entity
@Table(name = AppConstants.Database.AddressBookContacts.TableNames.TABLE_NAME)
public class AddressBookContact
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}