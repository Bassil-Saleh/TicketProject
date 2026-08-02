package com.ticketproject.webapp.constants;

import java.time.format.DateTimeFormatter;

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

        /**
         * AddressBookContacts holds constants related to the address book contacts database table.
         */
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

        /**
         * PasswordResetTokens holds constants related to the password reset tokens database table.
         */
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
                public static final String COLUMN_TOKEN_HASH = "token_hash";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_USED = "used";
                public static final String COLUMN_EXPIRES = "expires";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int TOKEN_LENGTH = 36;
                public static final int TOKEN_DURATION_HOURS = 1;
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String COLUMN_TOKEN_HASH = "VARBINARY(32)";
            }
        }

        /**
         * Attendees holds constants related to the attendees database table.
         */
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
                public static final String COLUMN_EMAIL_BLIND_INDEX = "email_blind_index";
                public static final String COLUMN_CREATED = "created";
            }

            public static final class MappedByNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private MappedByNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String MAPPED_BY_ATTENDEE = "attendee";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int NAME_LENGTH = 100;
                public static final int EMAIL_LENGTH = 254;
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String COLUMN_EMAIL = "VARBINARY(512)";
                public static final String COLUMN_EMAIL_BLIND_INDEX = "VARBINARY(32)";
            }
        }

        /**
         * BlockedRegistrations holds constants related to the blocked registrations database table.
         */
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

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String COLUMN_REASON = "TEXT";
            }
        }

        /**
         * TicketScans holds constants related to the ticket scans database table.
         */
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

            public static final class Sizes
            {

                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int DEVICE_INFO_LENGTH = 255;
            }
        }

        /**
         * Tickets holds constants related to the tickets database table.
         */
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

            public static final class MappedByNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private MappedByNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String MAPPED_BY_TICKET = "ticket";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final int PUBLIC_TOKEN_LENGTH = 512;
                public static final int TOKEN_IDENTIFIER_LENGTH = 36;
            }
        }

        /**
         * EventHosts holds constants related to the event hosts database table.
         */
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
                public static final String COLUMN_EMAIL_BLIND_INDEX = "email_blind_index";
                public static final String COLUMN_PASSWORD_HASH = "password_hash";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_LAST_LOGIN = "last_login";
                public static final String COLUMN_LAST_UPDATED = "last_updated";
                public static final String COLUMN_ACTIVE = "active";
                public static final String COLUMN_VERIFIED = "verified";
                public static final String COLUMN_VERIFICATION_KEY_HASH = "verification_key_hash";
                public static final String COLUMN_VERIFICATION_EXPIRES = "verification_expires";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int NAME_LENGTH = 100;
                public static final int VERIFICATION_KEY_LENGTH = 36;
                public static final int VERIFICATION_DURATION_HOURS = 1;
            }

            public static final class MappedByNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private MappedByNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String MAPPED_BY_EVENT_HOST = "eventHost";
                public static final String MAPPED_BY_BLOCKED_BY = "blockedBy";
                public static final String MAPPED_BY_SCANNED_BY = "scannedBy";
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String COLUMN_DATE_OF_BIRTH = "VARBINARY(128)";
                public static final String COLUMN_EMAIL = "VARBINARY(512)";
                public static final String COLUMN_EMAIL_BLIND_INDEX = "VARBINARY(32)";
                public static final String COLUMN_PASSWORD_HASH = "VARCHAR(255)";
                public static final String COLUMN_VERIFICATION_KEY_HASH = "VARBINARY(32)";
            }
        }

        /**
         * Events holds constants related to the events database table.
         */
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
                public static final String COLUMN_EVENT_ADDRESS_ID = "event_address_id";
                public static final String COLUMN_START_DATE_TIME = "start_date_time";
                public static final String COLUMN_END_DATE_TIME = "end_date_time";
                public static final String COLUMN_EVENT_TYPE = "event_type";
                public static final String COLUMN_MAX_ATTENDEES = "max_attendees";
                public static final String COLUMN_REGISTRATION_STATUS = "registration_status";
                public static final String COLUMN_EVENT_STATUS = "event_status";
            }

            public static final class MappedByNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private MappedByNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String MAPPED_BY_EVENT = "event";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int PUBLIC_ID_LENGTH = 36;
                public static final int NAME_LENGTH = 255;
                public static final int ADDRESS_LINE_LENGTH = 255;
                public static final int CITY_LENGTH = 100;
                public static final int STATE_LENGTH = 100;
                public static final int POSTAL_CODE_LENGTH = 20;
                public static final int COUNTRY_LENGTH = 100;
                public static final int LATITUDE_PRECISION = 10;
                public static final int LATITUDE_SCALE = 7;
                public static final int LONGITUDE_PRECISION = 10;
                public static final int LONGITUDE_SCALE = 7;
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String COLUMN_DESCRIPTION = "TEXT";
            }
        }

        /**
         * EventAddresses holds constants related to the event addresses database table.
         */
        public static final class EventAddresses
        {
            /**
             * Private constructor. Not meant to be used.
             * @throws UnsupportedOperationException
             */
            private EventAddresses()
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
                public static final String TABLE_NAME = "event_addresses";
                public static final String COLUMN_ID = "id";
                public static final String COLUMN_CREATED = "created";
                public static final String COLUMN_LAST_UPDATED = "last_updated";
                public static final String COLUMN_ADDRESS_LINE_1 = "address_line_1";
                public static final String COLUMN_ADDRESS_LINE_2 = "address_line_2";
                public static final String COLUMN_CITY = "city";
                public static final String COLUMN_STATE = "state";
                public static final String COLUMN_POSTAL_CODE = "postal_code";
                public static final String COLUMN_COUNTRY = "country";
                public static final String COLUMN_LATITUDE = "latitude";
                public static final String COLUMN_LONGITUDE = "longitude";
            }

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int PUBLIC_ID_LENGTH = 36;
                public static final int NAME_LENGTH = 255;
                public static final int ADDRESS_LINE_LENGTH = 255;
                public static final int CITY_LENGTH = 100;
                public static final int STATE_LENGTH = 100;
                public static final int POSTAL_CODE_LENGTH = 20;
                public static final int COUNTRY_LENGTH = 100;
                public static final int LATITUDE_PRECISION = 10;
                public static final int LATITUDE_SCALE = 7;
                public static final int LONGITUDE_PRECISION = 10;
                public static final int LONGITUDE_SCALE = 7;
            }

            public static final class MappedByNames
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private MappedByNames()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String MAPPED_BY_EVENT_ADDRESS = "eventAddress";
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String COLUMN_ADDRESS_LINE = "VARBINARY(1024)";
                public static final String COLUMN_CITY = "VARBINARY(1024)";
                public static final String COLUMN_STATE = "VARBINARY(1024)";
                public static final String COLUMN_POSTAL_CODE = "VARBINARY(1024)";
                public static final String COLUMN_COUNTRY = "VARBINARY(1024)";
                public static final String COLUMN_LATITUDE = "VARBINARY(1024)";
                public static final String COLUMN_LONGITUDE = "VARBINARY(1024)";
            }
        }

        /**
         * Sessions holds constants related to the sessions database table.
         */
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

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final int TOKEN_HASH_LENGTH = 64;
                public static final int IP_ADDRESS_LENGTH = 45;
                public static final int USER_AGENT_LENGTH = 512;
                public static final int LOGIN_SESSION_DURATION_HOURS = 6;
            }

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }
                public static final String COLUMN_TOKEN_HASH = "VARBINARY(32)";
            }
        }

        /**
         * EventSigningKeys holds constants related to the event signing keys database table.
         */
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

            public static final class Definitions
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Definitions()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final String COLUMN_PRIVATE_KEY = "VARBINARY(4096)";
                public static final String COLUMN_PUBLIC_KEY = "VARBINARY(4096)";
            }
        }
    }

    /**
     * The Crypto inner class holds constants related to things such as:
     * - Choice of encryption algorithm
     * - Choice of hash algorithm
     * - Key lengths
     * - Hash lengths
     * - How complex data such as dates and times should be
     *   represented in the database during encryption/decryption
     */
    public static final class Crypto
    {
        /**
         * Private constructor. Not meant to be used.
         * @throws UnsupportedOperationException
         */
        private Crypto()
        {
            throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
        }

        public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
        public static final String SECRET_KEY_ALGORITHM = "AES";
        public static final String HASH_ALGORITHM = "SHA-256";
        public static final String PUBLIC_PRIVATE_KEY_ALGORITHM = "RSA";
        public static final int PUBLIC_PRIVATE_KEY_SIZE_PROD = 4096;
        public static final int PUBLIC_PRIVATE_KEY_SIZE_TEST = 1024;
        public static final int GCM_IV_LENGTH_BYTES = 12;
        public static final int GCM_TAG_LENGTH_BITS = 128;
        public static final int HASH_LENGTH_BYTES = 32;
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
        public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        public static final String BLIND_INDEX_KEY_ALGORITHM = "HmacSHA256";
    }

    public static final class DTO
    {
        /**
         * Private constructor. Not meant to be used.
         * @throws UnsupportedOperationException
         */
        private DTO()
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

            public static final class Sizes
            {
                /**
                 * Private constructor. Not meant to be used.
                 * @throws UnsupportedOperationException
                 */
                private Sizes()
                {
                    throw new UnsupportedOperationException(NO_CONSTRUCTION_MSG);
                }

                public static final int MIN_PASSWORD_LENGTH = 12;
                public static final int MAX_PASSWORD_LENGTH = 128;
                public static final int MAX_NAME_LENGTH = 100;
                public static final int MAX_EMAIL_LENGTH = 254;
                public static final int VERIFICATION_LINK_DURATION_HOURS = 1;
            }
        }
    }
}