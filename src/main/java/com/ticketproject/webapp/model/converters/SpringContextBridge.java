package com.ticketproject.webapp.model.converters;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Since the JPA converters for handling encryption/decryption of database fields
 * are not Spring beans by deault, SpringContextBridge is used to provide a
 * bridge between them and the Spring framework.
 */
@Component
public class SpringContextBridge
{
    private static ApplicationContext context;

    public SpringContextBridge(ApplicationContext context)
    {
        SpringContextBridge.context = context;
    }

    public static <T> T getBean(Class<T> clazz)
    {
        return context.getBean(clazz);
    }
}