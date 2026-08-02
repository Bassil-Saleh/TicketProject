package com.ticketproject.webapp.constants;

/**
 * ApiPaths is a utility class for holding String constants to
 * construct various API routes used throughout this project.
 */
public final class ApiPaths
{
    private static final String NO_CONSTRUCTION_MSG = "This is a utility class which is not meant to be instantiated.";
    /**
     * Private constructor. Not meant to be used.
     * @throws UnsupportedOperationException
     */
    private ApiPaths()
    {
        throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
    }

    public static final String API = "/api";
    public static final String VERSION = "/v1";
    public static final String BASE = API + VERSION;

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

        public static final String ROOT = "/event-hosts";
        public static final String VERIFICATION = "/verification";
        public static final String PROFILE = "/profile";
        public static final String FULL_NAME = "/name";
        public static final String EMAIL = "/email";
        public static final String PASSWORD = "/password";
    }

    public static final class Sessions
    {
        /**
         * Private constructor. Not meant to be used.
         * @throws UnsupportedOperationException
         */
        private Sessions()
        {
            throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
        }

        public static final String ROOT = "/sessions";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String LOGOUT_ALL_DEVICES = "/logout-all-devices";
    }
}
