package com.ticketproject.webapp.constants;

/**
 * AppConstants is a utility class for holding different constants
 * used throughout this project.
 */
public final class AppConstants
{
    private static final String NO_CONSTRUCTION_MSG = "This is a utility class which is not meant to be instantiated.";
    /**
     * Private constructor. Not meant to be used.
     * @throws UnsupportedOperationException
     */
    private AppConstants()
    {
        throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
    }

    /**
     * The Database inner class holds constants
     * related to the application's SQL database.
     */
    public static final class Database
    {
        /**
         * Private constructor. Not meant to be used.
         * @throws UnsupportedOperationException
         */
        private Database()
        {
            throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
        }

        public static final class AddressBookContacts
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private AddressBookContacts()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "address_book_contacts";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_ATTENDEE_ID = "attendee_id";
                public static final String COLUMN_EVENT_HOST_ID = "event_host_id";
                public static final String COLUMN_CREATED = "created";
            }
        }

        public static final class PasswordResetTokens
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private PasswordResetTokens()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "password_reset_tokens";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_EVENT_HOST_ID = "event_host_id";
                public static final String COLUMN_TOKEN = "token";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_USED = "used";
                public static final String COLUMN_EXPIRES = "expires";
            }
        }

        public static final class Attendees
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private Attendees()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "attendees";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_FIRST_NAME = "first_name";
                public static final String COLUMN_MIDDLE_NAME = "middle_name";
                public static final String COLUMN_LAST_NAME = "last_name";
                public static final String COLUMN_EMAIL = "email";
                public static final String COLUMN_CREATED = "created";
            }
        }

        public static final class BlockedRegistrations
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private BlockedRegistrations()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "blocked_registrations";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_ATTENDEE_ID = "attendee_id";
                public static final String COLUMN_EVENT_ID = "event_id";
                public static final String COLUMN_BLOCKED_BY = "blocked_by";
                public static final String COLUMN_REASON = "reason";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_REVOKED = "revoked";
            }
        }

        public static final class TicketScans
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private TicketScans()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "ticket_scans";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_TICKET_ID = "ticket_id";
                public static final String COLUMN_SCANNED_BY = "scanned_by";
                public static final String COLUMN_SCANNED_AT = "scanned_at";
                public static final String COLUMN_DEVICE_INFO = "device_info";
            }
        }

        public static final class Tickets
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private Tickets()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "tickets";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_PUBLIC_TOKEN = "public_token";
                public static final String COLUMN_TOKEN_IDENTIFIER = "token_identifier";
                public static final String COLUMN_ATTENDEE_ID = "attendee_id";
                public static final String COLUMN_EVENT_ID = "event_id";
                public static final String COLUMN_PRESENT = "present";
                public static final String COLUMN_INVITATION_STATUS = "invitation_status";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_LAST_UPDATED = "last_updated";
                public static final String COLUMN_DELETED_AT = "deleted_at";
            }
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

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "event_hosts";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_FIRST_NAME = "first_name";
                public static final String COLUMN_MIDDLE_NAME = "middle_name";
                public static final String COLUMN_LAST_NAME = "last_name";
                public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
                public static final String COLUMN_EMAIL = "email";
                public static final String COLUMN_PASSWORD_HASH = "password_hash";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_LAST_LOGIN = "last_login";
                public static final String COLUMN_LAST_UPDATED = "last_updated";
                public static final String COLUMN_ACTIVE = "active";
                public static final String COLUMN_VERIFIED = "verified";
                public static final String COLUMN_VERIFICATION_KEY = "verification_key";
                public static final String COLUMN_VERIFICATION_EXPIRES = "verification_expires";
            }
        }

        public static final class Events
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private Events()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "events";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_PUBLIC_ID = "public_id";
                public static final String COLUMN_EVENT_HOST_ID = "event_host_id";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_LAST_UPDATED = "last_updated";
                public static final String COLUMN_NAME = "name";
                public static final String COLUMN_DESCRIPTION = "description";
                public static final String COLUMN_ADDRESS_LINE_1 = "address_line_1";
                public static final String COLUMN_ADDRESS_LINE_2 = "address_line_2";
                public static final String COLUMN_CITY = "city";
                public static final String COLUMN_STATE = "state";
                public static final String COLUMN_POSTAL_CODE = "postal_code";
                public static final String COLUMN_COUNTRY = "country";
                public static final String COLUMN_LATITUDE = "latitude";
                public static final String COLUMN_LONGITUDE = "longitude";
                public static final String COLUMN_START_DATE_TIME = "start_date_time";
                public static final String COLUMN_END_DATE_TIME = "end_date_time";
                public static final String COLUMN_EVENT_TYPE = "event_type";
                public static final String COLUMN_MAX_ATTENDEES = "max_attendees";
                public static final String COLUMN_REGISTRATION_STATUS = "registration_status";
                public static final String COLUMN_EVENT_STATUS = "event_status";
            }
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

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "sessions";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_EVENT_HOST_ID = "event_host_id";
                public static final String COLUMN_TOKEN_HASH = "token_hash";
                public static final String COLUMN_CLIENT_TYPE = "client_type";
                public static final String COLUMN_IP_ADDRESS = "ip_address";
                public static final String COLUMN_USER_AGENT = "user_agent";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_EXPIRES = "expires";
                public static final String COLUMN_REVOKED = "revoked";
            }
        }

        public static final class EventSigningKeys
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private EventSigningKeys()
            {
                throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
            }

            public static final class TableNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private TableNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String TABLE_NAME = "event_signing_keys";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_EVENT_ID = "event_id";
                public static final String COLUMN_PRIVATE_KEY = "private_key";
                public static final String COLUMN_PUBLIC_KEY = "public_key";
                public static final String COLUMN_CREATED = "created";
            }
        }
    }
}