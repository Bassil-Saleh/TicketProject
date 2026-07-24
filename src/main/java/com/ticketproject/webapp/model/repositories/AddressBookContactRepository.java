package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.AddressBookContact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressBookContactRepository extends JpaRepository<AddressBookContact, Long>
{
}
