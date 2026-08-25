import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/** Shape of a single event returned by GET /api/v1/events. */
interface EventInfo {
    publicId: string;
    name: string;
    description: string;
    startDateTime: string;
    endDateTime: string;
    eventType: string;
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
 * Dashboard is for implementing a dashboard page to let a
 * logged in event host navigate to and view different pages.
 * The dashboard should remain visible even when different pages
 * referenced by the dashboard become visible (i.e. view profile,
 * list of events, etc.).
 * @returns JSX for the site's dashboard
 */
export function Dashboard() {
    const { isLoggedIn, authFetch } = useAuth();
    const navigate = useNavigate();

    const [events, setEvents] = useState<EventInfo[]>([]);
    const [isLoadingEvents, setIsLoadingEvents] = useState(true);
    const [eventsError, setEventsError] = useState('');

    // If the user is not logged in, redirect to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    // Fetch the event host's events on mount.
    useEffect(() => {
        if (!isLoggedIn) return;

        const fetchEvents = async () => {
            setIsLoadingEvents(true);
            setEventsError('');
            try {
                const response = await authFetch('/api/v1/events?count=500', {
                    method: 'GET',
                });
                const data = await response.json();

                if (response.ok && data.events) {
                    setEvents(data.events);
                } else {
                    setEventsError(data.message || 'Failed to load events.');
                }
            } catch {
                setEventsError('An unexpected error occurred while loading events.');
            } finally {
                setIsLoadingEvents(false);
            }
        };

        fetchEvents();
    }, [isLoggedIn, authFetch]);

    /**
     * Formats an ISO date-time string into a human-readable format.
     */
    const formatDateTime = (isoString: string): string => {
        try {
            return new Date(isoString).toLocaleString(undefined, {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
            });
        } catch {
            return isoString;
        }
    };

    return (
        <main className="page-container">
            <div className="dashboard">
                <div className="dashboard__header">
                    <h1 className="dashboard__title">Dashboard</h1>
                </div>

                {/* Navigation links */}
                <nav className="dashboard__nav">
                    <Link to="/scan-tickets" className="dashboard__nav-link">
                        📷 Start Scanning Tickets
                    </Link>
                    <Link to="/profile" className="dashboard__nav-link">
                        👤 View Profile
                    </Link>
                    <Link to="/create-event" className="dashboard__nav-link">
                        ➕ Create Event
                    </Link>
                    <Link to="/scanned-tickets" className="dashboard__nav-link">
                        🎫 Scanned Tickets
                    </Link>
                </nav>

                {/* Events section */}
                <section className="dashboard__section">
                    <h2 className="dashboard__section-title">Your Events</h2>

                    {isLoadingEvents && (
                        <div className="dashboard__empty">
                            <div className="spinner" aria-label="Loading events" />
                            <p>Loading your events...</p>
                        </div>
                    )}

                    {eventsError && !isLoadingEvents && (
                        <div className="alert alert--error" role="alert">
                            {eventsError}
                        </div>
                    )}

                    {!isLoadingEvents && !eventsError && events.length === 0 && (
                        <div className="dashboard__empty">
                            <p>You haven't created any events yet.</p>
                        </div>
                    )}

                    {!isLoadingEvents && !eventsError && events.length > 0 && (
                        <div>
                            {events.map((event) => (
                                <div key={event.publicId} className="profile__field">
                                    <div className="profile__field-info">
                                        <div className="profile__field-label">
                                            {event.eventType} &middot; {formatDateTime(event.startDateTime)}
                                        </div>
                                        <div className="profile__field-value">
                                            {event.name}
                                        </div>
                                    </div>
                                    <div className="dashboard__event-actions">
                                        <Link
                                            to={`/events/${event.publicId}`}
                                            className="btn btn--outline btn--sm"
                                        >
                                            View Event
                                        </Link>
                                        <Link
                                            to={`/events/${event.publicId}/edit`}
                                            className="btn btn--ghost btn--sm"
                                        >
                                            Edit Event
                                        </Link>
                                        {event.eventType === 'PRIVATE' && (
                                            <Link
                                                to={`/events/${event.publicId}/invite`}
                                                className="btn btn--accent btn--sm"
                                            >
                                                Create Invitation
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            </div>
        </main>
    );
}