import { useState, useEffect, type SubmitEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/**
 * DeleteAccountPage is for implementing the page that lets
 * a logged in event host delete their account.
 * @returns JSX for the site's delete account page
 */
export function DeleteAccountPage() {
    const { isLoggedIn, authFetch, logout } = useAuth();
    const navigate = useNavigate();

    const [confirmed, setConfirmed] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    // If the user is not logged in, redirect to the home page.
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Handles the account deletion form submission. Calls
     * DELETE /api/v1/event-hosts. On success, discards the
     * local login session and redirects to the home page.
     * On failure, displays an error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!confirmed) {
            setErrorMessage('Please confirm that you understand the effects of deleting your account.');
            return;
        }

        setIsDeleting(true);

        try {
            const response = await authFetch('/api/v1/event-hosts', {
                method: 'DELETE',
            });
            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(data.message || 'Your account has been deleted.');
                // Discard the current login session and redirect home.
                logout();
                setTimeout(() => navigate('/', { replace: true }), 1500);
            } else {
                setErrorMessage(data.message || 'Failed to delete your account. Please try again.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsDeleting(false);
        }
    };

    return (
        <main className="page-container page-container--narrow">
            <div className="card delete-account">
                <h1 className="card__title">Account Deletion</h1>

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

                <p>
                    Deleting your account removes all:
                </p>
                <ul className="delete-account__list">
                    <li>Events you have created.</li>
                    <li>Registrations/invitations created for those events.</li>
                    <li>Records of tickets you've scanned over the course of your account's life.</li>
                    <li>Login session tokens for your account.</li>
                    <li>Password reset tokens for your account.</li>
                </ul>
                <p>
                    Please be certain you understand this before you proceed with deleting your account.
                </p>
                <p>
                    Before you attempt to delete your account, ensure that
                    you do not currently have any pending or ongoing events,
                    as they would need to either be finished or canceled
                    before deleting your account.
                </p>

                <form onSubmit={handleSubmit}>
                    <label className="delete-account__confirm-label" htmlFor="delete-confirm">
                        <input
                            id="delete-confirm"
                            type="checkbox"
                            checked={confirmed}
                            onChange={(e) => setConfirmed(e.target.checked)}
                        />
                        <span>
                            I understand the effects of deleting my account and I wish to proceed.
                        </span>
                    </label>

                    <div className="form-actions">
                        <button
                            type="submit"
                            className="btn btn--danger"
                            disabled={!confirmed || isDeleting}
                        >
                            {isDeleting ? 'Deleting Account...' : 'Delete My Account'}
                        </button>
                        <button
                            type="button"
                            className="btn btn--ghost"
                            onClick={() => navigate('/profile')}
                            disabled={isDeleting}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}