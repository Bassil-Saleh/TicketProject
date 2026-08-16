package com.ticketproject.webapp.constants;

/**
 * FrontendPaths is a utility class for holding String constants
 * used to construct various frontend routes used throughout
 * this project.
 */
public final class FrontendPaths
{
    private static final String NO_CONSTRUCTION_MSG = "This is a utility class which is not meant to be instantiated.";
    /**
     * Private constructor. Not meant to be used.
     * @throws UnsupportedOperationException
     */
    private FrontendPaths()
    {
        throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
    }

    public static final class EventHosts
    {
        /**
         * Private constructor. Not meant to be used.
         * @throws UnsupportedOperationException
         */
        private EventHosts()
        {
            throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
        }

        public static final String VERIFY_ACCOUNT = "/verify-account";
    }
}
