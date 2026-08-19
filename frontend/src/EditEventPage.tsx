import { useState, useEffect, type SubmitEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/** Shape of a single event returned by GET /api/v1/events/{publicId}. */
interface EventInfo {
    publicId: string;
    name: string;
    description: string;
    startDateTime: string;
    endDateTime: string;
    eventType: string;
    eventStatus: string;
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

/** Which event field is currently being edited, if any. */
type EditingField = 'name' | 'description' | 'dates' | 'address' | null;

/**
 * EditEventPage is for implementing the page that lets a
 * logged in event host edit or delete an event which
 * they've already created.
 * @returns JSX for the site's edit event page
 */
export function EditEventPage() {
    const { isLoggedIn, authFetch } = useAuth();
    const navigate = useNavigate();
    const { publicId } = useParams<{ publicId: string }>();

    const [event, setEvent] = useState<EventInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState('');

    const [editingField, setEditingField] = useState<EditingField>(null);
    const [saveMessage, setSaveMessage] = useState('');
    const [saveError, setSaveError] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    // Edit form state
    const [editName, setEditName] = useState('');
    const [editDescription, setEditDescription] = useState('');
    const [editStartDateTime, setEditStartDateTime] = useState('');
    const [editEndDateTime, setEditEndDateTime] = useState('');
    const [editAddressLine1, setEditAddressLine1] = useState('');
    const [editAddressLine2, setEditAddressLine2] = useState('');
    const [editCity, setEditCity] = useState('');
    const [editState, setEditState] = useState('');
    const [editPostalCode, setEditPostalCode] = useState('');
    const [editCountry, setEditCountry] = useState('');
    const [editLatitude, setEditLatitude] = useState('');
    const [editLongitude, setEditLongitude] = useState('');

    // Change-type state
    const [showChangeToPublicForm, setShowChangeToPublicForm] = useState(false);
    const [newMaxAttendees, setNewMaxAttendees] = useState('');
    const [typeMessage, setTypeMessage] = useState('');
    const [typeError, setTypeError] = useState('');
    const [isChangingType, setIsChangingType] = useState(false);

    // Delete state
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deleteMessage, setDeleteMessage] = useState('');
    const [deleteError, setDeleteError] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);

    // If the user is not logged in, redirect to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Converts an ISO date-time string (e.g. "2026-09-15T09:00:00")
     * into the format expected by an `<input type="datetime-local">`
     * (e.g. "2026-09-15T09:00").
     */
    const toDateTimeLocalValue = (isoString: string): string => {
        if (!isoString) return '';
        return isoString.length >= 16 ? isoString.slice(0, 16) : isoString;
    };

    /**
     * Fetches the event's current details so the edit forms
     * can be pre-populated.
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
        if (isLoggedIn && publicId) {
            fetchEvent();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isLoggedIn, publicId]);

    /**
     * Opens the inline edit form for the given field,
     * pre-populated with the current event values.
     */
    const startEditing = (field: EditingField) => {
        setSaveMessage('');
        setSaveError('');
        setEditingField(field);

        if (!event) return;

        if (field === 'name') {
            setEditName(event.name);
        } else if (field === 'description') {
            setEditDescription(event.description);
        } else if (field === 'dates') {
            setEditStartDateTime(toDateTimeLocalValue(event.startDateTime));
            setEditEndDateTime(toDateTimeLocalValue(event.endDateTime));
        } else if (field === 'address') {
            setEditAddressLine1(event.addressLine1);
            setEditAddressLine2(event.addressLine2 ?? '');
            setEditCity(event.city);
            setEditState(event.state);
            setEditPostalCode(event.postalCode);
            setEditCountry(event.country);
            setEditLatitude(event.latitude !== null ? String(event.latitude) : '');
            setEditLongitude(event.longitude !== null ? String(event.longitude) : '');
        }
    };

    /**
     * Cancels the current edit and returns to the display view.
     */
    const cancelEditing = () => {
        setEditingField(null);
        setSaveMessage('');
        setSaveError('');
    };

    /**
     * Handles saving the edited field to the appropriate
     * PATCH endpoint.
     */
    const handleSave = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setSaveMessage('');
        setSaveError('');

        if (!publicId) return;

        // Client-side validation for dates.
        if (editingField === 'dates' && editStartDateTime && editEndDateTime
            && new Date(editEndDateTime) <= new Date(editStartDateTime)) {
            setSaveError('The end date/time must be after the start date/time.');
            return;
        }

        setIsSaving(true);

        try {
            let url = '';
            let body: Record<string, string | number> = { publicId };

            if (editingField === 'name') {
                url = '/api/v1/events/name';
                body.name = editName;
            } else if (editingField === 'description') {
                url = '/api/v1/events/description';
                body.description = editDescription;
            } else if (editingField === 'dates') {
                url = '/api/v1/events/dates';
                body.startDateTime = editStartDateTime;
                body.endDateTime = editEndDateTime;
            } else if (editingField === 'address') {
                url = '/api/v1/events/address';
                body.addressLine1 = editAddressLine1;
                body.city = editCity;
                body.state = editState;
                body.postalCode = editPostalCode;
                body.country = editCountry;
                if (editAddressLine2.trim() !== '') {
                    body.addressLine2 = editAddressLine2;
                }
                if (editLatitude.trim() !== '') {
                    body.latitude = Number(editLatitude);
                }
                if (editLongitude.trim() !== '') {
                    body.longitude = Number(editLongitude);
                }
            }

            const response = await authFetch(url, {
                method: 'PATCH',
                body: JSON.stringify(body),
            });
            const data = await response.json();

            if (response.ok) {
                setSaveMessage(data.message || 'Saved successfully.');
                setEditingField(null);
                // Refresh the event to show updated values.
                await fetchEvent();
            } else {
                setSaveError(data.message || 'Failed to save changes.');
            }
        } catch {
            setSaveError('An unexpected error occurred. Please try again.');
        } finally {
            setIsSaving(false);
        }
    };

    /**
     * Handles sending a request to change the event to a private event.
     * Sends PATCH /api/v1/events/change-to-private with the event's publicId.
     */
    const handleChangeToPrivate = async () => {
        if (!publicId) return;
        setTypeMessage('');
        setTypeError('');
        setIsChangingType(true);

        try {
            const response = await authFetch('/api/v1/events/change-to-private', {
                method: 'PATCH',
                body: JSON.stringify({ publicId }),
            });
            const data = await response.json();

            if (response.ok) {
                setTypeMessage(data.message || 'Event changed to private.');
                setShowChangeToPublicForm(false);
                setNewMaxAttendees('');
                await fetchEvent();
            } else {
                setTypeError(data.message || 'Failed to change event type.');
            }
        } catch {
            setTypeError('An unexpected error occurred. Please try again.');
        } finally {
            setIsChangingType(false);
        }
    };

    /**
     * Handles sending a request to change the event to a public event.
     * Sends PATCH /api/v1/events/change-to-public with the event's publicId
     * and the new maxAttendees value.
     */
    const handleChangeToPublic = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!publicId) return;
        setTypeMessage('');
        setTypeError('');

        const parsedMaxAttendees = Number(newMaxAttendees);
        if (!Number.isInteger(parsedMaxAttendees) || parsedMaxAttendees < 1) {
            setTypeError('Max attendees must be a whole number of at least 1.');
            return;
        }

        setIsChangingType(true);

        try {
            const response = await authFetch('/api/v1/events/change-to-public', {
                method: 'PATCH',
                body: JSON.stringify({ publicId, maxAttendees: parsedMaxAttendees }),
            });
            const data = await response.json();

            if (response.ok) {
                setTypeMessage(data.message || 'Event changed to public.');
                setShowChangeToPublicForm(false);
                setNewMaxAttendees('');
                await fetchEvent();
            } else {
                setTypeError(data.message || 'Failed to change event type.');
            }
        } catch {
            setTypeError('An unexpected error occurred. Please try again.');
        } finally {
            setIsChangingType(false);
        }
    };

    /**
     * Handles deleting the event. Shows a confirmation dialog
     * first; on confirmation, calls DELETE /api/v1/events/{publicId}.
     */
    const handleDelete = async () => {
        if (!publicId) return;
        setDeleteMessage('');
        setDeleteError('');
        setIsDeleting(true);

        try {
            const response = await authFetch(`/api/v1/events/${encodeURIComponent(publicId)}`, {
                method: 'DELETE',
            });
            const data = await response.json();

            if (response.ok) {
                setDeleteMessage(data.message || 'Event deleted successfully.');
                setShowDeleteConfirm(false);
                // Redirect back to the dashboard after a short delay.
                setTimeout(() => navigate('/dashboard'), 1500);
            } else {
                setDeleteError(data.message || 'Failed to delete the event.');
                setShowDeleteConfirm(false);
            }
        } catch {
            setDeleteError('An unexpected error occurred. Please try again.');
            setShowDeleteConfirm(false);
        } finally {
            setIsDeleting(false);
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

    /**
     * Builds a single-line address string from the event data.
     */
    const getFullAddress = (): string => {
        if (!event) return '';
        const parts = [
            event.addressLine1,
            event.addressLine2,
            `${event.city}, ${event.state} ${event.postalCode}`,
            event.country,
        ];
        return parts.filter(Boolean).join(', ');
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
                <button className="btn btn--primary" onClick={fetchEvent}>
                    Try Again
                </button>
            </main>
        );
    }

    return (
        <main className="page-container">
            <div className="edit-event">
                <div className="edit-event__header">
                    <h1 className="edit-event__title">Edit Event</h1>
                    <button
                        className="btn btn--danger"
                        onClick={() => setShowDeleteConfirm(true)}
                    >
                        Delete Event
                    </button>
                </div>

                {saveMessage && (
                    <div className="alert alert--success" role="alert">
                        {saveMessage}
                    </div>
                )}

                {saveError && (
                    <div className="alert alert--error" role="alert">
                        {saveError}
                    </div>
                )}

                {deleteMessage && (
                    <div className="alert alert--success" role="alert">
                        {deleteMessage}
                    </div>
                )}

                {deleteError && (
                    <div className="alert alert--error" role="alert">
                        {deleteError}
                    </div>
                )}

                {typeMessage && (
                    <div className="alert alert--success" role="alert">
                        {typeMessage}
                    </div>
                )}

                {typeError && (
                    <div className="alert alert--error" role="alert">
                        {typeError}
                    </div>
                )}

                {/* Name field */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Event Name</div>
                        {editingField === 'name' ? (
                            <form className="profile__edit-form" onSubmit={handleSave}>
                                <input
                                    className="form-input"
                                    type="text"
                                    value={editName}
                                    onChange={(e) => setEditName(e.target.value)}
                                    maxLength={255}
                                    required
                                    aria-label="Event name"
                                />
                                <button type="submit" className="btn btn--primary btn--sm" disabled={isSaving}>
                                    {isSaving ? 'Saving...' : 'Save'}
                                </button>
                                <button type="button" className="btn btn--ghost btn--sm" onClick={cancelEditing}>
                                    Cancel
                                </button>
                            </form>
                        ) : (
                            <div className="profile__field-value">{event.name}</div>
                        )}
                    </div>
                    {editingField !== 'name' && (
                        <button className="btn btn--outline btn--sm" onClick={() => startEditing('name')}>
                            Edit
                        </button>
                    )}
                </div>

                {/* Description field */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Description</div>
                        {editingField === 'description' ? (
                            <form className="profile__edit-form profile__edit-form--column" onSubmit={handleSave}>
                                <textarea
                                    className="form-input"
                                    value={editDescription}
                                    onChange={(e) => setEditDescription(e.target.value)}
                                    maxLength={5000}
                                    rows={4}
                                    required
                                    aria-label="Event description"
                                />
                                <div className="form-actions">
                                    <button type="submit" className="btn btn--primary btn--sm" disabled={isSaving}>
                                        {isSaving ? 'Saving...' : 'Save'}
                                    </button>
                                    <button type="button" className="btn btn--ghost btn--sm" onClick={cancelEditing}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className="profile__field-value">{event.description}</div>
                        )}
                    </div>
                    {editingField !== 'description' && (
                        <button className="btn btn--outline btn--sm" onClick={() => startEditing('description')}>
                            Edit
                        </button>
                    )}
                </div>

                {/* Dates field */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Start & End Dates</div>
                        {editingField === 'dates' ? (
                            <form className="profile__edit-form profile__edit-form--column" onSubmit={handleSave}>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-start">Start</label>
                                        <input
                                            id="edit-start"
                                            className="form-input"
                                            type="datetime-local"
                                            value={editStartDateTime}
                                            onChange={(e) => setEditStartDateTime(e.target.value)}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-end">End</label>
                                        <input
                                            id="edit-end"
                                            className="form-input"
                                            type="datetime-local"
                                            value={editEndDateTime}
                                            onChange={(e) => setEditEndDateTime(e.target.value)}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="submit" className="btn btn--primary btn--sm" disabled={isSaving}>
                                        {isSaving ? 'Saving...' : 'Save'}
                                    </button>
                                    <button type="button" className="btn btn--ghost btn--sm" onClick={cancelEditing}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className="profile__field-value">
                                {formatDateTime(event.startDateTime)} &mdash; {formatDateTime(event.endDateTime)}
                            </div>
                        )}
                    </div>
                    {editingField !== 'dates' && (
                        <button className="btn btn--outline btn--sm" onClick={() => startEditing('dates')}>
                            Edit
                        </button>
                    )}
                </div>

                {/* Address field */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Address</div>
                        {editingField === 'address' ? (
                            <form className="profile__edit-form profile__edit-form--column" onSubmit={handleSave}>
                                <div className="form-group">
                                    <label className="form-label" htmlFor="edit-address1">Address Line 1</label>
                                    <input
                                        id="edit-address1"
                                        className="form-input"
                                        type="text"
                                        value={editAddressLine1}
                                        onChange={(e) => setEditAddressLine1(e.target.value)}
                                        maxLength={255}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label" htmlFor="edit-address2">Address Line 2 (optional)</label>
                                    <input
                                        id="edit-address2"
                                        className="form-input"
                                        type="text"
                                        value={editAddressLine2}
                                        onChange={(e) => setEditAddressLine2(e.target.value)}
                                        maxLength={255}
                                    />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-city">City</label>
                                        <input
                                            id="edit-city"
                                            className="form-input"
                                            type="text"
                                            value={editCity}
                                            onChange={(e) => setEditCity(e.target.value)}
                                            maxLength={100}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-state">State / Province</label>
                                        <input
                                            id="edit-state"
                                            className="form-input"
                                            type="text"
                                            value={editState}
                                            onChange={(e) => setEditState(e.target.value)}
                                            maxLength={100}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-postal">Postal Code</label>
                                        <input
                                            id="edit-postal"
                                            className="form-input"
                                            type="text"
                                            value={editPostalCode}
                                            onChange={(e) => setEditPostalCode(e.target.value)}
                                            maxLength={20}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-country">Country</label>
                                        <input
                                            id="edit-country"
                                            className="form-input"
                                            type="text"
                                            value={editCountry}
                                            onChange={(e) => setEditCountry(e.target.value)}
                                            maxLength={100}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-latitude">Latitude (optional)</label>
                                        <input
                                            id="edit-latitude"
                                            className="form-input"
                                            type="number"
                                            step="any"
                                            value={editLatitude}
                                            onChange={(e) => setEditLatitude(e.target.value)}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label" htmlFor="edit-longitude">Longitude (optional)</label>
                                        <input
                                            id="edit-longitude"
                                            className="form-input"
                                            type="number"
                                            step="any"
                                            value={editLongitude}
                                            onChange={(e) => setEditLongitude(e.target.value)}
                                        />
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button type="submit" className="btn btn--primary btn--sm" disabled={isSaving}>
                                        {isSaving ? 'Saving...' : 'Save'}
                                    </button>
                                    <button type="button" className="btn btn--ghost btn--sm" onClick={cancelEditing}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className="profile__field-value">{getFullAddress()}</div>
                        )}
                    </div>
                    {editingField !== 'address' && (
                        <button className="btn btn--outline btn--sm" onClick={() => startEditing('address')}>
                            Edit
                        </button>
                    )}
                </div>

                {/* Event type */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Event Type</div>
                        <div className="profile__field-value">
                            {event.eventType}
                            {(event.eventStatus === 'PUBLISHED' || event.eventStatus === 'CANCELED') && (
                                <span className="profile__field-hint">
                                    {" — The event's type cannot be changed once an event is published or canceled."}
                                </span>
                            )}
                        </div>
                        {event.eventStatus === 'DRAFT' && event.eventType === 'PUBLIC' && (
                            <button
                                className="btn btn--outline btn--sm"
                                onClick={handleChangeToPrivate}
                                disabled={isChangingType}
                            >
                                {isChangingType ? 'Changing...' : 'Change to Private Event'}
                            </button>
                        )}
                        {event.eventStatus === 'DRAFT' && event.eventType === 'PRIVATE' && !showChangeToPublicForm && (
                            <button
                                className="btn btn--outline btn--sm"
                                onClick={() => {
                                    setTypeMessage('');
                                    setTypeError('');
                                    setNewMaxAttendees('');
                                    setShowChangeToPublicForm(true);
                                }}
                                disabled={isChangingType}
                            >
                                Change to Public Event
                            </button>
                        )}
                        {event.eventStatus === 'DRAFT' && event.eventType === 'PRIVATE' && showChangeToPublicForm && (
                            <form className="profile__edit-form profile__edit-form--column" onSubmit={handleChangeToPublic}>
                                <div className="form-group">
                                    <label className="form-label" htmlFor="edit-max-attendees">
                                        Max Attendees
                                    </label>
                                    <input
                                        id="edit-max-attendees"
                                        className="form-input"
                                        type="number"
                                        min={1}
                                        step={1}
                                        value={newMaxAttendees}
                                        onChange={(e) => setNewMaxAttendees(e.target.value)}
                                        required
                                        aria-label="Maximum number of attendees"
                                    />
                                </div>
                                <div className="form-actions">
                                    <button type="submit" className="btn btn--primary btn--sm" disabled={isChangingType}>
                                        {isChangingType ? 'Submitting...' : 'Submit'}
                                    </button>
                                    <button
                                        type="button"
                                        className="btn btn--ghost btn--sm"
                                        onClick={() => {
                                            setShowChangeToPublicForm(false);
                                            setNewMaxAttendees('');
                                            setTypeMessage('');
                                            setTypeError('');
                                        }}
                                        disabled={isChangingType}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>
                </div>

                {/* Max attendees (read-only) */}
                <div className="profile__field">
                    <div className="profile__field-info">
                        <div className="profile__field-label">Max Attendees</div>
                        <div className="profile__field-value">{event.maxAttendees}</div>
                    </div>
                </div>

                <div className="form-actions">
                    <button className="btn btn--ghost" onClick={() => navigate('/dashboard')}>
                        Back To Dashboard
                    </button>
                </div>
            </div>

            {/* Delete confirmation dialog */}
            {showDeleteConfirm && (
                <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="delete-confirm-title">
                    <div className="modal">
                        <h2 id="delete-confirm-title" className="modal__title">Delete Event?</h2>
                        <p className="modal__message">
                            Are you sure you want to delete "{event.name}"? This action
                            cannot be undone, and all registrations and tickets for this
                            event will be permanently removed.
                        </p>
                        <div className="modal__actions">
                            <button
                                className="btn btn--danger"
                                onClick={handleDelete}
                                disabled={isDeleting}
                            >
                                {isDeleting ? 'Deleting...' : 'Yes, Delete Event'}
                            </button>
                            <button
                                className="btn btn--ghost"
                                onClick={() => setShowDeleteConfirm(false)}
                                disabled={isDeleting}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </main>
    );
}