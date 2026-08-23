import { useState, useEffect, type SubmitEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

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
 * CreateInvitationPage is for implementing the page that lets
 * a logged in event host invite someone to a private event
 * they've created.
 * @returns JSX for the site's invitation creation page.
 */
export function CreateInvitationPage() {
    const { isLoggedIn, authFetch } = useAuth();
    const navigate = useNavigate();
    const { publicId } = useParams<{ publicId: string }>();

    const [event, setEvent] = useState<EventInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState('');

    // Invitation form state
    const [firstName, setFirstName] = useState('');
    const [middleName, setMiddleName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    // If the user is not currently logged in, redirect them to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Fetches the event's details using its public ID so the page
     * can display the event's name and confirm that it is a private
     * event (invitations can only be created for private events).
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
     * Handles the invitation form submission. Calls
     * POST /api/v1/attendees/invitation with the event's public ID
     * and the invitee's details.
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

            const response = await authFetch('/api/v1/attendees/invitation', {
                method: 'POST',
                body: JSON.stringify(body),
            });
            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(
                    data.message ||
                    'Invitation created. A ticket with a QR code has been sent to the recipient\'s email.'
                );
                // Clear the form after a successful invitation.
                setFirstName('');
                setMiddleName('');
                setLastName('');
                setEmail('');
            } else {
                setErrorMessage(data.message || 'Failed to create the invitation. Please try again.');
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
                <Link to="/dashboard" className="btn btn--outline btn--lg">
                    Back to Dashboard
                </Link>
            </main>
        );
    }

    // Invitations can only be created for private events.
    if (event.eventType !== 'PRIVATE') {
        return (
            <main className="page-container page-container--narrow">
                <div className="card card--centered">
                    <h1 className="card__title">{event.name}</h1>
                    <div className="alert alert--info" role="alert">
                        This is a public event. Invitations can only be created
                        for private events. Attendees can register for this event
                        directly using its registration page.
                    </div>
                    <div className="form-actions">
                        <Link to={`/events/${event.publicId}`} className="btn btn--outline">
                            Back to Event
                        </Link>
                        <Link to="/dashboard" className="btn btn--ghost">
                            Back to Dashboard
                        </Link>
                    </div>
                </div>
            </main>
        );
    }

    return (
        <main className="page-container page-container--narrow">
            <div className="card">
                <h1 className="card__title">Invite Someone to {event.name}</h1>
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
                        <label className="form-label" htmlFor="inv-first-name">First Name</label>
                        <input
                            id="inv-first-name"
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
                        <label className="form-label" htmlFor="inv-middle-name">Middle Name (optional)</label>
                        <input
                            id="inv-middle-name"
                            className="form-input"
                            type="text"
                            value={middleName}
                            onChange={(e) => setMiddleName(e.target.value)}
                            placeholder="Marie"
                            maxLength={100}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="inv-last-name">Last Name</label>
                        <input
                            id="inv-last-name"
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
                        <label className="form-label" htmlFor="inv-email">Email Address</label>
                        <input
                            id="inv-email"
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
                            The invitation and ticket will be sent to this email address.
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Sending Invitation...' : 'Send Invitation'}
                    </button>
                </form>

                <div className="form-actions">
                    <Link to="/dashboard" className="btn btn--ghost">
                        Back to Dashboard
                    </Link>
                </div>
            </div>
        </main>
    );
}