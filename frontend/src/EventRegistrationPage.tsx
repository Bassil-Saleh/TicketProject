import { useState, useEffect, type SubmitEvent } from 'react';
import { Link, useParams } from 'react-router-dom';

/** Shape of a single event returned by GET /api/v1/events/{publicId}. */
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
 * EventRegistrationPage is for implementing the page
 * that lets a user register for a public event identified
 * by the event's public ID. Note that a user does not need
 * to be logged in to access or use this page.
 * @returns JSX for the site's public event registration page
 */
export function EventRegistrationPage() {
    const { publicId } = useParams<{ publicId: string }>();

    const [event, setEvent] = useState<EventInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState('');

    // Registration form state
    const [firstName, setFirstName] = useState('');
    const [middleName, setMiddleName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    /**
     * Fetches the event's details using its public ID so the
     * page can display the event and determine whether it is
     * public (open registration) or private (invitation only).
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
     * Handles the registration form submission. Calls
     * POST /api/v1/attendees/registration with the event's
     * public ID and the attendee's details.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');
        setIsSubmitting(true);

        try {
            const body: Record<string, string> = {
                publicId: publicId ?? '',
                firstName,
                lastName,
                email,
            };
            if (middleName.trim() !== '') {
                body.middleName = middleName;
            }

            const response = await fetch('/api/v1/attendees/registration', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
            });
            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(
                    data.message ||
                    'Registration successful! Your ticket has been sent to your email address.'
                );
                // Clear the form after a successful registration.
                setFirstName('');
                setMiddleName('');
                setLastName('');
                setEmail('');
            } else {
                setErrorMessage(data.message || 'Registration failed. Please try again.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

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
                <div className="status-page__icon" aria-hidden="true">❌</div>
                <h2 className="status-page__title">Error Loading Event</h2>
                <p className="status-page__message">{loadError || 'Event not found.'}</p>
                <Link to="/" className="btn btn--outline btn--lg">
                    Back to Home
                </Link>
            </main>
        );
    }

    // If the event is private, do not show a registration form.
    if (event.eventType === 'PRIVATE') {
        return (
            <main className="page-container page-container--narrow">
                <div className="card card--centered">
                    <h1 className="card__title">{event.name}</h1>
                    <div className="alert alert--info" role="alert">
                        This is a private event. If you would like to attend,
                        the event host who created this event will need to send
                        you an invitation directly.
                    </div>
                    <div className="form-actions">
                        <Link to={`/events/${event.publicId}`} className="btn btn--outline">
                            Back to Event
                        </Link>
                    </div>
                </div>
            </main>
        );
    }

    return (
        <main className="page-container page-container--narrow">
            <div className="card">
                <h1 className="card__title">Register for {event.name}</h1>
                <p className="card__subtitle">
                    {formatDateTime(event.startDateTime)} &mdash; {formatDateTime(event.endDateTime)}
                </p>

                {successMessage && (
                    <div className="alert alert--success" role="alert">
                        {successMessage}
                    </div>
                )}

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="form-group">
                        <label className="form-label" htmlFor="reg-first-name">First Name</label>
                        <input
                            id="reg-first-name"
                            className="form-input"
                            type="text"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            placeholder="Jane"
                            maxLength={100}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="reg-middle-name">Middle Name (optional)</label>
                        <input
                            id="reg-middle-name"
                            className="form-input"
                            type="text"
                            value={middleName}
                            onChange={(e) => setMiddleName(e.target.value)}
                            placeholder="Marie"
                            maxLength={100}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="reg-last-name">Last Name</label>
                        <input
                            id="reg-last-name"
                            className="form-input"
                            type="text"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            placeholder="Smith"
                            maxLength={100}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="reg-email">Email Address</label>
                        <input
                            id="reg-email"
                            className="form-input"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="jane.smith@example.com"
                            maxLength={254}
                            required
                            autoComplete="email"
                        />
                        <div className="form-hint">
                            Your ticket will be sent to this email address.
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Registering...' : 'Register'}
                    </button>
                </form>

                <div className="auth-card__footer">
                    <Link to={`/events/${event.publicId}`}>Back to Event</Link>
                </div>
            </div>
        </main>
    );
}