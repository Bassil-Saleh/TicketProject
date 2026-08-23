import { useState, type SubmitEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

/** The possible responses a user can send for an invitation. */
type InvitationResponse = 'ACCEPTED' | 'REJECTED';

/**
 * InvitationResponsePage is for implementing the page which
 * someone is sent to when they either click "Accept Invitation"
 * or "Reject Invitation" in the email they received when an
 * event host invites them to a private event.
 * @returns JSX for the site's invitation response page.
 */
export function InvitationResponsePage() {
    const [searchParams] = useSearchParams();

    // The public token is supplied as a URL query parameter
    // (e.g. /respond-to-invitation?publicToken=abc123).
    const publicToken = searchParams.get('publicToken');

    const [response, setResponse] = useState<InvitationResponse>('ACCEPTED');
    const [message, setMessage] = useState('');

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    /**
     * Handles the invitation response form submission. Calls
     * PATCH /api/v1/tickets/invitation with the public token,
     * the chosen response, and the optional message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');
        setIsSubmitting(true);

        try {
            const body: Record<string, string> = {
                publicToken: publicToken ?? '',
                invitationResponse: response,
            };
            if (message.trim() !== '') {
                body.message = message;
            }

            const res = await fetch('/api/v1/tickets/invitation', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
            });
            const data = await res.json();

            if (res.ok) {
                setSuccessMessage(
                    data.message ||
                    'Your response has been recorded. The event host has been notified.'
                );
                // Clear the message after a successful response.
                setMessage('');
            } else {
                setErrorMessage(data.message || 'Failed to record your response. Please try again.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    // A public token must be supplied as a URL query parameter in
    // order to respond to an invitation.
    if (!publicToken) {
        return (
            <main className="status-page">
                <div className="status-page__icon" aria-hidden="true">⚠️</div>
                <h2 className="status-page__title">Missing Invitation Token</h2>
                <p className="status-page__message">
                    No invitation token was provided. Please use the link from
                    your invitation email.
                </p>
                <Link to="/" className="btn btn--outline btn--lg">
                    Back to Home
                </Link>
            </main>
        );
    }

    return (
        <main className="page-container page-container--narrow">
            <div className="card">
                <h1 className="card__title">Respond to Your Invitation</h1>
                <p className="card__subtitle">
                    Let the event host know whether you can attend.
                </p>

                {successMessage && (
                    <>
                        <div className="alert alert--success" role="alert">
                            {successMessage}
                        </div>
                        <div className="auth-card__footer">
                            <Link to="/" className="btn btn--primary">
                                Back to Home
                            </Link>
                        </div>
                    </>
                )}

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                {!successMessage && (
                    <form onSubmit={handleSubmit} noValidate>
                        <div className="form-group">
                            <label className="form-label">Your Response</label>
                            <div className="invitation-response__options">
                                <label
                                    className={`invitation-response__option${response === 'ACCEPTED' ? ' invitation-response__option--selected' : ''}`}
                                >
                                    <input
                                        type="radio"
                                        name="invitation-response"
                                        value="ACCEPTED"
                                        checked={response === 'ACCEPTED'}
                                        onChange={() => setResponse('ACCEPTED')}
                                    />
                                    <span>✅ Accept</span>
                                </label>
                                <label
                                    className={`invitation-response__option${response === 'REJECTED' ? ' invitation-response__option--selected' : ''}`}
                                >
                                    <input
                                        type="radio"
                                        name="invitation-response"
                                        value="REJECTED"
                                        checked={response === 'REJECTED'}
                                        onChange={() => setResponse('REJECTED')}
                                    />
                                    <span>❌ Reject</span>
                                </label>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label" htmlFor="inv-response-message">
                                Message to the Event Host (optional)
                            </label>
                            <textarea
                                id="inv-response-message"
                                className="form-input"
                                value={message}
                                onChange={(e) => setMessage(e.target.value)}
                                placeholder="Can't make it today, sorry!"
                                maxLength={5000}
                                rows={4}
                            />
                            <div className="form-hint">
                                This message will be included with your response.
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="btn btn--primary btn--block"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Sending Response...' : 'Send Response'}
                        </button>
                    </form>
                )}

                <div className="auth-card__footer">
                    <Link to="/">Back to Home</Link>
                </div>
            </div>
        </main>
    );
}