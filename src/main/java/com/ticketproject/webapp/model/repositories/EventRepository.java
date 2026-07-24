package com.ticketproject.webapp.model.repositories;

import com.ticketproject.webapp.model.entities.Event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>
{
}
