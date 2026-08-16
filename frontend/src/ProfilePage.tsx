import { useState, useEffect, type SubmitEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/** Shape of the profile response from GET /api/v1/event-hosts/profile. */
interface ProfileData {
    firstName: string;
    middleName: string | null;
    lastName: string;
    email: string;
    lastLogin: string;
}

/** Which profile field is currently being edited, if any. */
type EditingField = 'name' | 'email' | 'password' | null;

/**
 * ProfilePage is for implementing the page where a logged in event host
 * can view and edit their profile info.
 * @returns JSX for the site's profile page
 */
export function ProfilePage() {
    const { isLoggedIn, authFetch } = useAuth();
    const navigate = useNavigate();

    const [profile, setProfile] = useState<ProfileData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState('');

    const [editingField, setEditingField] = useState<EditingField>(null);
    const [saveMessage, setSaveMessage] = useState('');
    const [saveError, setSaveError] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    // Edit form state
    const [editFirstName, setEditFirstName] = useState('');
    const [editMiddleName, setEditMiddleName] = useState('');
    const [editLastName, setEditLastName] = useState('');
    const [editEmail, setEditEmail] = useState('');
    const [editPassword, setEditPassword] = useState('');
    const [editConfirmPassword, setEditConfirmPassword] = useState('');

    // If the user is not logged in, redirect to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Fetches the event host's profile from the API.
     */
    const fetchProfile = async () => {
        setIsLoading(true);
        setLoadError('');
        try {
            const response = await authFetch('/api/v1/event-hosts/profile', {
                method: 'GET',
            });
            const data = await response.json();

            if (response.ok) {
                setProfile(data);
            } else {
                setLoadError(data.message || 'Failed to load profile.');
            }
        } catch {
            setLoadError('An unexpected error occurred while loading your profile.');
        } finally {
            setIsLoading(false);
        }
    };

    // Fetch profile on mount.
    useEffect(() => {
        if (isLoggedIn) {
            fetchProfile();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isLoggedIn]);

    /**
     * Opens the inline edit form for the given field,
     * pre-populated with the current values.
     */
    const startEditing = (field: EditingField) => {
        setSaveMessage('');
        setSaveError('');
        setEditingField(field);

        if (field === 'name' && profile) {
            setEditFirstName(profile.firstName);
            setEditMiddleName(profile.middleName ?? '');
            setEditLastName(profile.lastName);
        } else if (field === 'email' && profile) {
            setEditEmail(profile.email);
        } else if (field === 'password') {
            setEditPassword('');
            setEditConfirmPassword('');
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
     * Handles saving the edited field to the API.
     */
    const handleSave = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setSaveMessage('');
        setSaveError('');

        // Client-side validation: passwords must match when editing password.
        if (editingField === 'password' && editPassword !== editConfirmPassword) {
            setSaveError('Passwords do not match. Please try again.');
            return;
        }

        setIsSaving(true);

        try {
            let url = '';
            let body: Record<string, string> = {};

            if (editingField === 'name') {
                url = '/api/v1/event-hosts/name';
                body = {
                    firstName: editFirstName,
                    lastName: editLastName,
                };
                if (editMiddleName.trim() !== '') {
                    body.middleName = editMiddleName;
                }
            } else if (editingField === 'email') {
                url = '/api/v1/event-hosts/email';
                body = { email: editEmail };
            } else if (editingField === 'password') {
                url = '/api/v1/event-hosts/password';
                body = { password: editPassword };
            }

            const response = await authFetch(url, {
                method: 'PATCH',
                body: JSON.stringify(body),
            });
            const data = await response.json();

            if (response.ok) {
                setSaveMessage(data.message || 'Saved successfully.');
                setEditingField(null);
                // Refresh the profile to show updated values.
                await fetchProfile();
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
     * Builds the full display name from profile data.
     */
    const getFullName = (): string => {
        if (!profile) return '';
        const parts = [profile.firstName, profile.middleName, profile.lastName];
        return parts.filter(Boolean).join(' ');
    };

    if (isLoading) {
        return (
            <main className="status-page">
                <div className="spinner spinner--lg" aria-label="Loading profile" />
                <h2 className="status-page__title">Loading Your Profile...</h2>
            </main>
        );
    }

    if (loadError) {
        return (
            <main className="status-page">
                <div className="status-page__icon" aria-hidden="true">❌</div>
                <h2 className="status-page__title">Error Loading Profile</h2>
                <p className="status-page__message">{loadError}</p>
                <button className="btn btn--primary" onClick={fetchProfile}>
                    Try Again
                </button>
            </main>
        );
    }

    return (
        <main className="profile">
            <h1 className="profile__title">Your Profile</h1>

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

            {/* Name field */}
            <div className="profile__field">
                <div className="profile__field-info">
                    <div className="profile__field-label">Name</div>
                    {editingField === 'name' ? (
                        <form className="profile__edit-form" onSubmit={handleSave}>
                            <input
                                className="form-input"
                                type="text"
                                value={editFirstName}
                                onChange={(e) => setEditFirstName(e.target.value)}
                                placeholder="First name"
                                required
                                aria-label="First name"
                            />
                            <input
                                className="form-input"
                                type="text"
                                value={editMiddleName}
                                onChange={(e) => setEditMiddleName(e.target.value)}
                                placeholder="Middle name (optional)"
                                aria-label="Middle name"
                            />
                            <input
                                className="form-input"
                                type="text"
                                value={editLastName}
                                onChange={(e) => setEditLastName(e.target.value)}
                                placeholder="Last name"
                                required
                                aria-label="Last name"
                            />
                            <button
                                type="submit"
                                className="btn btn--primary btn--sm"
                                disabled={isSaving}
                            >
                                {isSaving ? 'Saving...' : 'Save'}
                            </button>
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                onClick={cancelEditing}
                            >
                                Cancel
                            </button>
                        </form>
                    ) : (
                        <>
                            <div className="profile__field-value">{getFullName()}</div>
                        </>
                    )}
                </div>
                {editingField !== 'name' && (
                    <button
                        className="btn btn--outline btn--sm"
                        onClick={() => startEditing('name')}
                    >
                        Edit
                    </button>
                )}
            </div>

            {/* Email field */}
            <div className="profile__field">
                <div className="profile__field-info">
                    <div className="profile__field-label">Email Address</div>
                    {editingField === 'email' ? (
                        <form className="profile__edit-form" onSubmit={handleSave}>
                            <input
                                className="form-input"
                                type="email"
                                value={editEmail}
                                onChange={(e) => setEditEmail(e.target.value)}
                                placeholder="Email address"
                                required
                                aria-label="Email address"
                            />
                            <button
                                type="submit"
                                className="btn btn--primary btn--sm"
                                disabled={isSaving}
                            >
                                {isSaving ? 'Saving...' : 'Save'}
                            </button>
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                onClick={cancelEditing}
                            >
                                Cancel
                            </button>
                        </form>
                    ) : (
                        <div className="profile__field-value">{profile?.email}</div>
                    )}
                </div>
                {editingField !== 'email' && (
                    <button
                        className="btn btn--outline btn--sm"
                        onClick={() => startEditing('email')}
                    >
                        Edit
                    </button>
                )}
            </div>

            {/* Password field */}
            <div className="profile__field">
                <div className="profile__field-info">
                    <div className="profile__field-label">Password</div>
                    {editingField === 'password' ? (
                        <form className="profile__edit-form" onSubmit={handleSave}>
                            <input
                                className="form-input"
                                type="password"
                                value={editPassword}
                                onChange={(e) => setEditPassword(e.target.value)}
                                placeholder="New password (12-128 characters)"
                                required
                                minLength={12}
                                maxLength={128}
                                aria-label="New password"
                            />
                            <input
                                className="form-input"
                                type="password"
                                value={editConfirmPassword}
                                onChange={(e) => setEditConfirmPassword(e.target.value)}
                                placeholder="Confirm new password"
                                required
                                aria-label="Confirm new password"
                            />
                            <button
                                type="submit"
                                className="btn btn--primary btn--sm"
                                disabled={isSaving}
                            >
                                {isSaving ? 'Saving...' : 'Save'}
                            </button>
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                onClick={cancelEditing}
                            >
                                Cancel
                            </button>
                        </form>
                    ) : (
                        <div className="profile__field-value">••••••••••••</div>
                    )}
                </div>
                {editingField !== 'password' && (
                    <button
                        className="btn btn--outline btn--sm"
                        onClick={() => startEditing('password')}
                    >
                        Edit
                    </button>
                )}
            </div>

            {/* Last login (read-only) */}
            <div className="profile__field">
                <div className="profile__field-info">
                    <div className="profile__field-label">Last Login</div>
                    <div className="profile__field-value">
                        {profile ? formatDateTime(profile.lastLogin) : 'N/A'}
                    </div>
                </div>
            </div>
        </main>
    );
}