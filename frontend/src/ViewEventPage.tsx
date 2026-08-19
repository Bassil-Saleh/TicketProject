import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';

/** Shape of a single event returned by GET /api/v1/events/{publicId}. */
interface EventInfo {
    publicId: string;
    name: string;
    description: string;
    startDateTime: string;
    endDateTime: string;
    eventType: string;
    numberOfRegisteredAttendees: number;
    maxAttendees: number;
    addressLine1: string;
    addressLine2: string | null;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    latitude: number | null;
    longitude: number | null;
}

/**
 * ViewEventPage is for implementing the page that lets a user
 * view an event's page given the event's public ID.
 * @returns JSX for the site's view event page
 */
export function ViewEventPage() {
    const { publicId } = useParams<{ publicId: string }>();

    const [event, setEvent] = useState<EventInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState('');

    /**
     * Fetches the event's details using its public ID via
     * GET /api/v1/events/{publicId}. This endpoint does not
     * require authentication.
     */
    const fetchEvent = async () => {
        if (!publicId) return;
        setIsLoading(true);
        setLoadError('');
        try {
            const response = await fetch(`/api/v1/events/${encodeURIComponent(publicId)}`);
            const data = await response.json();

            if (response.ok) {
                setEvent(data);
            } else {
                setLoadError(data.message || 'Failed to load the event.');
            }
        } catch {
            setLoadError('An unexpected error occurred while loading the event.');
        } finally {
            setIsLoading(false);
        }
    };

    // Fetch the event on mount.
    useEffect(() => {
        fetchEvent();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [publicId]);

    /**
     * Formats an ISO date-time string into a human-readable format.
     */
    const formatDateTime = (isoString: string): string => {
        try {
            return new Date(isoString).toLocaleString(undefined, {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
            });
        } catch {
            return isoString;
        }
    };

    /**
     * Builds a multi-line address display from the event data.
     */
    const getAddressLines = (): string[] => {
        if (!event) return [];
        const lines: string[] = [event.addressLine1];
        if (event.addressLine2) {
            lines.push(event.addressLine2);
        }
        lines.push(`${event.city}, ${event.state} ${event.postalCode}`);
        lines.push(event.country);
        return lines;
    };

    /**
     * Determines whether registration for the event is closed.
     * Since the API does not expose an explicit registration
     * status field, registration is treated as closed once the
     * event's end date/time has passed.
     */
    const isRegistrationClosed = (): boolean => {
        if (!event) return true;
        return new Date(event.endDateTime).getTime() <= Date.now();
    };

    if (isLoading) {
        return (
            <main className="status-page">
                <div className="spinner spinner--lg" aria-label="Loading event" />
                <h2 className="status-page__title">Loading Event...</h2>
            </main>
        );
    }

    if (loadError || !event) {
        return (
            <main className="status-page">
                <div className="status-page__icon" aria-hidden="true">🔍</div>
                <h2 className="status-page__title">Event Not Found</h2>
                <p className="status-page__message">
                    {loadError || 'The event you are looking for does not exist or may have been removed.'}
                </p>
                <Link to="/" className="btn btn--outline btn--lg">
                    Back to Home
                </Link>
            </main>
        );
    }

    const registrationClosed = isRegistrationClosed();

    return (
        <main className="page-container">
            <div className="view-event">
                <div className="view-event__header">
                    <div>
                        <span className="view-event__type-badge">
                            {event.eventType === 'PUBLIC' ? 'Public Event' : 'Private Event'}
                        </span>
                        <h1 className="view-event__title">{event.name}</h1>
                    </div>

                    {/* Only public events show a Register button. */}
                    {event.eventType === 'PUBLIC' && (
                        <div className="view-event__register">
                            <Link
                                to={registrationClosed ? '#' : `/events/${event.publicId}/register`}
                                className={`btn btn--accent btn--lg${registrationClosed ? ' btn--disabled' : ''}`}
                                aria-disabled={registrationClosed}
                                title={registrationClosed ? 'Registration is closed' : undefined}
                                onClick={(e) => {
                                    if (registrationClosed) {
                                        e.preventDefault();
                                    }
                                }}
                            >
                                Register
                            </Link>
                        </div>
                    )}
                </div>

                <div className="view-event__body">
                    <section className="view-event__section">
                        <h2 className="view-event__section-title">About This Event</h2>
                        <p className="view-event__description">{event.description}</p>
                    </section>

                    <section className="view-event__section">
                        <h2 className="view-event__section-title">Date & Time</h2>
                        <p>
                            <strong>Starts:</strong> {formatDateTime(event.startDateTime)}
                        </p>
                        <p>
                            <strong>Ends:</strong> {formatDateTime(event.endDateTime)}
                        </p>
                    </section>

                    <section className="view-event__section">
                        <h2 className="view-event__section-title">Location</h2>
                        {getAddressLines().map((line, index) => (
                            <p key={index}>{line}</p>
                        ))}
                        {event.latitude !== null && event.longitude !== null && (
                            <p className="view-event__coordinates">
                                Coordinates: {event.latitude}, {event.longitude}
                            </p>
                        )}
                    </section>

                    <section className="view-event__section">
                        <h2 className="view-event__section-title">Capacity</h2>
                        <p>Current / maximum attendees: {event.numberOfRegisteredAttendees} / {event.maxAttendees}</p>
                    </section>
                </div>
            </div>
        </main>
    );
}