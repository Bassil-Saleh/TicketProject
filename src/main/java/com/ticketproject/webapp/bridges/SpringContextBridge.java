package com.ticketproject.webapp.bridges;

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

    /**
     * Constructor which takes a Spring application context.
     * @param context a Spring application context
     */
    public SpringContextBridge(ApplicationContext context)
    {
        SpringContextBridge.context = context;
    }

    /**
     * Given a class, retrieve an instance of that class
     * from the bridge's Spring application context.
     * @param <T> the class's generic type
     * @param clazz the class
     * @return an instance of the given class from the bridge's Spring application context
     */
    public static <T> T getBean(Class<T> clazz)
    {
        return context.getBean(clazz);
    }
}