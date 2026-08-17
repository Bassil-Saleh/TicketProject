import { useState, useEffect, type SubmitEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/**
 * CreateEventPage is for implementing the page that lets
 * a logged in event host create a new event.
 * @returns JSX for the site's create event page
 */
export function CreateEventPage() {
    const { isLoggedIn, authFetch } = useAuth();
    const navigate = useNavigate();

    // Form field state
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [startDateTime, setStartDateTime] = useState('');
    const [endDateTime, setEndDateTime] = useState('');
    const [eventType, setEventType] = useState('PUBLIC');
    const [maxAttendees, setMaxAttendees] = useState('');
    const [addressLine1, setAddressLine1] = useState('');
    const [addressLine2, setAddressLine2] = useState('');
    const [city, setCity] = useState('');
    const [state, setState] = useState('');
    const [postalCode, setPostalCode] = useState('');
    const [country, setCountry] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [createdPublicId, setCreatedPublicId] = useState<string | null>(null);

    // If the user is not logged in, redirect to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Handles the create event form submission. Calls the
     * POST /api/v1/events endpoint. On success, displays the
     * newly created event's shareable URL. On failure, shows
     * an error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');

        // Client-side validation: end must be after start.
        if (startDateTime && endDateTime && new Date(endDateTime) <= new Date(startDateTime)) {
            setErrorMessage('The end date/time must be after the start date/time.');
            return;
        }

        setIsSubmitting(true);

        try {
            const body: Record<string, string | number> = {
                name,
                description,
                startDateTime,
                endDateTime,
                eventType,
                maxAttendees: Number(maxAttendees),
                addressLine1,
                city,
                state,
                postalCode,
                country,
            };

            if (addressLine2.trim() !== '') {
                body.addressLine2 = addressLine2;
            }
            if (latitude.trim() !== '') {
                body.latitude = Number(latitude);
            }
            if (longitude.trim() !== '') {
                body.longitude = Number(longitude);
            }

            const response = await authFetch('/api/v1/events', {
                method: 'POST',
                body: JSON.stringify(body),
            });
            const data = await response.json();

            if (response.ok && data.publicId) {
                setCreatedPublicId(data.publicId);
            } else {
                setErrorMessage(data.message || 'Failed to create the event. Please try again.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    // If the event was created successfully, show the success view.
    if (createdPublicId) {
        const eventUrl = `${window.location.origin}/events/${createdPublicId}`;
        return (
            <main className="page-container page-container--narrow">
                <div className="card card--centered">
                    <div className="status-page__icon" aria-hidden="true">🎉</div>
                    <h1 className="card__title">Event Created!</h1>
                    <p className="card__subtitle">
                        Your event has been created successfully. Share the link
                        below so people can view and register for your event.
                    </p>
                    <div className="alert alert--success" role="alert">
                        <a href={eventUrl} target="_blank" rel="noreferrer">
                            {eventUrl}
                        </a>
                    </div>
                    <div className="form-actions">
                        <Link to="/dashboard" className="btn btn--primary btn--block">
                            Back To Dashboard
                        </Link>
                    </div>
                </div>
            </main>
        );
    }

    return (
        <main className="page-container page-container--narrow">
            <div className="card">
                <h1 className="card__title">Create Event</h1>
                <p className="card__subtitle">
                    Fill in the details below to create a new event.
                </p>

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="form-group">
                        <label className="form-label" htmlFor="event-name">Event Name</label>
                        <input
                            id="event-name"
                            className="form-input"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Annual Tech Conference 2026"
                            maxLength={255}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="event-description">Description</label>
                        <textarea
                            id="event-description"
                            className="form-input"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="A detailed description of your event..."
                            maxLength={5000}
                            rows={4}
                            required
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-start">Start Date & Time</label>
                            <input
                                id="event-start"
                                className="form-input"
                                type="datetime-local"
                                value={startDateTime}
                                onChange={(e) => setStartDateTime(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-end">End Date & Time</label>
                            <input
                                id="event-end"
                                className="form-input"
                                type="datetime-local"
                                value={endDateTime}
                                onChange={(e) => setEndDateTime(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-type">Event Type</label>
                            <select
                                id="event-type"
                                className="form-input"
                                value={eventType}
                                onChange={(e) => setEventType(e.target.value)}
                                required
                            >
                                <option value="PUBLIC">Public</option>
                                <option value="PRIVATE">Private</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-max-attendees">Max Attendees</label>
                            <input
                                id="event-max-attendees"
                                className="form-input"
                                type="number"
                                min={1}
                                value={maxAttendees}
                                onChange={(e) => setMaxAttendees(e.target.value)}
                                placeholder="100"
                                required
                            />
                        </div>
                    </div>

                    <h2 className="form-section-title">Address</h2>

                    <div className="form-group">
                        <label className="form-label" htmlFor="event-address1">Address Line 1</label>
                        <input
                            id="event-address1"
                            className="form-input"
                            type="text"
                            value={addressLine1}
                            onChange={(e) => setAddressLine1(e.target.value)}
                            placeholder="123 Main Street"
                            maxLength={255}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="event-address2">Address Line 2 (optional)</label>
                        <input
                            id="event-address2"
                            className="form-input"
                            type="text"
                            value={addressLine2}
                            onChange={(e) => setAddressLine2(e.target.value)}
                            placeholder="Suite 400"
                            maxLength={255}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-city">City</label>
                            <input
                                id="event-city"
                                className="form-input"
                                type="text"
                                value={city}
                                onChange={(e) => setCity(e.target.value)}
                                placeholder="San Francisco"
                                maxLength={100}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-state">State / Province</label>
                            <input
                                id="event-state"
                                className="form-input"
                                type="text"
                                value={state}
                                onChange={(e) => setState(e.target.value)}
                                placeholder="California"
                                maxLength={100}
                                required
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-postal">Postal Code</label>
                            <input
                                id="event-postal"
                                className="form-input"
                                type="text"
                                value={postalCode}
                                onChange={(e) => setPostalCode(e.target.value)}
                                placeholder="94105"
                                maxLength={20}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-country">Country</label>
                            <input
                                id="event-country"
                                className="form-input"
                                type="text"
                                value={country}
                                onChange={(e) => setCountry(e.target.value)}
                                placeholder="United States"
                                maxLength={100}
                                required
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-latitude">Latitude (optional)</label>
                            <input
                                id="event-latitude"
                                className="form-input"
                                type="number"
                                step="any"
                                value={latitude}
                                onChange={(e) => setLatitude(e.target.value)}
                                placeholder="37.7749295"
                            />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="event-longitude">Longitude (optional)</label>
                            <input
                                id="event-longitude"
                                className="form-input"
                                type="number"
                                step="any"
                                value={longitude}
                                onChange={(e) => setLongitude(e.target.value)}
                                placeholder="-122.4194155"
                            />
                        </div>
                    </div>

                    <div className="form-actions">
                        <button
                            type="submit"
                            className="btn btn--primary"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Creating Event...' : 'Create Event'}
                        </button>
                        <button
                            type="button"
                            className="btn btn--ghost"
                            onClick={() => navigate('/dashboard')}
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}