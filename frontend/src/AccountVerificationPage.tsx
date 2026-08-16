import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

type VerificationStatus = 'loading' | 'success' | 'error' | 'missing-token';

/**
 * AccountVerificationPage is for implementing the page that
 * the user is sent to after clicking the link they received
 * by email to verify their account.
 * @returns JSX for the site's account verification page
 */
export function AccountVerificationPage() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');

    const [status, setStatus] = useState<VerificationStatus>('loading');
    const [message, setMessage] = useState('');

    /**
     * On mount, if a token is present in the URL query parameters,
     * call the verify account API endpoint. Otherwise, show a
     * "missing token" message.
     */
    useEffect(() => {
        if (!token) {
            setStatus('missing-token');
            setMessage(
                'No verification token was provided. Please use the link ' +
                'from your verification email.'
            );
            return;
        }

        const verifyAccount = async () => {
            try {
                const response = await fetch(
                    `/api/v1/event-hosts/verification?token=${encodeURIComponent(token)}`
                );
                const data = await response.json();

                if (response.ok) {
                    setStatus('success');
                    setMessage(
                        data.message ||
                        'Your account has been verified successfully! You can now log in.'
                    );
                } else {
                    setStatus('error');
                    setMessage(
                        data.message ||
                        'Account verification failed. The link may have expired.'
                    );
                }
            } catch {
                setStatus('error');
                setMessage('An unexpected error occurred. Please try again.');
            }
        };

        verifyAccount();
    }, [token]);

    return (
        <main className="status-page">
            {status === 'loading' && (
                <>
                    <div className="spinner spinner--lg" aria-label="Verifying account" />
                    <h2 className="status-page__title">Verifying Your Account...</h2>
                    <p className="status-page__message">
                        Please wait while we verify your account.
                    </p>
                </>
            )}

            {status === 'success' && (
                <>
                    <div className="status-page__icon" aria-hidden="true">✅</div>
                    <h2 className="status-page__title">Account Verified!</h2>
                    <p className="status-page__message">{message}</p>
                    <Link to="/login" className="btn btn--primary btn--lg">
                        Go to Log In
                    </Link>
                </>
            )}

            {status === 'error' && (
                <>
                    <div className="status-page__icon" aria-hidden="true">❌</div>
                    <h2 className="status-page__title">Verification Failed</h2>
                    <p className="status-page__message">{message}</p>
                    <Link to="/login" className="btn btn--outline btn--lg">
                        Back to Log In
                    </Link>
                </>
            )}

            {status === 'missing-token' && (
                <>
                    <div className="status-page__icon" aria-hidden="true">⚠️</div>
                    <h2 className="status-page__title">Missing Verification Token</h2>
                    <p className="status-page__message">{message}</p>
                    <Link to="/" className="btn btn--outline btn--lg">
                        Back to Home
                    </Link>
                </>
            )}
        </main>
    );
}